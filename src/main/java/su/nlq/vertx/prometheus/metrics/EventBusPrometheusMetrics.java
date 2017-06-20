package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EventBusPrometheusMetrics extends PrometheusMetrics implements EventBusMetrics<EventBusPrometheusMetrics.Metric> {

  private static final @NotNull Gauge handlers = Gauge.build("vertx_eventbus_handlers", "Message subscriptions number")
      .labelNames("address").create();

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
    register(messages);
    register(failures);
    register(time);
    register(bytes);
  }

  @Override
  public @NotNull Metric handlerRegistered(@NotNull String address, @Nullable String repliedAddress) {
    handlers.labels(address).inc();
    return new Metric(address);
  }


  @Override
  public void handlerUnregistered(@NotNull Metric metric) {
    handlers.labels(metric.address).dec();
  }

  @Override
  public void scheduleMessage(@NotNull Metric metric, boolean local) {
    messages.labels(local ? "local" : "remote", "scheduled", metric.address).inc();
  }

  @Override
  public void beginHandleMessage(@NotNull Metric metric, boolean local) {
    final String range = local ? "local" : "remote";
    final String address = metric.address;
    messages.labels(range, "pending", address).dec();
    messages.labels(range, "scheduled", address).dec();
    metric.stopwatch.reset();
  }

  @Override
  public void endHandleMessage(@NotNull Metric metric, @Nullable Throwable failure) {
    final String address = metric.address;
    time.labels(address).inc(metric.stopwatch.stop());
    if (failure != null) {
      failures.labels(address, "request", failure.getClass().getSimpleName()).inc();
    }
  }

  @Override
  public void messageSent(@NotNull String address, boolean publish, boolean local, boolean remote) {
    messages.labels(local ? "local" : "remote", publish ? "publish" : "sent", address).inc();
  }

  @Override
  public void messageReceived(@NotNull String address, boolean publish, boolean local, int handlersNumber) {
    final String range = local ? "local" : "remote";
    messages.labels(range, "pending", address).inc(handlersNumber);
    messages.labels(range, "received", address).inc();
    if (handlersNumber > 0) {
      messages.labels(range, "delivered", address).inc();
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

  public static final class Metric {
    private final @NotNull String address;
    private final @NotNull Stopwatch stopwatch = new Stopwatch();

    public Metric(@NotNull String address) {
      this.address = address;
    }
  }
}