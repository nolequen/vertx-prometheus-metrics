package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.TCPMetrics;
import org.jetbrains.annotations.NotNull;

public final class NetServerPrometheusMetrics extends PrometheusMetrics implements TCPMetrics<SocketAddress> {

  private static final @NotNull Gauge connections = Gauge.build("vertx_netserver_connections", "Net server active connections number")
      .labelNames("local_address", "remote_address").create();

  private static final @NotNull Counter bytes = Counter.build("vertx_netserver_bytes", "Net server read/write bytes")
      .labelNames("local_address", "remote_address", "operation").create();

  private static final @NotNull Counter errors = Counter.build("vertx_netserver_errors", "Net server errors number")
      .labelNames("local_address", "remote_address", "class").create();

  private final @NotNull SocketAddress localAddress;

  public NetServerPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull SocketAddress localAddress) {
    super(registry);
    this.localAddress = localAddress;
    register(connections);
    register(bytes);
    register(errors);
  }

  @Override
  public @NotNull SocketAddress connected(@NotNull SocketAddress remoteAddress, @NotNull String remoteName) {
    final SocketAddress namedRemoteAddress = new SocketAddressImpl(remoteAddress.port(), remoteName);
    connections.labels(localAddress.toString(), namedRemoteAddress.toString()).inc();
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress) {
    connections.labels(localAddress.toString(), namedRemoteAddress.toString()).dec();
  }

  @Override
  public void bytesRead(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.labels(localAddress.toString(), namedRemoteAddress.toString(), "read").inc(numberOfBytes);
  }

  @Override
  public void bytesWritten(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.labels(localAddress.toString(), namedRemoteAddress.toString(), "write").inc(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, @NotNull Throwable throwable) {
    errors.labels(localAddress.toString(), namedRemoteAddress.toString(), throwable.getClass().getSimpleName()).inc();
  }
}
