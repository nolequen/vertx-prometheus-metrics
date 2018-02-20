package io.vertx.ext.prometheus;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.CollectorRegistry;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@RunWith(VertxUnitRunner.class)
abstract class PrometheusMetricsTestCase {
  protected static final int PORT = 8080;

  private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(10);

  private final @NotNull Function<VertxPrometheusOptions, VertxPrometheusOptions> options;
  private final @NotNull CollectorRegistry registry;

  private @NotNull TestContext context;

  private @NotNull Vertx vertx;

  protected PrometheusMetricsTestCase() {
    this(UnaryOperator.identity(), CollectorRegistry.defaultRegistry);
  }

  protected PrometheusMetricsTestCase(@NotNull UnaryOperator<VertxPrometheusOptions> options) {
    this(options, CollectorRegistry.defaultRegistry);
  }

  protected PrometheusMetricsTestCase(@NotNull CollectorRegistry registry) {
    this(UnaryOperator.identity(), registry);
  }

  private PrometheusMetricsTestCase(@NotNull UnaryOperator<VertxPrometheusOptions> options, @NotNull CollectorRegistry registry) {
    this.registry = registry;
    this.options = options.compose(o -> o
        .setEnabled(true)
        .setPort(PORT)
        .setRegistry(registry));
  }

  @Before
  public void setUp(@NotNull TestContext context) {
    this.context = context;
    this.vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(options.apply(new VertxPrometheusOptions())));
  }

  @After
  public void tearDown() {
    await(latch -> vertx.close(e -> {
      registry.clear();
      latch.complete();
    }));
  }

  protected final void await(long timeout, TimeUnit timeoutUnit, @NotNull Consumer<Async> task) {
    final Async latch = context.async();
    task.accept(latch);
    latch.await(timeoutUnit.toMillis(timeout));
  }

  protected final void await(@NotNull Consumer<Async> task) {
    await(TIMEOUT, TimeUnit.MILLISECONDS, task);
  }

  protected final @NotNull Consumer<Async> response(@NotNull Handler<Buffer> handler) {
    return latch -> request(handler, latch)
        .exceptionHandler(event -> {
          // too fast, try again after delay
          LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
          request(handler, latch).end();
        })
        .end();
  }

  protected final @NotNull TestContext context() {
    return context;
  }

  protected final @NotNull Vertx vertx() {
    return vertx;
  }

  private @NotNull HttpClientRequest request(@NotNull Handler<Buffer> handler, @NotNull Async latch) {
    return vertx.createHttpClient()
        .get(PORT, "localhost", "/metrics")
        .handler(response -> {
          context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
          response.bodyHandler(body -> {
            handler.handle(body);
            latch.complete();
          });
        });
  }
}
