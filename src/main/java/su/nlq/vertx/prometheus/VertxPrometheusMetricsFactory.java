package su.nlq.vertx.prometheus;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.metrics.VertxMetrics;
import org.jetbrains.annotations.NotNull;

public final class VertxPrometheusMetricsFactory implements VertxMetricsFactory {

  @Override
  public @NotNull VertxMetrics metrics(@NotNull Vertx vertx, @NotNull VertxOptions vertxOptions) {
    return new VertxPrometheusMetrics(vertx, getOptions(vertxOptions));
  }

  @Override
  public @NotNull MetricsOptions newOptions() {
    return new VertxPrometheusOptions();
  }

  private static @NotNull VertxPrometheusOptions getOptions(@NotNull VertxOptions vertxOptions) {
    final MetricsOptions metricsOptions = vertxOptions.getMetricsOptions();
    return metricsOptions instanceof VertxPrometheusOptions
        ? (VertxPrometheusOptions) metricsOptions
        : new VertxPrometheusOptions(metricsOptions.toJson());
  }
}
