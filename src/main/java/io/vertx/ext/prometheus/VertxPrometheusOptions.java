package io.vertx.ext.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.prometheus.server.ExpositionFormat;
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
  private @NotNull ExpositionFormat format = ExpositionFormat.Text;
  private @NotNull String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private boolean embeddedServerEnabled = false;

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
      metrics.add(MetricsType.valueOf(metric.toString()));
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

  @Override
  public @NotNull VertxPrometheusOptions setEnabled(boolean enable) {
    super.setEnabled(enable);
    return this;
  }

  /**
   * Set embedded Prometheus Metrics server port. Default is {@link VertxPrometheusOptions#DEFAULT_PORT}.
   *
   * @param port metrics server port
   * @return a reference to this, so the API can be used fluently
   */
  public @NotNull VertxPrometheusOptions setPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * Set embedded Prometheus Metrics server host. Default is {@link VertxPrometheusOptions#DEFAULT_HOST}.
   *
   * @param host metrics server host
   * @return a reference to this, so the API can be used fluently
   */
  public @NotNull VertxPrometheusOptions setHost(@NotNull String host) {
    this.host = host;
    return this;
  }

  /**
   * Enable metrics by type.
   *
   * @param type metrics type to enable
   * @return a reference to this, so the API can be used fluently
   */
  public @NotNull VertxPrometheusOptions enable(@NotNull MetricsType type) {
    metrics.add(type);
    return this;
  }

  /**
   * Disable metrics by type.
   *
   * @param type metrics type to disable
   * @return a reference to this, so the API can be used fluently
   */
  public @NotNull VertxPrometheusOptions disable(@NotNull MetricsType type) {
    metrics.remove(type);
    return this;
  }

  /**
   * Check whether metrics are enabled.
   *
   * @param type metrics type to check
   * @return a reference to this, so the API can be used fluently
   */
  public boolean isEnabled(@NotNull MetricsType type) {
    return metrics.contains(type);
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
   *
   * @param registry the Prometheus collector registry
   * @return a reference to this, so the API can be used fluently
   */
  public @NotNull VertxPrometheusOptions setRegistry(@NotNull CollectorRegistry registry) {
    this.registry = registry;
    return this;
  }

  /**
   * Check whether embedded server is enabled.
   *
   * @return true if embedded server is enabled
   */
  public boolean isEmbeddedServerEnabled() {
    return embeddedServerEnabled;
  }

  /**
   * Enable or disable embedded Prometheus server. Default is {@code false}.
   *
   * @param enabled or disable the embedded server
   * @return a reference to this, so the API can be used fluently
   */
  public @NotNull VertxPrometheusOptions setEmbeddedServerEnabled(boolean enabled) {
    this.embeddedServerEnabled = enable;
    return this;
  }

  /**
   * Prometheus exposition format for embedded server.
   *
   * @return registry
   */
  public @NotNull ExpositionFormat getFormat() {
    return format;
  }

  /**
   * Set Prometheus exposition format for embedded server. Default is {@link ExpositionFormat#Text}.
   *
   * @param format Prometheus exposition format
   * @return a reference to this, so the API can be used fluently
   */
  public @NotNull VertxPrometheusOptions setFormat(@NotNull ExpositionFormat format) {
    this.format = format;
    return this;
  }
}
