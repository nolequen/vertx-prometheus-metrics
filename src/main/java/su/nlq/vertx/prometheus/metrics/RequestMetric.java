package su.nlq.vertx.prometheus.metrics;

import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;

public final class RequestMetric {
  private final @NotNull SocketAddress localAddress;
  private final @NotNull SocketAddress remoteAddress;
  private final @NotNull Stopwatch stopwatch = new Stopwatch();

  public RequestMetric(@NotNull SocketAddress localAddress, @NotNull SocketAddress remoteAddress) {
    this.localAddress = localAddress;
    this.remoteAddress = remoteAddress;
  }

  public @NotNull Stopwatch getStopwatch() {
    return stopwatch;
  }

  public @NotNull SocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  public @NotNull SocketAddress getLocalAddress() {
    return localAddress;
  }
}
