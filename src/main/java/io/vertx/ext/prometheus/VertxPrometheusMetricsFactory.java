package io.vertx.ext.prometheus;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.metrics.VertxMetrics;
import org.jetbrains.annotations.NotNull;

public final class VertxPrometheusMetricsFactory implements VertxMetricsFactory {


  @Override
  public VertxMetrics metrics(@NotNull VertxOptions vertxOptions) {
    return new VertxPrometheusMetrics( asPrometheusOptions(vertxOptions));
  }

  @Override
  public @NotNull MetricsOptions newOptions() {
    return new VertxPrometheusOptions();
  }

  @Override
  public MetricsOptions newOptions(JsonObject jsonObject) {
    return new VertxPrometheusOptions(jsonObject);
  }

  private static @NotNull VertxPrometheusOptions asPrometheusOptions(@NotNull VertxOptions vertxOptions) {
    final MetricsOptions metricsOptions = vertxOptions.getMetricsOptions();
    return metricsOptions instanceof VertxPrometheusOptions
        ? (VertxPrometheusOptions) metricsOptions
        : new VertxPrometheusOptions(metricsOptions.toJson());
  }
}
