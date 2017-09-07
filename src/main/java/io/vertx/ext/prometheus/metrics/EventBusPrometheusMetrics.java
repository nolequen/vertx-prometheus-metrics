package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;
import io.vertx.ext.prometheus.metrics.counters.Stopwatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class EventBusPrometheusMetrics extends PrometheusMetrics implements EventBusMetrics<EventBusPrometheusMetrics.Metric> {

  private static final @NotNull Gauge handlers = Gauge
      .build("vertx_eventbus_handlers", "Message handlers number")
      .create();

  private static final @NotNull Gauge respondents = Gauge
      .build("vertx_eventbus_respondents", "Reply handlers number")
      .create();

  private static final @NotNull Gauge messages = Gauge
      .build("vertx_eventbus_messages", "EventBus messages metrics")
      .labelNames("range", "state", "address")
      .create();

  private static final @NotNull Counter failures = Counter
      .build("vertx_eventbus_failures", "Message handling failures number")
      .labelNames("address", "type", "reason")
      .create();

  private static final @NotNull Counter time = Counter
      .build("vertx_eventbus_messages_time", "Total messages processing time (us)")
      .labelNames("address")
      .create();

  private static final @NotNull Counter bytes = Counter
      .build("vertx_eventbus_bytes", "Total read/written bytes")
      .labelNames("address", "type")
      .create();

  public EventBusPrometheusMetrics(@NotNull CollectorRegistry registry) {
    super(registry);
    register(handlers);
    register(respondents);
    register(messages);
    register(failures);
    register(time);
    register(bytes);
  }

  @Override
  public @NotNull Metric handlerRegistered(@NotNull String address, @Nullable String repliedAddress) {
    handlers.inc();
    final Optional<String> respondent = Optional.ofNullable(repliedAddress);
    respondent.ifPresent(r -> respondents.inc());
    return new Metric(address, respondent);
  }


  @Override
  public void handlerUnregistered(@Nullable Metric metric) {
    handlers.dec();
    if (metric != null) {
      metric.respondent.ifPresent(r -> respondents.dec());
    }
  }

  @Override
  public void scheduleMessage(@Nullable Metric metric, boolean local) {
    messages(address(metric), local, "scheduled").inc();
  }

  @Override
  public void beginHandleMessage(@Nullable Metric metric, boolean local) {
    messages(address(metric), local, "pending").dec();
    messages(address(metric), local, "scheduled").dec();
    if (metric != null) {
      metric.stopwatch.reset();
    }
  }

  @Override
  public void endHandleMessage(@Nullable Metric metric, @Nullable Throwable failure) {
    if (metric != null) {
      time.labels(safe(metric.address)).inc(metric.stopwatch.stop());
    }
    if (failure != null) {
      failures.labels(safe(address(metric)), "request", failure.getClass().getSimpleName()).inc();
    }
  }

  @Override
  public void messageSent(@NotNull String address, boolean publish, boolean local, boolean remote) {
    messages(address, local, publish ? "publish" : "sent").inc();
  }

  @Override
  public void messageReceived(@NotNull String address, boolean publish, boolean local, int handlersNumber) {
    messages(address, local, "pending").inc(handlersNumber);
    messages(address, local, "received").inc();
    if (handlersNumber > 0) {
      messages(address, local, "delivered").inc();
    }
  }

  @Override
  public void messageWritten(@NotNull String address, int numberOfBytes) {
    bytes(address, "write").inc(numberOfBytes);
  }

  @Override
  public void messageRead(@NotNull String address, int numberOfBytes) {
    bytes(address, "read").inc(numberOfBytes);
  }

  @Override
  public void replyFailure(@NotNull String address, @NotNull ReplyFailure failure) {
    failures.labels(safe(address), "reply", failure.name()).inc();
  }

  private static @NotNull Counter.Child bytes(@NotNull String address, @NotNull String type) {
    return bytes.labels(safe(address), type);
  }

  private static @NotNull String address(@Nullable Metric metric) {
    return metric == null ? "unknown" : metric.address;
  }

  private static @NotNull Gauge.Child messages(@NotNull String address, boolean local, @NotNull String state) {
    return messages.labels(local ? "local" : "remote", state, safe(address));
  }

  private static @NotNull String safe(@NotNull String address) {
    try {
      UUID.fromString(address);
      return "vertx-generated-respondent";
    } catch (IllegalArgumentException ignore) {
      return address;
    }
  }

  public static final class Metric {
    private final @NotNull String address;
    private final @NotNull Optional<String> respondent;
    private final @NotNull Stopwatch stopwatch = new Stopwatch();

    public Metric(@NotNull String address, @NotNull Optional<String> respondent) {
      this.address = address;
      this.respondent = respondent;
    }
  }
}