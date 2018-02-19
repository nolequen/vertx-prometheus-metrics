package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import org.jetbrains.annotations.NotNull;

public final class NetServerPrometheusMetrics extends TCPPrometheusMetrics {

  public NetServerPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull SocketAddress localAddress, @NotNull GaugeFactory gauges, @NotNull CounterFactory counters) {
    super(registry, "netserver", localAddress.toString(), gauges, counters);
  }
}
