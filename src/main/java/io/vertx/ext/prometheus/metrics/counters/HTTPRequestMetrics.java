package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import io.vertx.ext.prometheus.metrics.factories.HistogramFactory;
import org.jetbrains.annotations.NotNull;

public final class HTTPRequestMetrics {
  private final @NotNull Gauge requests;
  private final @NotNull Counter responses;
  private final @NotNull Stopwatch proocessTime;
  private final @NotNull String localAddress;

  public HTTPRequestMetrics(@NotNull String name, @NotNull String localAddress, @NotNull GaugeFactory gauges, @NotNull CounterFactory counters, @NotNull HistogramFactory histograms) {
    this.localAddress = localAddress;
    requests = gauges.httpRequests(name);
    responses = counters.httpResponses(name);
    proocessTime = new Stopwatch(name + "_requests", localAddress, histograms);
  }

  public @NotNull Metric begin(@NotNull HttpMethod method, @NotNull String path) {
    requests(method.name(), path, "active").inc();
    requests(method.name(), path, "total").inc();
    return new Metric(method, path, proocessTime.start());
  }

  public void reset(@NotNull Metric metric) {
    metric.resetTimer(proocessTime.start());
    requests(metric, "reset").inc();
    requests(metric, "processed").inc();
    requests(metric, "active").dec();
  }

  public void responseEnd(@NotNull Metric metric, int responseStatusCode) {
    metric.resetTimer(proocessTime.start());
    requests(metric, "active").dec();
    requests(metric, "processed").inc();
    responses(responseStatusCode).inc();
  }

  public void requestEnd(@NotNull Metric metric) {
    metric.resetTimer(proocessTime.start());
  }

  public void upgrade(@NotNull Metric metric) {
    requests(metric, "upgraded").inc();
  }

  private @NotNull Counter.Child responses(int responseStatusCode) {
    return responses.labels(localAddress, Integer.toString(responseStatusCode));
  }

  private @NotNull Gauge.Child requests(@NotNull HTTPRequestMetrics.@NotNull Metric metric, @NotNull String state) {
    return requests(metric.method.name(), metric.path, state);
  }

  private @NotNull Gauge.Child requests(@NotNull String method, @NotNull String path, @NotNull String state) {
    return requests.labels(localAddress, method, path, state);
  }

  public static final class Metric {
    private final @NotNull HttpMethod method;
    private final @NotNull String path;
    private @NotNull Histogram.Timer timer;

    public Metric(@NotNull HttpMethod method, @NotNull String path, @NotNull Histogram.Timer timer) {
      this.method = method;
      this.path = path;
      this.timer = timer;
    }

    public void resetTimer(@NotNull Histogram.Timer newTimer) {
      timer.observeDuration();
      timer = newTimer;
    }
  }
}
