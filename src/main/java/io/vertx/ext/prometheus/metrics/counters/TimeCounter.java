package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Summary;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;
import org.jetbrains.annotations.NotNull;

public final class TimeCounter {
  private final @NotNull Summary summary;
  private final @NotNull Summary.Child time;

  public TimeCounter(@NotNull String name, @NotNull String localAddress) {
    summary = new SummaryBuilder().get("vertx_" + name + "_time_us", "Processing time (us)")
        .labelNames("local_address")
        .create();
    time = summary.labels(localAddress);
  }

  public void apply(@NotNull Stopwatch stopwatch) {
    time.observe(stopwatch.stop());
  }

  public @NotNull TimeCounter register(@NotNull PrometheusMetrics metrics) {
    metrics.register(summary);
    return this;
  }

  public static final class SummaryBuilder {

    @SuppressWarnings("MagicNumber")
    public @NotNull Summary.Builder get(@NotNull String name, @NotNull String help) {
      return Summary.build(name, help)
          .quantile(0.5, 0.01)
          .quantile(0.7, 0.01)
          .quantile(0.9, 0.01)
          .quantile(0.99, 0.01);
    }
  }
}
