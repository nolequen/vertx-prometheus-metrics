package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.vertx.core.spi.metrics.PoolMetrics;
import io.vertx.ext.prometheus.metrics.counters.Stopwatch;
import io.vertx.ext.prometheus.metrics.counters.TimeCounter;
import org.jetbrains.annotations.NotNull;

public final class PoolPrometheusMetrics extends PrometheusMetrics implements PoolMetrics<Stopwatch> {
  private final @NotNull TaskMetrics tasks;
  private final @NotNull TimeMetrics time;

  public PoolPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String type, @NotNull String name, int maxSize) {
    super(registry);
    register(TaskMetrics.gauge);
    register(TimeMetrics.summary);
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
    time.delay.observe(submittedStopwatch.stop());
    return submittedStopwatch;
  }

  @Override
  public void end(@NotNull Stopwatch beginStopwatch, boolean succeeded) {
    time.process.observe(beginStopwatch.stop());
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
    private static final @NotNull Summary summary = new TimeCounter.SummaryBuilder()
        .get("vertx_pool_time_us", "Pool time metrics (us)")
        .labelNames("type", "name", "state")
        .create();

    private final @NotNull Summary.Child delay;
    private final @NotNull Summary.Child process;

    public TimeMetrics(@NotNull String type, @NotNull String name) {
      delay = summary.labels(type, name, "delay");
      process = summary.labels(type, name, "process");
    }
  }
}
