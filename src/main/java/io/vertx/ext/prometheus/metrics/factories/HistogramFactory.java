package io.vertx.ext.prometheus.metrics.factories;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory for shared histograms.
 *
 * @author jansorg
 */
public class HistogramFactory {
  private final CollectorRegistry registry;
  private final Map<String, Histogram> histograms = new ConcurrentHashMap<>();

  public HistogramFactory(CollectorRegistry registry) {
    this.registry = registry;
  }

  /**
   * Unregisters the histograms created by this factory from the registry.
   */
  public void close() {
    histograms.values().forEach(registry::unregister);
  }

  /**
   * @param name The name of the counter, without prefix and suffix.
   * @return A histogram for http requests, identified by the given name. Histograms with the same name are shared.
   */
  public Histogram timeSeconds(String name) {
    return histograms.computeIfAbsent("vertx_" + name + "_time_seconds", key -> Histogram.build(key, "Processing time in seconds")
        .labelNames("local_address")
        .create());
  }
}
