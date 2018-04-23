package io.vertx.ext.prometheus.metrics.factories;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory for shared histograms.
 *
 * @author jansorg
 */
public final class HistogramFactory {
  private final @NotNull CollectorRegistry registry;
  private final @NotNull Map<String, Histogram> histograms = new ConcurrentHashMap<>();

  public HistogramFactory(@NotNull CollectorRegistry registry) {
    this.registry = registry;
  }

  /**
   * Unregisters the histograms created by this factory from the registry.
   */
  public void close() {
    histograms.values().forEach(registry::unregister);
  }

  /**
   * Time histogram.
   *
   * @param name The name of the histogram, without prefix and suffix.
   * @return A histogram for http requests, identified by the given name. Histograms with the same name are shared.
   */
  public @NotNull Histogram timeSeconds(@NotNull String name) {
    return histograms.computeIfAbsent("vertx_" + name + "_time_seconds", key -> register(Histogram.build(key, "Processing time in seconds")
        .labelNames("local_address")
        .create()));
  }

  private @NotNull Histogram register(@NotNull Histogram histogram) {
    registry.register(histogram);
    return histogram;
  }
}
