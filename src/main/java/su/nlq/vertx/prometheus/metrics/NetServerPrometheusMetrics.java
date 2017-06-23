package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;

public final class NetServerPrometheusMetrics extends TCPPrometheusMetrics {

  public NetServerPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull SocketAddress localAddress) {
    super(registry, "netserver", localAddress.toString());
  }
}
