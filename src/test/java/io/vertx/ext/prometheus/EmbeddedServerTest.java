package io.vertx.ext.prometheus;

import org.junit.Test;

public final class EmbeddedServerTest extends PrometheusMetricsTestCase {

  @Test
  public void test() {
    await(response(body -> context().assertTrue(body.length() > 0, "Response is empty")));
  }
}