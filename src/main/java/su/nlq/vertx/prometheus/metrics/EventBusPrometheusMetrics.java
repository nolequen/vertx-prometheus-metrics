package su.nlq.vertx.prometheus.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EventBusPrometheusMetrics extends PrometheusMetrics implements EventBusMetrics<EventBusPrometheusMetrics.EventBuMetric> {

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
  public @NotNull EventBuMetric handlerRegistered(@NotNull String address, @NotNull String repliedAddress) {
    handlers.labels(address).inc();
    return new EventBuMetric(address);
  }


  @Override
  public void handlerUnregistered(@NotNull EventBuMetric handler) {
    handlers.labels(handler.address).dec();
  }

  @Override
  public void scheduleMessage(@NotNull EventBuMetric handler, boolean local) {
    messages.labels(local ? "local" : "remote", "scheduled", handler.address).inc();
  }

  @Override
  public void beginHandleMessage(@NotNull EventBuMetric handler, boolean local) {
    final String range = local ? "local" : "remote";
    final String address = handler.address;
    messages.labels(range, "pending", address).dec();
    messages.labels(range, "scheduled", address).dec();
    handler.stopwatch.reset();
  }

  @Override
  public void endHandleMessage(@NotNull EventBuMetric handler, @Nullable Throwable failure) {
    final String address = handler.address;
    time.labels(address).inc(handler.stopwatch.stop());
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

  public static final class EventBuMetric {
    private final @NotNull String address;
    private final @NotNull Stopwatch stopwatch = new Stopwatch();

    public EventBuMetric(@NotNull String address) {
      this.address = address;
    }
  }
}