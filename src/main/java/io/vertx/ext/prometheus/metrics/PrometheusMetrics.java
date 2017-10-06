package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.vertx.core.spi.metrics.Metrics;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class PrometheusMetrics implements Metrics {
  private final @NotNull CollectorRegistry registry;
  private final @NotNull Map<Collector, Collector> collectors = new HashMap<>();

  protected PrometheusMetrics(@NotNull CollectorRegistry registry) {
    this.registry = registry;
  }

  @Override
  public final boolean isEnabled() {
    return !collectors.isEmpty();
  }

  @Override
  public final void close() {
    collectors.keySet().forEach(registry::unregister);
    collectors.clear();
  }

  public final void register(@NotNull Collector collector) {
    if (collectors.put(collector, collector) == null) {
      registry.register(collector);
    }
  }
}
