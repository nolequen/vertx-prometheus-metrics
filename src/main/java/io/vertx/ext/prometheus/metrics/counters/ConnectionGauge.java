package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;
import org.jetbrains.annotations.NotNull;

public final class ConnectionGauge {
  private final @NotNull Gauge gauge;
  private final @NotNull Gauge.Child connections;

  public ConnectionGauge(@NotNull String name, @NotNull String localAddress) {
    gauge = Gauge.build("vertx_" + name + "_connections", "Active connections number")
        .labelNames("local_address").create();
    connections = gauge.labels(localAddress);
  }

  public void connected() {
    connections.inc();
  }

  public void disconnected() {
    connections.dec();
  }

  public @NotNull ConnectionGauge register(@NotNull PrometheusMetrics metrics) {
    metrics.register(gauge);
    return this;
  }
}
