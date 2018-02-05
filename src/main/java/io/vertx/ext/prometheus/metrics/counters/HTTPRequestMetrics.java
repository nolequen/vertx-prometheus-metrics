package io.vertx.ext.prometheus.metrics.counters;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.prometheus.metrics.PrometheusMetrics;
import org.jetbrains.annotations.NotNull;

public final class HTTPRequestMetrics {
  private final @NotNull Gauge requests;
  private final @NotNull Counter responses;
  private final @NotNull Stopwatch proocessTime;
  private final @NotNull String localAddress;

  public HTTPRequestMetrics(@NotNull String name, @NotNull String localAddress) {
    this.localAddress = localAddress;
    requests = Gauge.build("vertx_" + name + "_requests", "HTTP requests number")
        .labelNames("local_address", "method", "path", "state").create();
    responses = Counter.build("vertx_" + name + "_responses", "HTTP responses number")
        .labelNames("local_address", "code").create();
    proocessTime = new Stopwatch(name + "_requests", localAddress);
  }

  public @NotNull HTTPRequestMetrics register(@NotNull PrometheusMetrics metrics) {
    metrics.register(requests);
    metrics.register(responses);
    proocessTime.register(metrics);
    return this;
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
