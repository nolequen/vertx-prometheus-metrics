package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import org.jetbrains.annotations.NotNull;
import su.nlq.vertx.prometheus.metrics.counters.TimeCounter;
import su.nlq.vertx.prometheus.metrics.counters.WebsocketGauge;

public final class HTTPServerPrometheusMetrics extends TCPPrometheusMetrics implements HttpServerMetrics<RequestMetric, SocketAddress, SocketAddress> {
  private static final @NotNull Gauge requests = Gauge.build("vertx_httpserver_requests", "HTTP server requests number")
      .labelNames("local_address", "state").create();

  private final @NotNull SocketAddress localAddress;

  private final @NotNull WebsocketGauge websockets;
  private final @NotNull TimeCounter requestTime;

  public HTTPServerPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull SocketAddress localAddress) {
    super(registry, "httpserver", localAddress.toString());
    this.localAddress = localAddress;
    websockets = new WebsocketGauge("httpserver", localAddress.toString()).register(this);
    requestTime = new TimeCounter("httpserver_request", localAddress.toString()).register(this);
    register(requests);
  }

  @Override
  public @NotNull RequestMetric requestBegin(@NotNull SocketAddress namedRemoteAddress, @NotNull HttpServerRequest request) {
    requests.labels(localAddress.toString(), "active").inc();
    return new RequestMetric(localAddress, namedRemoteAddress);
  }

  @Override
  public void requestReset(@NotNull RequestMetric metric) {
    requestTime.apply(metric.getRemoteAddress(), metric.getStopwatch());
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
    //todo: response
    requestTime.apply(metric.getRemoteAddress(), metric.getStopwatch());
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
    websockets.increment(namedRemoteAddress);
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress) {
    websockets.decrement(namedRemoteAddress);
  }
}