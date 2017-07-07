package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Counter;
import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;

public final class ErrorCounter {
  private final @NotNull Counter counter;
  private final @NotNull String localAddress;

  public ErrorCounter(@NotNull String name, @NotNull String localAddress) {
    this.localAddress = localAddress;
    counter = Counter.build("vertx_" + name + "_errors", "Errors number")
        .labelNames("local_address", "class").create();
  }

  public void increment(@NotNull Throwable throwable) {
    counter.labels(localAddress, throwable.getClass().getSimpleName()).inc();
  }

  public @NotNull ErrorCounter register(@NotNull PrometheusMetrics metrics) {
    metrics.register(counter);
    return this;
  }
}