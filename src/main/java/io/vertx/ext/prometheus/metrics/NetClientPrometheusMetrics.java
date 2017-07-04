package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import org.jetbrains.annotations.NotNull;

public final class NetClientPrometheusMetrics extends TCPPrometheusMetrics {

  public NetClientPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String localAddress) {
    super(registry, "netclient", localAddress);
  }
}
