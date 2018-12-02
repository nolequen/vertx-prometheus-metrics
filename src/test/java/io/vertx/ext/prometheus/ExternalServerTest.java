package io.vertx.ext.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.prometheus.server.ExpositionFormat;
import io.vertx.ext.prometheus.server.MetricsServer;
import org.junit.Test;

import java.net.InetSocketAddress;

public final class ExternalServerTest extends PrometheusMetricsTestCase {

  public ExternalServerTest() {
    super(options -> options.setEmbeddedServerEnabled(false));
  }

  @Test
  public void test() {
    MetricsServer
        .create(vertx())
        .apply(CollectorRegistry.defaultRegistry, ExpositionFormat.Text)
        .apply(new SocketAddressImpl(new InetSocketAddress(PORT)));

    await(response(body -> context().assertTrue(body.length() > 0, "Response is empty")));
  }
}
