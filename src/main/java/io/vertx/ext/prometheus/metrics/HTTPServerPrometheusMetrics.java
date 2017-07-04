package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.ext.prometheus.metrics.counters.HTTPRequestMetrics;
import io.vertx.ext.prometheus.metrics.counters.WebsocketGauge;
import org.jetbrains.annotations.NotNull;

public final class HTTPServerPrometheusMetrics extends TCPPrometheusMetrics implements HttpServerMetrics<HTTPRequestMetrics.Metric, SocketAddress, SocketAddress> {
  private static final @NotNull String NAME = "httpserver";

  private final @NotNull HTTPRequestMetrics requests;
  private final @NotNull WebsocketGauge websockets;

  public HTTPServerPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull SocketAddress localAddress) {
    super(registry, NAME, localAddress.toString());
    websockets = new WebsocketGauge(NAME, localAddress.toString()).register(this);
    requests = new HTTPRequestMetrics(NAME, localAddress.toString()).register(this);
  }

  @Override
  public @NotNull HTTPRequestMetrics.Metric requestBegin(@NotNull SocketAddress namedRemoteAddress, @NotNull HttpServerRequest request) {
    return requests.begin(namedRemoteAddress, request.method(), request.path());
  }

  @Override
  public void requestReset(@NotNull HTTPRequestMetrics.Metric metric) {
    requests.reset(metric);
  }

  @Override
  public @NotNull HTTPRequestMetrics.Metric responsePushed(@NotNull SocketAddress namedRemoteAddress, @NotNull HttpMethod method, @NotNull String uri, @NotNull HttpServerResponse response) {
    return requests.begin(namedRemoteAddress, method, uri);
  }

  @Override
  public void responseEnd(@NotNull HTTPRequestMetrics.Metric metric, @NotNull HttpServerResponse response) {
    requests.responseEnd(metric, response.getStatusCode());
  }

  @Override
  public @NotNull SocketAddress upgrade(@NotNull HTTPRequestMetrics.Metric metric, @NotNull ServerWebSocket serverWebSocket) {
    return requests.upgrade(metric);
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