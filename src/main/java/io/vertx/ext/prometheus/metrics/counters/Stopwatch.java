package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Histogram;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;
import org.jetbrains.annotations.NotNull;

public final class Stopwatch {
  private final @NotNull Histogram summary;
  private final @NotNull Histogram.Child time;

  public Stopwatch(@NotNull String name, @NotNull String localAddress) {
    summary = Histogram.build("vertx_" + name + "_time_seconds", "Processing time in seconds")
        .labelNames("local_address")
        .create();
    time = summary.labels(localAddress);
  }

  public @NotNull Stopwatch register(@NotNull PrometheusMetrics metrics) {
    metrics.register(summary);
    return this;
  }

  public @NotNull Histogram.Timer start() {
    return time.startTimer();
  }
}
