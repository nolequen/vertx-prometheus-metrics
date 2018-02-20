package io.vertx.ext.prometheus.metrics.factories;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for shared counters.
 * Counters are identified by name and can only be registered once with a name.
 *
 * @author jansorg
 */
public class CounterFactory {
  private final CollectorRegistry registry;
  private final Map<String, Counter> counters = new ConcurrentHashMap<>();

  public CounterFactory(CollectorRegistry registry) {
    this.registry = registry;
  }

  /**
   * Unregisters the counters which were created by this factory from the registry.
   */
  public void close() {
    counters.values().forEach(registry::unregister);
  }

  /**
   * @param name The name of the counter, without prefix and suffix.
   * @return A counter of bytes for the given name. Counters with the same name are shared.
   */
  public Counter bytes(String name) {
    return counters.computeIfAbsent("vertx_" + name + "_bytes", key -> register(Counter.build(key, "Read/written bytes")
        .labelNames("local_address", "type").create()));
  }

  /**
   * @param name The name of the counter, without prefix and suffix.
   * @return A counter for errors, identified by the given name. Counters with the same name are shared.
   */
  public Counter errors(String name) {
    return counters.computeIfAbsent("vertx_" + name + "_errors", key -> register(Counter.build(key, "Errors number")
        .labelNames("local_address", "class").create()));
  }

  /**
   * @param name The name of the counter, without prefix and suffix.
   * @return A counter of http responses, identified by the given name. Counters with the same name are shared.
   */
  public Counter httpResponses(String name) {
    return counters.computeIfAbsent("vertx_" + name + "_responses", key -> register(Counter.build(key, "HTTP responses number")
        .labelNames("local_address", "code").create()));
  }

  private Counter register(Counter counter) {
    registry.register(counter);
    return counter;
  }
}
