package su.nlq.vertx.prometheus.metrics;

import java.util.concurrent.TimeUnit;

public final class Stopwatch {
  private long startTime;

  public Stopwatch() {
    reset();
  }

  public long stop() {
    final long elapsed = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);
    reset();
    return elapsed;
  }

  public void reset() {
    startTime = System.nanoTime();
  }
}
