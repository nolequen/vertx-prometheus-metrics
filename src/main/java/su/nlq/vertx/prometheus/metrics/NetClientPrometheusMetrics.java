package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.TCPMetrics;
import org.jetbrains.annotations.NotNull;

public final class NetClientPrometheusMetrics extends PrometheusMetrics implements TCPMetrics<SocketAddress> {

  private static final @NotNull Gauge connections = Gauge.build("vertx_netclient_connections", "Net client active connections number")
      .labelNames("remote_address").create();

  private static final @NotNull Counter bytes = Counter.build("vertx_netclient_bytes", "Net client read/write bytes")
      .labelNames("remote_address", "operation").create();

  private static final @NotNull Counter errors = Counter.build("vertx_netclient_errors", "Net client errors number")
      .labelNames("remote_address", "class").create();

  public NetClientPrometheusMetrics(@NotNull CollectorRegistry registry) {
    super(registry);
    register(connections);
    register(bytes);
    register(errors);
  }

  @Override
  public @NotNull SocketAddress connected(@NotNull SocketAddress remoteAddress, @NotNull String remoteName) {
    final SocketAddress namedRemoteAddress = new SocketAddressImpl(remoteAddress.port(), remoteName);
    connections.labels(namedRemoteAddress.toString()).inc();
    return namedRemoteAddress;
  }

  @Override
  public void disconnected(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress) {
    connections.labels(namedRemoteAddress.toString()).dec();
  }

  @Override
  public void bytesRead(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.labels(namedRemoteAddress.toString(), "read").inc(numberOfBytes);
  }

  @Override
  public void bytesWritten(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.labels(namedRemoteAddress.toString(), "write").inc(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, @NotNull Throwable throwable) {
    errors.labels(namedRemoteAddress.toString(), throwable.getClass().getSimpleName()).inc();
  }
}
