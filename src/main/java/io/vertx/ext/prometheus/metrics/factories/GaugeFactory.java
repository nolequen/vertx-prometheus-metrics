package io.vertx.ext.prometheus.metrics.factories;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory for shared gauges.
 * Gauges are identified by name, a gauge can only be shared once in a {@link CollectorRegistry}.
 *
 * @author jansorg
 */
public final class GaugeFactory {
  private final @NotNull CollectorRegistry registry;
  private final @NotNull Map<String, Gauge> gauges = new ConcurrentHashMap<>();

  public GaugeFactory(@NotNull CollectorRegistry registry) {
    this.registry = registry;
  }

  /**
   * Unregisters the gauges created by this factory from the registry.
   */
  public void close() {
    gauges.values().forEach(registry::unregister);
  }

  /**
   * HTTP requests gauge.
   *
   * @param name The name of the gauge, without prefix and suffix.
   * @return A gauge for http requests, identified by the given name. Gauges with the same name are shared.
   */
  public @NotNull Gauge httpRequests(@NotNull String name) {
    return gauges.computeIfAbsent("vertx_" + name + "_requests", key -> register(Gauge.build(key, "HTTP requests number")
        .labelNames("local_address", "method", "path", "state")
        .create()));
  }

  /**
   * Websockets requests gauge.
   *
   * @param name The name of the gauge, without prefix and suffix.
   * @return A gauge for websockets, identified by the given name. Gauges with the same name are shared.
   */
  public @NotNull Gauge websockets(@NotNull String name) {
    return gauges.computeIfAbsent("vertx_" + name + "_websockets", key -> register(Gauge.build(key, "Websockets number")
        .labelNames("local_address").create()));
  }

  /**
   * Connections gauge.
   *
   * @param name The name of the gauge, without prefix and suffix.
   * @return A gauge for connections, identified by the given name. Gauges with the same name are shared.
   */
  public @NotNull Gauge connections(@NotNull String name) {
    return gauges.computeIfAbsent("vertx_" + name + "_connections", key -> register(Gauge.build(key, "Active connections number")
        .labelNames("local_address").create()));
  }

  /**
   * Endpoints gauge.
   *
   * @param name The name of the gauge, without prefix and suffix.
   * @return A gauge for endpoints, identified by the given name. Gauges with the same name are shared.
   */
  public @NotNull Gauge endpoints(@NotNull String name) {
    return gauges.computeIfAbsent("vertx_" + name + "_endpoints", key -> register(Gauge.build(key, "Endpoints number")
        .labelNames("local_address", "state").create()));
  }

  private @NotNull Gauge register(@NotNull Gauge gauge) {
    registry.register(gauge);
    return gauge;
  }
}
