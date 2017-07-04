package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.prometheus.metrics.Stopwatch;
import org.jetbrains.annotations.NotNull;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;

public final class EndpointMetrics {
  private final @NotNull Gauge gauge;
  private final @NotNull TimeCounter queueTime;
  private final @NotNull String localAddress;

  public EndpointMetrics(@NotNull String name, @NotNull String localAddress) {
    this.localAddress = localAddress;
    gauge = Gauge.build("vertx_" + name + "_endpoints", "Endpoints metrics")
        .labelNames("local_address", "remote_address", "counter").create();
    queueTime = new TimeCounter(name + "_endpoint_queue", localAddress);
  }

  public @NotNull EndpointMetrics register(@NotNull PrometheusMetrics metrics) {
    metrics.register(gauge);
    queueTime.register(metrics);
    return this;
  }

  public void increment(@NotNull SocketAddress endpoint) {
    connectionsGauge(endpoint).inc();
  }

  public void decrement(@NotNull SocketAddress endpoint) {
    connectionsGauge(endpoint).dec();
  }

  public @NotNull Stopwatch enqueue(@NotNull SocketAddress endpoint) {
    queueGauge(endpoint).inc();
    return new Stopwatch();
  }

  public void dequeue(@NotNull SocketAddress endpoint, @NotNull Stopwatch stopwatch) {
    queueGauge(endpoint).dec();
    queueTime.apply(endpoint, stopwatch);
  }

  private @NotNull Gauge.@NotNull Child queueGauge(@NotNull SocketAddress endpoint) {
    return gauge(endpoint, "queue-size");
  }

  private @NotNull Gauge.Child connectionsGauge(@NotNull SocketAddress endpoint) {
    return gauge(endpoint, "connections");
  }

  private @NotNull Gauge.Child gauge(@NotNull SocketAddress endpoint, @NotNull String name) {
    return gauge.labels(localAddress, endpoint.toString(), name);
  }
}
