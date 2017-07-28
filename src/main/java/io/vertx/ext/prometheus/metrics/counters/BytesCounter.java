package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Counter;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class BytesCounter {
  private final @NotNull Counter counter;
  private final @NotNull Supplier<String> localAddress;

  public BytesCounter(@NotNull String name, @NotNull String localAddress) {
    this(name, () -> localAddress);
  }

  public BytesCounter(@NotNull String name, @NotNull Supplier<String> localAddress) {
    this.localAddress = localAddress;
    counter = Counter.build("vertx_" + name + "_bytes", "Read/written bytes")
        .labelNames("local_address", "type").create();
  }

  public void read(long bytes) {
    increment("read", bytes);
  }

  public void written(long bytes) {
    increment("written", bytes);
  }

  public @NotNull BytesCounter register(@NotNull PrometheusMetrics metrics) {
    metrics.register(counter);
    return this;
  }

  private void increment(@NotNull String operation, long bytes) {
    counter.labels(localAddress.get(), operation).inc(bytes);
  }
}