package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import org.jetbrains.annotations.NotNull;

public final class HTTPServerPrometheusMetrics extends TCPPrometheusMetrics implements HttpServerMetrics<RequestMetric, SocketAddress, SocketAddress> {

  private static final @NotNull Gauge websockets = Gauge.build("vertx_httpserver_websockets", "HTTP server websockets number")
      .labelNames("local_address", "remote_address").create();

  private static final @NotNull Gauge requests = Gauge.build("vertx_httpserver_requests", "HTTP server requests number")
      .labelNames("local_address", "state").create();

  private static final @NotNull Counter time = Counter.build("vertx_httpserver_requests_time", "HTTP server total requests processing time (μs)")
      .labelNames("local_address").create();

  private final @NotNull SocketAddress localAddress;

  public HTTPServerPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull SocketAddress localAddress) {
    super(registry, "httpserver", localAddress.toString());
    this.localAddress = localAddress;
    register(requests);
    register(time);
  }

  @Override
  public @NotNull RequestMetric requestBegin(@NotNull SocketAddress namedRemoteAddress, @NotNull HttpServerRequest request) {
    requests.labels(localAddress.toString(), "active").inc();
    return new RequestMetric(localAddress, namedRemoteAddress);
  }

  @Override
  public void requestReset(@NotNull RequestMetric metric) {
    time.labels(localAddress.toString()).inc(metric.getStopwatch().stop());
    requests.labels(localAddress.toString(), "reset").inc();
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
    websockets.labels(localAddress.toString(), namedRemoteAddress.toString()).inc();
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress) {
    websockets.labels(localAddress.toString(), namedRemoteAddress.toString()).dec();
  }
}