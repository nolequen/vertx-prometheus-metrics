package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DatagramSocketPrometheusMetrics extends PrometheusMetrics implements DatagramSocketMetrics {
  private final @NotNull Counter counter = Counter.build("vertx_datagram_socket", "Datagram socket metrics").labelNames("type", "address").create();

  private volatile @Nullable SocketAddress address;

  public DatagramSocketPrometheusMetrics(@NotNull CollectorRegistry registry) {
    super(registry);
    register(counter);
  }

  @Override
  public void listening(@NotNull String localName, @NotNull SocketAddress localAddress) {
    this.address = new SocketAddressImpl(localAddress.port(), localName);
  }

  @Override
  public void bytesRead(@NotNull Void socketMetric, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    counter.labels("received", String.valueOf(address)).inc(numberOfBytes);
  }

  @Override
  public void bytesWritten(@NotNull Void socketMetric, @NotNull SocketAddress remoteAddress, long numberOfBytes) {
    counter.labels("sent", remoteAddress.toString()).inc(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(@NotNull Void socketMetric, @NotNull SocketAddress remoteAddress, @NotNull Throwable t) {
    counter.labels("errors", remoteAddress.toString()).inc();
  }
}
