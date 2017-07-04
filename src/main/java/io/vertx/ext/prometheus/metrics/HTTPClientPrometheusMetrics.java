package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import org.jetbrains.annotations.NotNull;
import io.vertx.ext.prometheus.metrics.counters.EndpointMetrics;
import io.vertx.ext.prometheus.metrics.counters.HTTPRequestMetrics;
import io.vertx.ext.prometheus.metrics.counters.WebsocketGauge;

public final class HTTPClientPrometheusMetrics extends TCPPrometheusMetrics implements HttpClientMetrics<HTTPRequestMetrics.Metric, SocketAddress, SocketAddress, SocketAddress, Stopwatch> {
  private static final @NotNull String NAME = "httpclient";

  private final @NotNull EndpointMetrics endpoints;
  private final @NotNull WebsocketGauge websockets;
  private final @NotNull HTTPRequestMetrics requests;

  public HTTPClientPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String localAddress) {
    super(registry, NAME, localAddress);
    requests = new HTTPRequestMetrics(NAME, localAddress).register(this);
    endpoints = new EndpointMetrics(NAME, localAddress).register(this);
    websockets = new WebsocketGauge(NAME, localAddress).register(this);
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
    endpoints.increment(endpoint);
  }

  @Override
  public void endpointDisconnected(@NotNull SocketAddress endpoint, @NotNull SocketAddress socket) {
    endpoints.decrement(endpoint);
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
    endpoints.enqueue(endpoint);
    return new Stopwatch();
  }

  @Override
  public void dequeueRequest(@NotNull SocketAddress endpoint, @NotNull Stopwatch stopwatch) {
    endpoints.dequeue(endpoint, stopwatch);
  }

  @Override
  public @NotNull HTTPRequestMetrics.Metric requestBegin(@NotNull SocketAddress endpoint, @NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress, @NotNull HttpClientRequest request) {
    return requests.begin(namedRemoteAddress, request.method(), request.path());
  }

  @Override
  public void requestReset(@NotNull HTTPRequestMetrics.Metric requestMetric) {
    requests.reset(requestMetric);
  }

  @Override
  public void requestEnd(@NotNull HTTPRequestMetrics.Metric requestMetric) {
    requests.requestEnd(requestMetric);
  }

  @Override
  public @NotNull HTTPRequestMetrics.Metric responsePushed(@NotNull SocketAddress endpoint, @NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress, @NotNull HttpClientRequest request) {
    return requestBegin(endpoint, namedRemoteAddress, localAddress, remoteAddress, request);
  }

  @Override
  public void responseBegin(@NotNull HTTPRequestMetrics.Metric requestMetric, @NotNull HttpClientResponse response) {
  }

  @Override
  public void responseEnd(@NotNull HTTPRequestMetrics.Metric requestMetric, @NotNull HttpClientResponse response) {
    requests.responseEnd(requestMetric, response.statusCode());
  }
}