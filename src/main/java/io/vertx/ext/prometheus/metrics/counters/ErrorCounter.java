package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Counter;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class ErrorCounter {
  private final @NotNull Counter counter;
  private final @NotNull Supplier<String> localAddress;

  public ErrorCounter(@NotNull String name, @NotNull String localAddress, @NotNull CounterFactory counters) {
    this(name, () -> localAddress, counters);
  }

  public ErrorCounter(@NotNull String name, @NotNull Supplier<String> localAddress, @NotNull CounterFactory counters) {
    this.localAddress = localAddress;
    counter = counters.errors(name);
  }

  public void increment(@NotNull Throwable throwable) {
    counter.labels(localAddress.get(), throwable.getClass().getSimpleName()).inc();
  }
}