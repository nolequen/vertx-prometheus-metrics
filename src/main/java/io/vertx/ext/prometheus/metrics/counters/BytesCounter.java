package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Counter;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;
import org.jetbrains.annotations.NotNull;

public final class BytesCounter {
  private final @NotNull Counter counter;
  private final @NotNull String localAddress;

  public BytesCounter(@NotNull String name, @NotNull String localAddress) {
    this.localAddress = localAddress;
    counter = Counter.build("vertx_" + name + "_bytes", "Read/written bytes")
        .labelNames("local_address", "operation").create();
  }

  public void read(long bytes) {
    increment("read", bytes);
  }

  public void written(long bytes) {
    increment("write", bytes);
  }

  public @NotNull BytesCounter register(@NotNull PrometheusMetrics metrics) {
    metrics.register(counter);
    return this;
  }

  private void increment(@NotNull String operation, long bytes) {
    counter.labels(localAddress, operation).inc(bytes);
  }
}