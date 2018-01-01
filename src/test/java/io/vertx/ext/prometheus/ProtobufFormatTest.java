package io.vertx.ext.prometheus;

import io.prometheus.client.Metrics;
import io.vertx.ext.prometheus.server.ExpositionFormat;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class ProtobufFormatTest extends PrometheusMetricsTestCase {

  public ProtobufFormatTest() {
    super(options -> options.setFormat(ExpositionFormat.Protobuf));
  }

  @Test
  public void protobuf() {
    await(response(body -> {
      try {
        Metrics.MetricFamily.parseDelimitedFrom(new ByteArrayInputStream(body.getBytes()));
      } catch (IOException e) {
        context().fail(e);
      }
    }));
  }
}
