package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.http.*;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import org.jetbrains.annotations.NotNull;

public final class HTTPClientPrometheusMetrics extends TCPPrometheusMetrics implements HttpClientMetrics<HTTPClientPrometheusMetrics.RequestMetric, SocketAddress, SocketAddress, SocketAddress, Stopwatch> {

  private static final @NotNull Gauge websockets = Gauge.build("vertx_httpserver_websockets", "HTTP client websockets number")
      .labelNames("local_address", "remote_address").create();

  private static final @NotNull Gauge requests = Gauge.build("vertx_httpclient_requests", "HTTP client requests number")
      .labelNames("local_address", "remote_address", "method", "path", "state").create();

  private static final @NotNull Counter requestTime = Counter.build("vertx_httpclient_request_time", "HTTP client request/response processing time (μs)")
      .labelNames("local_address", "remote_address").create();

  private static final @NotNull Gauge endpoints = Gauge.build("vertx_endpoints", "Endpoints metrics")
      .labelNames("address", "counter").create();

  private static final @NotNull Gauge endpointQueueTime = Gauge.build("vertx_endpoint_queue_time", "Endpoint queue time")
      .labelNames("address").create();

  private final @NotNull String localAddress;

  public HTTPClientPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String localAddress) {
    super(registry, "httpclient", localAddress);
    this.localAddress = localAddress;
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
    websockets.labels(localAddress, namedRemoteAddress.toString()).inc();
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress) {
    websockets.labels(localAddress, namedRemoteAddress.toString()).dec();
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
    requests.labels(localAddress.toString(), namedRemoteAddress.toString(), request.method().name(), request.path(), "active").inc();
    requests.labels(localAddress.toString(), namedRemoteAddress.toString(), request.method().name(), request.path(), "total").inc();
    return new RequestMetric(localAddress, namedRemoteAddress, request);
  }

  @Override
  public void requestReset(@NotNull RequestMetric requestMetric) {
    requests.labels(requestMetric.localAddress.toString(), requestMetric.remoteAddress.toString(), requestMetric.method.name(), requestMetric.path, "active").dec();
  }

  @Override
  public void requestEnd(@NotNull RequestMetric requestMetric) {
    requestTime.labels(requestMetric.localAddress.toString(), requestMetric.remoteAddress.toString(), requestMetric.method.name(), requestMetric.path).inc(requestMetric.stopwatch.stop());
  }

  @Override
  public @NotNull RequestMetric responsePushed(@NotNull SocketAddress endpoint, @NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress, @NotNull HttpClientRequest request) {
    return requestBegin(endpoint, namedRemoteAddress, localAddress, remoteAddress, request);
  }

  @Override
  public void responseBegin(@NotNull RequestMetric requestMetric, @NotNull HttpClientResponse response) {
    requestMetric.stopwatch.reset();
  }

  @Override
  public void responseEnd(@NotNull RequestMetric requestMetric, @NotNull HttpClientResponse response) {
    requestTime.labels(requestMetric.localAddress.toString(), requestMetric.remoteAddress.toString(), requestMetric.method.name(), requestMetric.path).inc(requestMetric.stopwatch.stop());
    requests.labels(requestMetric.localAddress.toString(), requestMetric.remoteAddress.toString(), requestMetric.method.name(), requestMetric.path, "active").dec();
  }

  public static final class RequestMetric {
    private final @NotNull SocketAddress localAddress;
    private final @NotNull SocketAddress remoteAddress;
    private final @NotNull HttpMethod method;
    private final @NotNull String path;
    private final @NotNull Stopwatch stopwatch = new Stopwatch();

    public RequestMetric(@NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress, @NotNull HttpClientRequest request) {
      this.localAddress = localAddress;
      this.remoteAddress = remoteAddress;
      this.method = request.method();
      this.path = request.path();
    }
  }
}