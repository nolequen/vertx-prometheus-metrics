package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.vertx.core.spi.metrics.Metrics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public abstract class PrometheusMetrics implements Metrics {
  private final @NotNull CollectorRegistry registry;
  private final @NotNull Collection<Collector> collectors = new ArrayList<>();

  protected PrometheusMetrics(@NotNull CollectorRegistry registry) {
    this.registry = registry;
  }

  @Override
  public final boolean isEnabled() {
    return !collectors.isEmpty();
  }

  @Override
  public final void close() {
    collectors.forEach(registry::unregister);
    collectors.clear();
  }

  protected final void register(@NotNull Collector collector) {
    try {
      registry.register(collector);
      collectors.add(collector);
    } catch (IllegalArgumentException ignore) {
    }
  }
}
