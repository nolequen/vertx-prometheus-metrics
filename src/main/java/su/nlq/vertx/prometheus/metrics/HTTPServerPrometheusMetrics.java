package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.http.*;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import org.jetbrains.annotations.NotNull;

public final class HTTPServerPrometheusMetrics extends PrometheusMetrics implements HttpServerMetrics<RequestMetric, SocketAddress, SocketAddress> {

  private static final @NotNull Gauge connections = Gauge.build("vertx_httpserver_connections", "HTTP server active connections number")
      .labelNames("local_address", "remote_address", "type").create();

  private static final @NotNull Gauge requests = Gauge.build("vertx_httpserver_requests", "HTTP server requests number")
      .labelNames("local_address", "state").create();

  private static final @NotNull Counter bytes = Counter.build("vertx_httpserver_bytes", "HTTP server read/write bytes")
      .labelNames("local_address", "remote_address", "operation").create();

  private static final @NotNull Counter time = Counter.build("vertx_httpserver_requests_time", "HTTP server total requests processing time (μs)")
      .labelNames("local_address").create();

  private static final @NotNull Counter errors = Counter.build("vertx_httpserver_errors", "HTTP server errors number")
      .labelNames("local_address", "remote_address", "class").create();

  private final @NotNull SocketAddress localAddress;

  public HTTPServerPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull SocketAddress localAddress) {
    super(registry);
    this.localAddress = localAddress;
    register(connections);
    register(requests);
    register(bytes);
    register(time);
    register(errors);
  }

  @Override
  public @NotNull RequestMetric requestBegin(@NotNull SocketAddress namedRemoteAddress, @NotNull HttpServerRequest request) {
    requests.labels(localAddress.toString(), "active").inc();
    return new RequestMetric(localAddress, namedRemoteAddress);
  }

  @Override
  public void requestReset(@NotNull RequestMetric metric) {
    requests.labels(localAddress.toString(), "processed").inc();
    requests.labels(localAddress.toString(), "active").dec();
  }

  @Override
  public @NotNull RequestMetric responsePushed(@NotNull SocketAddress namedRemoteAddress, @NotNull HttpMethod method, @NotNull String uri, @NotNull HttpServerResponse response) {
    requests.labels(localAddress.toString(), "active").inc();
    return new RequestMetric(localAddress, namedRemoteAddress);
  }

  @Override
  public void responseEnd(@NotNull RequestMetric metric, @NotNull HttpServerResponse response) {
    time.labels(localAddress.toString()).inc(metric.getStopwatch().stop());
    requests.labels(localAddress.toString(), "processed").inc();
    requests.labels(localAddress.toString(), "active").dec();
  }

  @Override
  public @NotNull SocketAddress upgrade(@NotNull RequestMetric metric, @NotNull ServerWebSocket serverWebSocket) {
    requests.labels(localAddress.toString(), "upgraded").inc();
    return metric.getRemoteAddress();
  }

  @Override
  public @NotNull SocketAddress connected(@NotNull SocketAddress namedRemoteAddress, @NotNull ServerWebSocket serverWebSocket) {
    connections.labels(localAddress.toString(), namedRemoteAddress.toString(), "websocket").inc();
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress) {
    connections.labels(localAddress.toString(), namedRemoteAddress.toString(), "websocket").dec();
  }

  @Override
  public @NotNull SocketAddress connected(@NotNull SocketAddress remoteAddress, @NotNull String remoteName) {
    final SocketAddress namedRemoteAddress = new SocketAddressImpl(remoteAddress.port(), remoteName);
    connections.labels(localAddress.toString(), namedRemoteAddress.toString(), "http").inc();
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress) {
    connections.labels(localAddress.toString(), namedRemoteAddress.toString(), "http").dec();
  }

  @Override
  public void bytesRead(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.labels(localAddress.toString(), namedRemoteAddress.toString(), "read").inc(numberOfBytes);
  }

  @Override
  public void bytesWritten(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.labels(localAddress.toString(), namedRemoteAddress.toString(), "write").inc(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, @NotNull Throwable throwable) {
    errors.labels(localAddress.toString(), namedRemoteAddress.toString(), throwable.getClass().getSimpleName()).inc();
  }
}