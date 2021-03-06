package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import io.vertx.ext.prometheus.metrics.factories.HistogramFactory;
import org.jetbrains.annotations.NotNull;

public final class EndpointMetrics {
  private final @NotNull Gauge gauge;
  private final @NotNull Stopwatch queueTime;
  private final @NotNull String localAddress;

  public EndpointMetrics(@NotNull String name, @NotNull String localAddress, @NotNull GaugeFactory gauges, @NotNull HistogramFactory histograms) {
    this.localAddress = localAddress;
    gauge = gauges.endpoints(name);
    queueTime = new Stopwatch(name + "_endpoints_queue", localAddress, histograms);
  }

  public void increment() {
    gauge("established").inc();
  }

  public void decrement() {
    gauge("established").dec();
  }

  public @NotNull Histogram.Timer enqueue() {
    gauge("queued").inc();
    return queueTime.start();
  }

  public void dequeue(@NotNull Histogram.Timer timer) {
    gauge("queued").dec();
    timer.observeDuration();
  }

  private @NotNull Gauge.Child gauge(@NotNull String name) {
    return gauge.labels(localAddress, name);
  }
}
