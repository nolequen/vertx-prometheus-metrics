package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;

public final class WebsocketGauge {
  private final @NotNull Gauge gauge;
  private final @NotNull String localAddress;

  public WebsocketGauge(@NotNull String name, @NotNull String localAddress) {
    this.localAddress = localAddress;
    gauge = Gauge.build("vertx_" + name + "_websockets", "Websockets number")
        .labelNames("local_address", "remote_address").create();
  }

  public void increment(@NotNull SocketAddress remoteAddress) {
    gauge(remoteAddress).inc();
  }

  public void decrement(@NotNull SocketAddress remoteAddress) {
    gauge(remoteAddress).inc();
  }

  public @NotNull WebsocketGauge register(@NotNull PrometheusMetrics metrics) {
    metrics.register(gauge);
    return this;
  }

  private @NotNull Gauge.Child gauge(@NotNull SocketAddress remoteAddress) {
    return gauge.labels(localAddress, remoteAddress.toString());
  }
}