package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.vertx.core.spi.metrics.PoolMetrics;
import org.jetbrains.annotations.NotNull;

public final class PoolPrometheusMetrics extends PrometheusMetrics implements PoolMetrics<Histogram.Timer> {
  private final @NotNull TaskMetrics tasks;
  private final @NotNull TimeMetrics time;

  public PoolPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String type, @NotNull String name, int maxSize) {
    super(registry);
    register(TaskMetrics.gauge);
    register(TimeMetrics.histogram);
    tasks = new TaskMetrics(type, name);
    time = new TimeMetrics(type, name);

    TaskMetrics.gauge.labels(type, name, "max_size").set(maxSize);
  }

  @Override
  public @NotNull Histogram.Timer submitted() {
    tasks.queued.inc();
    return time.delay.startTimer();
  }

  @Override
  public void rejected(@NotNull Histogram.Timer submittedTimer) {
    tasks.queued.dec();
  }

  @Override
  public @NotNull Histogram.Timer begin(@NotNull Histogram.Timer submittedTimer) {
    tasks.queued.dec();
    tasks.used.inc();
    submittedTimer.observeDuration();
    return time.process.startTimer();
  }

  @Override
  public void end(@NotNull Histogram.Timer beginTimer, boolean succeeded) {
    beginTimer.observeDuration();
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
    private static final @NotNull Histogram histogram = Histogram.build("vertx_pool_time_seconds", "Pool time metrics in seconds")
        .labelNames("type", "name", "state")
        .create();

    private final @NotNull Histogram.Child delay;
    private final @NotNull Histogram.Child process;

    public TimeMetrics(@NotNull String type, @NotNull String name) {
      delay = histogram.labels(type, name, "delay");
      process = histogram.labels(type, name, "process");
    }
  }
}
