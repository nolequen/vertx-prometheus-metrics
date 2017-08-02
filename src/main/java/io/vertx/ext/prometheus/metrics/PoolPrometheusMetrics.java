package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.spi.metrics.PoolMetrics;
import io.vertx.ext.prometheus.metrics.counters.Stopwatch;
import org.jetbrains.annotations.NotNull;

public final class PoolPrometheusMetrics extends PrometheusMetrics implements PoolMetrics<Stopwatch> {
  private final @NotNull TaskMetrics tasks;
  private final @NotNull TimeMetrics time;

  public PoolPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String type, @NotNull String name, int maxSize) {
    super(registry);
    register(TaskMetrics.gauge);
    register(TimeMetrics.counter);
    tasks = new TaskMetrics(type, name);
    time = new TimeMetrics(type, name);

    TaskMetrics.gauge.labels(type, name, "max_size").set(maxSize);
  }

  @Override
  public @NotNull Stopwatch submitted() {
    tasks.queued.inc();
    return new Stopwatch();
  }

  @Override
  public void rejected(@NotNull Stopwatch submittedStopwatch) {
    tasks.queued.dec();
  }

  @Override
  public @NotNull Stopwatch begin(@NotNull Stopwatch submittedStopwatch) {
    tasks.queued.dec();
    tasks.used.inc();
    time.delay.inc(submittedStopwatch.stop());
    return submittedStopwatch;
  }

  @Override
  public void end(@NotNull Stopwatch beginStopwatch, boolean succeeded) {
    time.process.inc(beginStopwatch.stop());
    tasks.used.dec();
  }

  private static final class TaskMetrics {
    private static final @NotNull Gauge gauge = Gauge.build("vertx_pool_tasks", "Pool queue metrics")
        .labelNames("type", "name", "state").create();

    private final @NotNull Gauge.Child queued;
    private final @NotNull Gauge.Child used;

    public TaskMetrics(@NotNull String type, @NotNull String name) {
      queued = gauge.labels(type, name, "queued");
      used = gauge.labels(type, name, "used");
    }
  }

  private static final class TimeMetrics {
    private static final @NotNull Counter counter = Counter.build("vertx_pool_time", "Pool time metrics (us)")
        .labelNames("type", "name", "state").create();

    private final @NotNull Counter.Child delay;
    private final @NotNull Counter.Child process;

    public TimeMetrics(@NotNull String type, @NotNull String name) {
      delay = counter.labels(type, name, "delay");
      process = counter.labels(type, name, "process");
    }
  }
}
