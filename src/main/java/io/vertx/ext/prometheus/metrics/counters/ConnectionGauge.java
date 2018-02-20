package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import org.jetbrains.annotations.NotNull;

public final class ConnectionGauge {
  private final @NotNull Gauge.Child connections;

  public ConnectionGauge(@NotNull String name, @NotNull String localAddress, @NotNull GaugeFactory gauges) {
    Gauge gauge = gauges.connections(name);
    connections = gauge.labels(localAddress);
  }

  public void connected() {
    connections.inc();
  }

  public void disconnected() {
    connections.dec();
  }
}
