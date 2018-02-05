package io.vertx.ext.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.regex.Pattern;

public final class EventBusPrometheusMetrics extends PrometheusMetrics implements EventBusMetrics<EventBusPrometheusMetrics.Metric> {

  private final @NotNull Gauge handlers = Gauge
      .build("vertx_eventbus_handlers", "Message handlers number")
      .create();

  private final @NotNull Gauge respondents = Gauge
      .build("vertx_eventbus_respondents", "Reply handlers number")
      .create();

  private final @NotNull Gauge messages = Gauge
      .build("vertx_eventbus_messages", "EventBus messages metrics")
      .labelNames("range", "state", "address")
      .create();

  private final @NotNull Counter failures = Counter
      .build("vertx_eventbus_failures", "Message handling failures number")
      .labelNames("address", "type", "reason")
      .create();

  private final @NotNull Histogram time = Histogram.build("vertx_eventbus_messages_time_seconds", "Total messages processing time in seconds")
      .labelNames("address")
      .create();

  private final @NotNull Counter bytes = Counter
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
    return new Metric(address, respondent, time.labels(Address.Generated.apply(address)));
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
      metric.resetTimer();
    }
  }

  @Override
  public void endHandleMessage(@Nullable Metric metric, @Nullable Throwable failure) {
    if (metric != null) {
      metric.timer.observeDuration();
    }
    if (failure != null) {
      failures.labels(Address.Generated.apply(address(metric)), "request", failure.getClass().getSimpleName()).inc();
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
    failures.labels(Address.Generated.apply(address), "reply", failure.name()).inc();
  }

  private static @NotNull String address(@Nullable Metric metric) {
    return metric == null ? "unknown" : metric.address;
  }

  private @NotNull Counter.Child bytes(@NotNull String address, @NotNull String type) {
    return bytes.labels(Address.Generated.apply(address), type);
  }

  private @NotNull Gauge.Child messages(@NotNull String address, boolean local, @NotNull String state) {
    return messages.labels(local ? "local" : "remote", state, Address.Generated.apply(address));
  }

  private enum Address {
    Generated(Pattern.compile("^\\d+$"), "vertx-generated-address");

    private final @NotNull Pattern pattern;
    private final @NotNull String replacement;

    private Address(@NotNull Pattern pattern, @NotNull String replacement) {
      this.pattern = pattern;
      this.replacement = replacement;
    }

    public @NotNull String apply(@NotNull String address) {
      if (pattern.matcher(address).matches()) {
        return replacement;
      }
      if (address.split("-").length == 5) {
        return replacement;
      }
      return address;
    }
  }

  public static final class Metric {
    private final @NotNull String address;
    private final @NotNull Optional<String> respondent;
    private final @NotNull Histogram.Child histogram;
    private @NotNull Histogram.Timer timer;

    public Metric(@NotNull String address, @NotNull Optional<String> respondent, @NotNull Histogram.Child histogram) {
      this.address = address;
      this.respondent = respondent;
      this.histogram = histogram;
      timer = histogram.startTimer();
    }

    public @NotNull Histogram.Timer resetTimer() {
      timer = histogram.startTimer();
      return timer;
    }
  }
}