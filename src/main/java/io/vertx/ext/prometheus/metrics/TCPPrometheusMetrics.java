package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.ext.prometheus.metrics.counters.BytesCounter;
import io.vertx.ext.prometheus.metrics.counters.ConnectionGauge;
import io.vertx.ext.prometheus.metrics.counters.ErrorCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TCPPrometheusMetrics extends PrometheusMetrics implements TCPMetrics<Void> {
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
  public final @Nullable Void connected(@NotNull SocketAddress remoteAddress, @NotNull String remoteName) {
    connections.connected();
    return null;
  }

  @Override
  public final void disconnected(@Nullable Void metric, @NotNull SocketAddress remoteAddress) {
    connections.disconnected();
  }

  @Override
  public final void bytesRead(@Nullable Void metric, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.read(numberOfBytes);
  }

  @Override
  public final void bytesWritten(@Nullable Void metric, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.written(numberOfBytes);
  }

  @Override
  public final void exceptionOccurred(@Nullable Void metric, @NotNull SocketAddress remoteAddress, @NotNull Throwable throwable) {
    errors.increment(throwable);
  }
}
