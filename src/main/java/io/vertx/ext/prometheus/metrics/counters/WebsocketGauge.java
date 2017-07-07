package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import org.jetbrains.annotations.NotNull;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;

public final class WebsocketGauge {
  private final @NotNull Gauge gauge;
  private final @NotNull String localAddress;

  public WebsocketGauge(@NotNull String name, @NotNull String localAddress) {
    this.localAddress = localAddress;
    gauge = Gauge.build("vertx_" + name + "_websockets", "Websockets number")
        .labelNames("local_address").create();
  }

  public void increment() {
    gauge.labels(localAddress).inc();
  }

  public void decrement() {
    gauge.labels(localAddress).dec();
  }

  public @NotNull WebsocketGauge register(@NotNull PrometheusMetrics metrics) {
    metrics.register(gauge);
    return this;
  }
}