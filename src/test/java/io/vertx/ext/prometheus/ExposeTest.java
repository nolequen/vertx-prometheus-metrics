package io.vertx.ext.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunnerWithParametersFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(VertxUnitRunnerWithParametersFactory.class)
public final class ExposeTest extends PrometheusMetricsTestCase {

  @Parameterized.Parameters
  public static @NotNull Iterable<CollectorRegistry> data() {
    return Arrays.asList(
        CollectorRegistry.defaultRegistry,
        new CollectorRegistry(false) // custom registry
    );
  }

  public ExposeTest(@NotNull CollectorRegistry registry) {
    super(registry);
  }

  @Test
  public void eventBus(@NotNull TestContext context) {
    final EventBus eventBus = vertx().eventBus();
    await(latch -> {
      eventBus.consumer("test-address", message -> latch.complete());
      eventBus.publish("test-address", "test message");
    });

    test(context, Arrays.asList(
        "vertx_eventbus_messages{range=\"local\",state=\"delivered\",address=\"test-address\",} 1.0",
        "vertx_eventbus_messages{range=\"local\",state=\"received\",address=\"test-address\",} 1.0",
        "vertx_eventbus_messages{range=\"local\",state=\"scheduled\",address=\"test-address\",} 0.0",
        "vertx_eventbus_messages{range=\"local\",state=\"publish\",address=\"test-address\",} 1.0",
        "vertx_eventbus_messages{range=\"local\",state=\"pending\",address=\"test-address\",} 0.0"
    ));
  }

  private void test(@NotNull TestContext context, @NotNull Iterable<String> data) {
    await(response(body -> data.forEach(s -> context.assertTrue(body.toString().contains(s), "Response doesn't contain " + s))));
  }
}
