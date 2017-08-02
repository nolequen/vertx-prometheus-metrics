package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;
import org.jetbrains.annotations.NotNull;

public final class WebsocketGauge {
  private final @NotNull Gauge gauge;
  private final @NotNull Gauge.Child websockets;

  public WebsocketGauge(@NotNull String name, @NotNull String localAddress) {
    gauge = Gauge.build("vertx_" + name + "_websockets", "Websockets number")
        .labelNames("local_address").create();
    websockets = gauge.labels(localAddress);
  }

  public void increment() {
    websockets.inc();
  }

  public void decrement() {
    websockets.dec();
  }

  public @NotNull WebsocketGauge register(@NotNull PrometheusMetrics metrics) {
    metrics.register(gauge);
    return this;
  }
}