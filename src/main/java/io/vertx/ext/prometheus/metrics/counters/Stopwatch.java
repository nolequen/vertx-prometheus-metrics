package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Histogram;
import io.vertx.ext.prometheus.metrics.factories.HistogramFactory;
import org.jetbrains.annotations.NotNull;

public final class Stopwatch {
  private final @NotNull Histogram.Child time;

  public Stopwatch(@NotNull String name, @NotNull String localAddress, @NotNull HistogramFactory histograms) {
    time = histograms.timeSeconds(name).labels(localAddress);
  }

  public @NotNull Histogram.Timer start() {
    return time.startTimer();
  }
}
