package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import io.vertx.ext.prometheus.metrics.counters.EndpointMetrics;
import io.vertx.ext.prometheus.metrics.counters.HTTPRequestMetrics;
import io.vertx.ext.prometheus.metrics.counters.WebsocketGauge;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import io.vertx.ext.prometheus.metrics.factories.HistogramFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HTTPClientPrometheusMetrics extends TCPPrometheusMetrics implements HttpClientMetrics<HTTPRequestMetrics.Metric, Void, Void, Void, Histogram.Timer> {
  private static final @NotNull String NAME = "httpclient";

  private final @NotNull EndpointMetrics endpoints;
  private final @NotNull WebsocketGauge websockets;
  private final @NotNull HTTPRequestMetrics requests;

  public HTTPClientPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String localAddress, @NotNull GaugeFactory gauges, @NotNull CounterFactory counters, @NotNull HistogramFactory histograms) {
    super(registry, NAME, localAddress, gauges, counters);
    requests = new HTTPRequestMetrics(NAME, localAddress, gauges, counters, histograms);
    endpoints = new EndpointMetrics(NAME, localAddress, gauges, histograms);
    websockets = new WebsocketGauge(NAME, localAddress, gauges);
  }

  @Override
  public @Nullable Void createEndpoint(@NotNull String host, int port, int maxPoolSize) {
    return null;
  }

  @Override
  public void closeEndpoint(@NotNull String host, int port, @Nullable Void endpoint) {
    // nothing to do
  }

  @Override
  public void endpointConnected(@Nullable Void endpointMetric, @Nullable Void socketMetric) {
    endpoints.increment();
  }

  @Override
  public void endpointDisconnected(@Nullable Void endpointMetric, @Nullable Void socketMetric) {
    endpoints.decrement();
  }

  @Override
  public @Nullable Void connected(@Nullable Void endpointMetric, @Nullable Void socketMetric, @NotNull WebSocket webSocket) {
    websockets.increment();
    return socketMetric;
  }

  @Override
  public void disconnected(@Nullable Void endpointMetric) {
    websockets.decrement();
  }

  @Override
  public @NotNull Histogram.Timer enqueueRequest(@Nullable Void endpointMetric) {
    return endpoints.enqueue();
  }

  @Override
  public void dequeueRequest(@Nullable Void endpointMetric, @NotNull Histogram.Timer timer) {
    endpoints.dequeue(timer);
  }

  @Override
  public @NotNull HTTPRequestMetrics.Metric requestBegin(@Nullable Void endpointMetric, @Nullable Void socketMetric, @NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress, @NotNull HttpClientRequest request) {
    return requests.begin(request.method(), request.path());
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
  public @NotNull HTTPRequestMetrics.Metric responsePushed(@Nullable Void endpointMetric, @Nullable Void socketMetric, @NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress, @NotNull HttpClientRequest request) {
    return requestBegin(endpointMetric, socketMetric, localAddress, remoteAddress, request);
  }

  @Override
  public void responseBegin(@NotNull HTTPRequestMetrics.Metric requestMetric, @NotNull HttpClientResponse response) {
    // nothing to do
  }

  @Override
  public void responseEnd(@NotNull HTTPRequestMetrics.Metric requestMetric, @NotNull HttpClientResponse response) {
    requests.responseEnd(requestMetric, response.statusCode());
  }
}