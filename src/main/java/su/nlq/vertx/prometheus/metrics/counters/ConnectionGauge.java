package su.nlq.vertx.prometheus.metrics.counters;

import io.prometheus.client.Gauge;
import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;
import su.nlq.vertx.prometheus.metrics.PrometheusMetrics;

public final class ConnectionGauge {
  private final @NotNull Gauge gauge;
  private final @NotNull String localAddress;

  public ConnectionGauge(@NotNull String name, @NotNull String localAddress) {
    this.localAddress = localAddress;
    gauge = Gauge.build("vertx_" + name + "_connections", "Active connections number")
        .labelNames("local_address", "remote_address").create();
  }

  public void connected(@NotNull SocketAddress remoteAddress) {
    gauge(remoteAddress).inc();
  }

  public void disconnected(@NotNull SocketAddress remoteAddress) {
    gauge(remoteAddress).dec();
  }

  public @NotNull ConnectionGauge register(@NotNull PrometheusMetrics metrics) {
    metrics.register(gauge);
    return this;
  }

  private @NotNull Gauge.Child gauge(@NotNull SocketAddress remoteAddress) {
    return gauge.labels(localAddress, remoteAddress.toString());
  }
}
