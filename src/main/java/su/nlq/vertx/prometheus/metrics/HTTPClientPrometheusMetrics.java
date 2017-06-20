package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import org.jetbrains.annotations.NotNull;

public final class HTTPClientPrometheusMetrics extends PrometheusMetrics implements HttpClientMetrics<RequestMetric, SocketAddress, SocketAddress, SocketAddress, Stopwatch> {

  private static final @NotNull Gauge connections = Gauge.build("vertx_httpclient_connections", "HTTP client active connections number")
      .labelNames("remote_address", "type").create();

  private static final @NotNull Counter bytes = Counter.build("vertx_httpclient_bytes", "HTTP client read/write bytes")
      .labelNames("remote_address", "operation").create();

  private static final @NotNull Counter errors = Counter.build("vertx_httpclient_errors", "HTTP client errors number")
      .labelNames("remote_address", "class").create();

  private static final @NotNull Gauge requests = Gauge.build("vertx_httpclient_requests", "HTTP client requests number")
      .labelNames("local_address", "remote_address", "state").create();

  private static final @NotNull Counter requestTime = Counter.build("vertx_httpclient_request_time", "HTTP client request/response processing time (μs)")
      .labelNames("operation", "local_address", "remote_address").create();

  private static final @NotNull Gauge endpoints = Gauge.build("vertx_endpoints", "Endpoints metrics")
      .labelNames("address", "counter").create();

  private static final @NotNull Gauge endpointQueueTime = Gauge.build("vertx_endpoint_queue_time", "Endpoint queue time")
      .labelNames("address").create();

  public HTTPClientPrometheusMetrics(@NotNull CollectorRegistry registry) {
    super(registry);
    register(connections);
    register(bytes);
    register(errors);
    register(requests);
    register(requestTime);
    register(endpoints);
  }

  @Override
  public @NotNull SocketAddress createEndpoint(@NotNull String host, int port, int maxPoolSize) {
    return new SocketAddressImpl(port, host);
  }

  @Override
  public void closeEndpoint(@NotNull String host, int port, @NotNull SocketAddress endpoint) {
  }

  @Override
  public void endpointConnected(@NotNull SocketAddress endpoint, @NotNull SocketAddress socket) {
    endpoints.labels(endpoint.toString(), "connections").inc();
  }

  @Override
  public void endpointDisconnected(@NotNull SocketAddress endpoint, @NotNull SocketAddress socket) {
    endpoints.labels(endpoint.toString(), "connections").dec();
  }

  @Override
  public @NotNull SocketAddress connected(@NotNull SocketAddress endpoint, @NotNull SocketAddress namedRemoteAddress, @NotNull WebSocket webSocket) {
    connections.labels(namedRemoteAddress.toString(), "websocket").inc();
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress) {
    connections.labels(namedRemoteAddress.toString(), "websocket").dec();
  }

  @Override
  public @NotNull SocketAddress connected(@NotNull SocketAddress remoteAddress, @NotNull String remoteName) {
    final SocketAddress namedRemoteAddress = new SocketAddressImpl(remoteAddress.port(), remoteName);
    connections.labels(namedRemoteAddress.toString(), "tcp").inc();
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress) {
    connections.labels(namedRemoteAddress.toString(), "tcp").dec();
  }

  @Override
  public @NotNull Stopwatch enqueueRequest(@NotNull SocketAddress endpoint) {
    endpoints.labels(endpoint.toString(), "queue-size").inc();
    return new Stopwatch();
  }

  @Override
  public void dequeueRequest(@NotNull SocketAddress endpoint, @NotNull Stopwatch stopwatch) {
    endpoints.labels(endpoint.toString(), "queue-size").dec();
    endpointQueueTime.labels(endpoint.toString()).inc(stopwatch.stop());
  }

  @Override
  public @NotNull RequestMetric requestBegin(@NotNull SocketAddress endpoint, @NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress, @NotNull HttpClientRequest request) {
    requests.labels(localAddress.toString(), namedRemoteAddress.toString(), "active").inc();
    requests.labels(localAddress.toString(), namedRemoteAddress.toString(), "total").inc();
    return new RequestMetric(localAddress, namedRemoteAddress);
  }

  @Override
  public void requestReset(@NotNull RequestMetric requestMetric) {
    requests.labels(requestMetric.getLocalAddress().toString(), requestMetric.getRemoteAddress().toString(), "active").dec();
  }

  @Override
  public void requestEnd(@NotNull RequestMetric requestMetric) {
    requestTime.labels("request", requestMetric.getLocalAddress().toString(), requestMetric.getRemoteAddress().toString()).inc(requestMetric.getStopwatch().stop());
  }

  @Override
  public @NotNull RequestMetric responsePushed(@NotNull SocketAddress endpoint, @NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress, @NotNull HttpClientRequest request) {
    return requestBegin(endpoint, namedRemoteAddress, localAddress, remoteAddress, request);
  }

  @Override
  public void responseBegin(@NotNull RequestMetric requestMetric, @NotNull HttpClientResponse response) {
    requestMetric.getStopwatch().reset();
  }

  @Override
  public void responseEnd(@NotNull RequestMetric requestMetric, @NotNull HttpClientResponse response) {
    requestTime.labels("response", requestMetric.getLocalAddress().toString(), requestMetric.getRemoteAddress().toString()).inc(requestMetric.getStopwatch().stop());
    requests.labels(requestMetric.getLocalAddress().toString(), requestMetric.getRemoteAddress().toString(), "active").dec();
  }

  @Override
  public void bytesRead(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.labels(namedRemoteAddress.toString(), "read").inc(numberOfBytes);
  }

  @Override
  public void bytesWritten(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.labels(namedRemoteAddress.toString(), "write").inc(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, @NotNull Throwable throwable) {
    errors.labels(namedRemoteAddress.toString(), throwable.getClass().getSimpleName()).inc();
  }
}