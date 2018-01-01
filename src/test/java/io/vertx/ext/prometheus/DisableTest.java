package io.vertx.ext.prometheus;

import io.vertx.ext.unit.junit.VertxUnitRunnerWithParametersFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.EnumSet;

@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(VertxUnitRunnerWithParametersFactory.class)
public final class DisableTest extends PrometheusMetricsTestCase {
  public enum Metric {
    //DatagramSocket(MetricsType.DatagramSocket, "vertx_datagram_socket"),
    //Verticles(MetricsType.Verticles, "vertx_verticle"),
    //NetServer(MetricsType.NetServer, "vertx_netserver"),
    //NetClient(MetricsType.NetClient, "vertx_netclient"),
    //HttpServer(MetricsType.HTTPServer, "vertx_httpserver"),
    Timers(MetricsType.Timers, "vertx_timers"),
    EventBus(MetricsType.EventBus, "vertx_eventbus"),
    HttpClient(MetricsType.HTTPClient, "vertx_httpclient"),
    Pools(MetricsType.Pools, "vertx_pool");

    private final @NotNull MetricsType type;
    private final @NotNull String prefix;

    private Metric(@NotNull MetricsType type, @NotNull String prefix) {
      this.type = type;
      this.prefix = prefix;
    }
  }

  private final @NotNull Metric disabled;

  @Parameterized.Parameters
  public static @NotNull Iterable<Metric> data() {
    return Arrays.asList(Metric.values());
  }

  public DisableTest(@NotNull Metric disabled) {
    super(options -> options.disable(disabled.type));
    this.disabled = disabled;
  }

  @Test
  public void test() {
    await(response(body -> {
      final String content = body.toString();

      context().assertFalse(content.contains("# TYPE " + disabled.prefix), "Response contains " + disabled.prefix);

      EnumSet.complementOf(EnumSet.of(disabled))
          .forEach(enabled ->
              context().assertTrue(content.contains("# TYPE " + enabled.prefix), "Response doesn't contain " + enabled.prefix));
    }));
  }
}
