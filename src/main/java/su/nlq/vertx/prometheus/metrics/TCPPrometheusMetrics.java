package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.TCPMetrics;
import org.jetbrains.annotations.NotNull;
import su.nlq.vertx.prometheus.metrics.counters.BytesCounter;
import su.nlq.vertx.prometheus.metrics.counters.ConnectionGauge;
import su.nlq.vertx.prometheus.metrics.counters.ErrorCounter;

public abstract class TCPPrometheusMetrics extends PrometheusMetrics implements TCPMetrics<SocketAddress> {
  private final @NotNull ConnectionGauge connections;
  private final @NotNull BytesCounter bytes;
  private final @NotNull ErrorCounter errors;

  protected TCPPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull String name, @NotNull String localAddress) {
    super(registry);
    connections = new ConnectionGauge(name, localAddress).register(this);
    errors = new ErrorCounter(name, localAddress).register(this);
    bytes = new BytesCounter(name, localAddress).register(this);
  }

  @Override
  public final @NotNull SocketAddress connected(@NotNull SocketAddress remoteAddress, @NotNull String remoteName) {
    final SocketAddress namedRemoteAddress = new SocketAddressImpl(remoteAddress.port(), remoteName);
    connections.connected(namedRemoteAddress);
    return namedRemoteAddress;
  }

  @Override
  public final void disconnected(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress) {
    connections.disconnected(namedRemoteAddress);
  }

  @Override
  public final void bytesRead(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.read(namedRemoteAddress, numberOfBytes);
  }

  @Override
  public final void bytesWritten(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.written(namedRemoteAddress, numberOfBytes);
  }

  @Override
  public final void exceptionOccurred(@NotNull SocketAddress namedRemoteAddress, @NotNull SocketAddress remoteAddress, @NotNull Throwable throwable) {
    errors.increment(namedRemoteAddress, throwable);
  }
}
