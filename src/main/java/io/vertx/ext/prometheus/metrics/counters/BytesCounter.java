package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Counter;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class BytesCounter {
  private final @NotNull Counter counter;
  private final @NotNull Supplier<String> localAddress;

  public BytesCounter(@NotNull String name, @NotNull String localAddress, @NotNull CounterFactory counters) {
    this(name, () -> localAddress, counters);
  }

  public BytesCounter(@NotNull String name, @NotNull Supplier<String> localAddress, @NotNull CounterFactory counters) {
    this.localAddress = localAddress;
    counter = counters.bytes(name);
  }

  public void read(long bytes) {
    increment("read", bytes);
  }

  public void written(long bytes) {
    increment("written", bytes);
  }

  private void increment(@NotNull String operation, long bytes) {
    counter.labels(localAddress.get(), operation).inc(bytes);
  }
}