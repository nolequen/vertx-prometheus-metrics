package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import org.jetbrains.annotations.NotNull;
import su.nlq.vertx.prometheus.metrics.counters.TimeCounter;
import su.nlq.vertx.prometheus.metrics.counters.WebsocketGauge;

public final class HTTPClientPrometheusMetrics extends TCPPrometheusMetrics implements HttpClientMetrics<HTTPClientPrometheusMetrics.RequestMetric, SocketAddress, SocketAddress, SocketAddress, Stopwatch> {

  private static final @NotNull Gauge requests = Gauge.build("vertx_httpclient_requests", "HTTP client requests number")
      .labelNames("local_address", "remote_address", "method", "path", "state").create();

  private static final @NotNull Gauge endpoints = Gauge.build("vertx_endpoints", "Endpoints metrics")
      .labelNames("address", "counter").create();

  private final @NotNull WebsocketGauge websockets;
  private final @NotNull TimeCounter requestTime;
  private final @NotNull TimeCounter endpointQueueTime;

  public HTTPClientPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String localAddress) {
    super(registry, "httpclient", localAddress);
    register(requests);
    register(endpoints);
    websockets = new WebsocketGauge("httpclient", localAddress).register(this);
    requestTime = new TimeCounter("httpclient_request", localAddress).register(this);
    endpointQueueTime = new TimeCounter("httpclient_endpoint_queue", localAddress).register(this);
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
    websockets.increment(namedRemoteAddress);
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress) {
    websockets.decrement(namedRemoteAddress);
  }

  @Override
  public @NotNull Stopwatch enqueueRequest(@NotNull SocketAddress endpoint) {
    endpoints.labels(endpoint.toString(), "queue-size").inc();
    return new Stopwatch();
  }

  @Override
  public void dequeueRequest(@NotNull SocketAddress endpoint, @NotNull Stopwatch stopwatch) {
    endpoints.labels(endpoint.toString(), "queue-size").dec();
    endpointQueueTime.apply(endpoint, stopwatch);
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
    requestTime.apply(requestMetric.remoteAddress, requestMetric.stopwatch);
  }

  @Override
  public @NotNull RequestMetric responsePushed(@NotNull SocketAddress endpoint, @NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress, @NotNull HttpClientRequest request) {
    return requestBegin(endpoint, namedRemoteAddress, localAddress, remoteAddress, request);
  }

  @Override
  public void responseBegin(@NotNull RequestMetric requestMetric, @NotNull HttpClientResponse response) {
    //todo: response
    requestMetric.stopwatch.reset();
  }

  @Override
  public void responseEnd(@NotNull RequestMetric requestMetric, @NotNull HttpClientResponse response) {
    requestTime.apply(requestMetric.remoteAddress, requestMetric.stopwatch);
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