package su.nlq.vertx.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import org.jetbrains.annotations.NotNull;
import su.nlq.vertx.prometheus.metrics.MetricsType;

import java.util.EnumSet;

public final class VertxPrometheusOptions extends MetricsOptions {

  private final @NotNull EnumSet<MetricsType> metrics = EnumSet.allOf(MetricsType.class);

  private @NotNull CollectorRegistry registry = CollectorRegistry.defaultRegistry;
  private @NotNull String host = "localhost";
  private int port = 9090;

  public VertxPrometheusOptions() {
    super();
  }

  public VertxPrometheusOptions(@NotNull VertxPrometheusOptions other) {
    super(other);
  }

  public VertxPrometheusOptions(@NotNull JsonObject json) {
    super(json);
  }

  @Override
  public @NotNull JsonObject toJson() {
    return super.toJson();
  }

  public @NotNull SocketAddress getAddress() {
    return new SocketAddressImpl(port, host);
  }

  public @NotNull VertxPrometheusOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public @NotNull VertxPrometheusOptions setHost(@NotNull String host) {
    this.host = host;
    return this;
  }

  public @NotNull VertxPrometheusOptions enable(@NotNull MetricsType metricsType) {
    metrics.add(metricsType);
    return this;
  }

  public @NotNull VertxPrometheusOptions disable(@NotNull MetricsType metricsType) {
    metrics.remove(metricsType);
    return this;
  }

  public @NotNull CollectorRegistry getRegistry() {
    return registry;
  }

  public @NotNull VertxPrometheusOptions setRegistry(@NotNull CollectorRegistry registry) {
    this.registry = registry;
    return this;
  }

  public boolean isEnabled(@NotNull MetricsType metricsType) {
    return metrics.contains(metricsType);
  }
}
