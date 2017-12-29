package io.vertx.ext.prometheus;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.CollectorRegistry;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@RunWith(VertxUnitRunner.class)
public final class ServerTest {
  private @NotNull TestContext context;

  @Before
  public void setUp(@NotNull TestContext context) {
    this.context = context;
    CollectorRegistry.defaultRegistry.clear(); //todo: ?
  }

  @After
  public void tearDown() {
    CollectorRegistry.defaultRegistry.clear();
    //todo: close the vertx
  }

  @Test
  public void startEmbeddedServer() {
    final int port = 8081;

    await(
        response(
            port,
            options -> options.setPort(port),
            body -> context.assertTrue(body.length() > 0)
        )
    );
  }

  @Test
  public void attach() {

  }

  @Test
  public void shouldExcludeVerticleMetrics() {
    final int port = 8081;

    await(
        response(
            port,
            options -> options
                .setPort(port)
                .disable(MetricsType.Verticles),
            body -> {//todo: String?
              context.assertFalse(body.toString().contains("# HELP vertx_verticle"));
              context.assertTrue(body.toString().contains("# HELP vertx_pool_tasks"));
            }));
  }

  private void await(@NotNull Consumer<Async> task) {
    final Async async = context.async();
    task.accept(async);
    async.await(TimeUnit.SECONDS.toMillis(10));
  }

  private static @NotNull Vertx vertx(@NotNull UnaryOperator<VertxPrometheusOptions> options) {
    return Vertx.vertx(
        new VertxOptions().setMetricsOptions(
            options.apply(new VertxPrometheusOptions().setEnabled(true))
        )
    );
  }

  private @NotNull Consumer<Async> response(int port, @NotNull UnaryOperator<VertxPrometheusOptions> options, @NotNull Handler<Buffer> handler) {
    return async ->
        Vertx.vertx(
            new VertxOptions().setMetricsOptions(
                options.apply(new VertxPrometheusOptions().setEnabled(true))
            ))
            .createHttpClient()
            .get(port, "localhost", "/metrics")
            .handler(response -> {
              context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
              response.bodyHandler(body -> {
                handler.handle(body);
                async.complete();
              });
            })
            .end();
  }
}