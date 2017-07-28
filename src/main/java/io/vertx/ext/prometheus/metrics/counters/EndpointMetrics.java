package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import org.jetbrains.annotations.NotNull;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;

public final class EndpointMetrics {
  private final @NotNull Gauge gauge;
  private final @NotNull TimeCounter queueTime;
  private final @NotNull String localAddress;

  public EndpointMetrics(@NotNull String name, @NotNull String localAddress) {
    this.localAddress = localAddress;
    gauge = Gauge.build("vertx_" + name + "_endpoints", "Endpoints number")
        .labelNames("local_address", "state").create();
    queueTime = new TimeCounter(name + "_endpoints_queue", localAddress);
  }

  public @NotNull EndpointMetrics register(@NotNull PrometheusMetrics metrics) {
    metrics.register(gauge);
    queueTime.register(metrics);
    return this;
  }

  public void increment() {
    gauge("established").inc();
  }

  public void decrement() {
    gauge("established").dec();
  }

  public @NotNull Stopwatch enqueue() {
    gauge("queued").inc();
    return new Stopwatch();
  }

  public void dequeue(@NotNull Stopwatch stopwatch) {
    gauge("queued").dec();
    queueTime.apply(stopwatch);
  }

  private @NotNull Gauge.Child gauge(@NotNull String name) {
    return gauge.labels(localAddress, name);
  }
}
