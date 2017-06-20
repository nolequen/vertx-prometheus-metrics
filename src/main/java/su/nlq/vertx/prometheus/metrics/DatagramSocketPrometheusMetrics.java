package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DatagramSocketPrometheusMetrics extends PrometheusMetrics implements DatagramSocketMetrics {
  private final @NotNull Counter counter = Counter.build("vertx_datagram_socket", "Datagram socket metrics")
      .labelNames("type", "local_address", "remote_address").create();

  private volatile @Nullable SocketAddress namedLocalAddress;

  public DatagramSocketPrometheusMetrics(@NotNull CollectorRegistry registry) {
    super(registry);
    register(counter);
  }

  @Override
  public void listening(@NotNull String localName, @NotNull SocketAddress localAddress) {
    this.namedLocalAddress = new SocketAddressImpl(localAddress.port(), localName);
  }

  @Override
  public void bytesRead(@Nullable Void socketMetric, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    counter.labels("received", String.valueOf(namedLocalAddress), remoteAddress.toString()).inc(numberOfBytes);
  }

  @Override
  public void bytesWritten(@Nullable Void socketMetric, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    counter.labels("sent", String.valueOf(namedLocalAddress), remoteAddress.toString()).inc(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(@Nullable Void socketMetric, @NotNull SocketAddress remoteAddress, @NotNull Throwable throwable) {
    counter.labels("errors", String.valueOf(namedLocalAddress), remoteAddress.toString()).inc();
  }
}
