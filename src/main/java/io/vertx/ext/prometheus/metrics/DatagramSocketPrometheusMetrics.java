package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import io.vertx.ext.prometheus.metrics.counters.BytesCounter;
import io.vertx.ext.prometheus.metrics.counters.ErrorCounter;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class DatagramSocketPrometheusMetrics extends PrometheusMetrics implements DatagramSocketMetrics {
  private static final @NotNull String NAME = "datagram_socket";

  private final @NotNull BytesCounter bytes;
  private final @NotNull ErrorCounter errors;

  private volatile @Nullable SocketAddress namedLocalAddress;

  public DatagramSocketPrometheusMetrics(@NotNull CollectorRegistry registry, @NotNull CounterFactory counters) {
    super(registry);
    final Supplier<String> localAddress = () -> String.valueOf(namedLocalAddress);
    bytes = new BytesCounter(NAME, localAddress, counters);
    errors = new ErrorCounter(NAME, localAddress, counters);
  }

  @Override
  public void listening(@NotNull String localName, @NotNull SocketAddress localAddress) {
    this.namedLocalAddress = new SocketAddressImpl(localAddress.port(), localName);
  }

  @Override
  public void bytesRead(@Nullable Void socketMetric, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.read(numberOfBytes);
  }

  @Override
  public void bytesWritten(@Nullable Void socketMetric, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    bytes.read(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(@Nullable Void socketMetric, @NotNull SocketAddress remoteAddress, @NotNull Throwable throwable) {
    errors.increment(throwable);
  }
}
