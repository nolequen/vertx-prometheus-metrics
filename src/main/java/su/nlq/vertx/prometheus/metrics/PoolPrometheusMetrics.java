package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.spi.metrics.PoolMetrics;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class PoolPrometheusMetrics extends PrometheusMetrics implements PoolMetrics<Stopwatch> {
  private final @NotNull String type;
  private final @NotNull String name;

  private static final @NotNull Gauge states = Gauge.build("vertx_pools", "Pools states")
      .labelNames("type", "name", "state").create();

  private static final @NotNull Counter time = Counter.build("vertx_pools_time", "Pools time metrics (μs)")
      .labelNames("type", "name", "state").create();

  public PoolPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String type, @NotNull String name, int maxSize) {
    super(registry);
    this.type = type;
    this.name = name;
    register(states);
    register(time);
    states.labels(type, name, "max_size").set(maxSize);
  }

  @Override
  public @NotNull Stopwatch submitted() {
    states.labels(type, name, "queued").inc();
    return new Stopwatch();
  }

  @Override
  public void rejected(@NotNull Stopwatch submittedStopwatch) {
    states.labels(type, name, "queued").dec();
  }

  @Override
  public @NotNull Stopwatch begin(@NotNull Stopwatch submittedStopwatch) {
    states.labels(type, name, "queued").dec();
    states.labels(type, name, "used").inc();

    time.labels(type, name, "delay").inc(submittedStopwatch.stop());
    return submittedStopwatch;
  }

  @Override
  public void end(@NotNull Stopwatch beginStopwatch, boolean succeeded) {
    time.labels(type, name, "usage").inc(TimeUnit.NANOSECONDS.toMicros(beginStopwatch.stop()));

    states.labels(type, name, "used").dec();
  }
}
