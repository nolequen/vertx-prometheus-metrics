package io.vertx.ext.prometheus.server;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.ProtobufFormatter;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class MetricsProtobufHandler implements Handler<RoutingContext> {
  private final @NotNull CollectorRegistry registry;

  public MetricsProtobufHandler(@NotNull CollectorRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void handle(@NotNull RoutingContext context) {
    context.vertx().<Buffer>executeBlocking(future -> {
      try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
        new ProtobufFormatter(registry.metricFamilySamples()).write(output);
        future.complete(Buffer.buffer(output.toByteArray()));
      } catch (IOException e) {
        future.fail(e);
      }
    }, false, result -> {
      if (result.succeeded()) {
        context.response()
            .setStatusCode(HttpResponseStatus.OK.code())
            .putHeader("Content-Type", ProtobufFormatter.CONTENT_TYPE)
            .end(result.result());
      } else {
        context.fail(result.cause());
      }
    });
  }
}
