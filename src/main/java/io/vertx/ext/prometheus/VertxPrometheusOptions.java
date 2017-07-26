package io.vertx.ext.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

public final class VertxPrometheusOptions extends MetricsOptions {
  private static final @NotNull JsonArray EMPTY_METRICS = new JsonArray(Collections.emptyList());

  private static final @NotNull String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 9090;

  private final @NotNull EnumSet<MetricsType> metrics;

  private @NotNull CollectorRegistry registry = CollectorRegistry.defaultRegistry;
  private @NotNull String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private boolean embeddedServerEnabled = true;

  public VertxPrometheusOptions() {
    super();
    metrics = EnumSet.allOf(MetricsType.class);
  }

  public VertxPrometheusOptions(@NotNull VertxPrometheusOptions other) {
    super(other);
    registry = other.registry;
    host = other.host;
    port = other.port;
    metrics = EnumSet.copyOf(other.metrics);
  }

  public VertxPrometheusOptions(@NotNull JsonObject json) {
    super(json);
    host = json.getString("host", DEFAULT_HOST);
    port = json.getInteger("port", DEFAULT_PORT);
    metrics = EnumSet.noneOf(MetricsType.class);
    for (Object metric : json.getJsonArray("metrics", EMPTY_METRICS).getList()) {
      metrics.add((MetricsType) metric);
    }
  }

  @Override
  public @NotNull JsonObject toJson() {
    final JsonObject entries = super.toJson();
    entries.put("host", host);
    entries.put("port", port);
    entries.put("metrics", new JsonArray(new ArrayList<>(metrics)));
    return entries;
  }

  /**
   * Prometheus Metrics server address.
   *
   * @return server address
   */
  public @NotNull SocketAddress getAddress() {
    return new SocketAddressImpl(port, host);
  }

  /**
   * Set embedded Prometheus Metrics server port. Default is {@link VertxPrometheusOptions#DEFAULT_PORT}.
   */
  public @NotNull VertxPrometheusOptions setPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * Set embedded Prometheus Metrics server host. Default is {@link VertxPrometheusOptions#DEFAULT_HOST}.
   */
  public @NotNull VertxPrometheusOptions setHost(@NotNull String host) {
    this.host = host;
    return this;
  }

  /**
   * Enable metrics by type.
   */
  public @NotNull VertxPrometheusOptions enable(@NotNull MetricsType metricsType) {
    metrics.add(metricsType);
    return this;
  }

  /**
   * Disable metrics by type.
   */
  public @NotNull VertxPrometheusOptions disable(@NotNull MetricsType metricsType) {
    metrics.remove(metricsType);
    return this;
  }

  /**
   * Check whether metrics are enabled.
   */
  public boolean isEnabled(@NotNull MetricsType metricsType) {
    return metrics.contains(metricsType);
  }

  /**
   * Current Prometheus collector registry.
   *
   * @return registry
   */
  public @NotNull CollectorRegistry getRegistry() {
    return registry;
  }

  /**
   * Set Prometheus collector registry. Default is {@link CollectorRegistry#defaultRegistry}.
   */
  public @NotNull VertxPrometheusOptions setRegistry(@NotNull CollectorRegistry registry) {
    this.registry = registry;
    return this;
  }

  /**
   * Check whether embedded server is enabled.
   */
  public boolean isEmbeddedServerEnabled() {
    return embeddedServerEnabled;
  }

  /**
   * Enable or disable embedded Prometheus server. Default is {@code true}.
   */
  public @NotNull VertxPrometheusOptions enableEmbeddedServer(boolean enable) {
    this.embeddedServerEnabled = enable;
    return this;
  }
}
