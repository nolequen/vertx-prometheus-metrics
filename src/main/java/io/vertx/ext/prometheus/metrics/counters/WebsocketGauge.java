package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import org.jetbrains.annotations.NotNull;

public final class WebsocketGauge {
  private final @NotNull Gauge.Child websockets;

  public WebsocketGauge(@NotNull String name, @NotNull String localAddress, @NotNull GaugeFactory gauges) {
    Gauge gauge = gauges.websockets(name);
    websockets = gauge.labels(localAddress);
  }

  public void increment() {
    websockets.inc();
  }

  public void decrement() {
    websockets.dec();
  }
}