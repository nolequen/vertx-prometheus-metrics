package su.nlq.vertx.prometheus.metrics.counters;

import io.prometheus.client.Counter;
import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;
import su.nlq.vertx.prometheus.metrics.PrometheusMetrics;

public final class ErrorCounter {
  private final @NotNull Counter counter;
  private final @NotNull String localAddress;

  public ErrorCounter(@NotNull String name, @NotNull String localAddress) {
    this.localAddress = localAddress;
    counter = Counter.build("vertx_" + name + "_errors", "Errors number")
        .labelNames("local_address", "remote_address", "class").create();
  }

  public void increment(@NotNull SocketAddress remoteAddress, @NotNull Throwable throwable) {
    counter.labels(localAddress, remoteAddress.toString(), throwable.getClass().getSimpleName()).inc();
  }

  public @NotNull ErrorCounter register(@NotNull PrometheusMetrics metrics) {
    metrics.register(counter);
    return this;
  }
}