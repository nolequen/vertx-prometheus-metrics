package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class EventBusPrometheusMetrics extends PrometheusMetrics implements EventBusMetrics<EventBusPrometheusMetrics.Metric> {

  private static final @NotNull Gauge handlers = Gauge.build("vertx_eventbus_handlers", "Message handlers number")
      .labelNames("address").create();

  private static final @NotNull Gauge respondents = Gauge.build("vertx_eventbus_respondents", "Reply handlers number")
      .labelNames("handler_address").create();

  private static final @NotNull Gauge messages = Gauge.build("vertx_eventbus_messages", "EventBus messages metrics")
      .labelNames("range", "status", "address").create();

  private static final @NotNull Counter failures = Counter.build("vertx_eventbus_failures", "Message handling failures number")
      .labelNames("address", "direction", "reason").create();

  private static final @NotNull Counter time = Counter.build("vertx_eventbus_messages_time", "Total messages processing time (μs)")
      .labelNames("address").create();

  private static final @NotNull Counter bytes = Counter.build("vertx_eventbus_data", "Total write/read bytes by address")
      .labelNames("address", "operation").create();

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
    handlers.labels(address).inc();
    final Optional<String> respondent = Optional.ofNullable(repliedAddress);
    respondent.ifPresent(r -> respondents.labels(address).inc());
    return new Metric(address, respondent);
  }


  @Override
  public void handlerUnregistered(@NotNull Metric metric) {
    handlers.labels(metric.address).dec();
    metric.respondent.ifPresent(r -> respondents.labels(metric.address).dec());
  }

  @Override
  public void scheduleMessage(@Nullable Metric metric, boolean local) {
  }

  @Override
  public void beginHandleMessage(@NotNull Metric metric, boolean local) {
    final String address = metric.address;
    messages(address, local, "pending").dec();
    metric.stopwatch.reset();
  }

  @Override
  public void endHandleMessage(@NotNull Metric metric, @Nullable Throwable failure) {
    time.labels(metric.address).inc(metric.stopwatch.stop());
    if (failure != null) {
      failures.labels(metric.address, "request", failure.getClass().getSimpleName()).inc();
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
    bytes.labels(address, "write").inc(numberOfBytes);
  }

  @Override
  public void messageRead(@NotNull String address, int numberOfBytes) {
    bytes.labels(address, "read").inc(numberOfBytes);
  }

  @Override
  public void replyFailure(@NotNull String address, @NotNull ReplyFailure failure) {
    failures.labels(address, "reply", failure.name()).inc();
  }

  private static @NotNull Gauge.Child messages(@NotNull String address, boolean local, @NotNull String state) {
    return messages.labels(local ? "local" : "remote", state, address);
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