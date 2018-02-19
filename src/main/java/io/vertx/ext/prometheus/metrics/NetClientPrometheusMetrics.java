package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import org.jetbrains.annotations.NotNull;

public final class NetClientPrometheusMetrics extends TCPPrometheusMetrics {

  public NetClientPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String localAddress, @NotNull GaugeFactory gauges, @NotNull CounterFactory counters) {
    super(registry, "netclient", localAddress, gauges, counters);
  }
}
