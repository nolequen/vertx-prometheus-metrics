package io.vertx.ext.prometheus.server;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

public final class MetricsTextHandler implements Handler<RoutingContext> {
  private final @NotNull CollectorRegistry registry;

  public MetricsTextHandler(@NotNull CollectorRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void handle(@NotNull RoutingContext context) {
    context.vertx().<Buffer>executeBlocking(future -> {
      try (final BufferWriter writer = new BufferWriter()) {
        TextFormat.write004(writer, registry.metricFamilySamples());
        future.complete(writer.buffer);
      } catch (IOException e) {
        future.fail(e);
      }
    }, false, result -> {
      if (result.succeeded()) {
        context.response()
            .setStatusCode(HttpResponseStatus.OK.code())
            .putHeader("Content-Type", TextFormat.CONTENT_TYPE_004)
            .end(result.result());
      } else {
        context.fail(result.cause());
      }
    });
  }

  private static final class BufferWriter extends Writer {
    private final @NotNull Buffer buffer = Buffer.buffer();

    @Override
    public void write(@NotNull char[] cbuf, int off, int len) {
      buffer.appendString(new String(cbuf, off, len));
    }

    @Override
    public void flush() {
      // do nothing
    }

    @Override
    public void close() {
      // do nothing
    }
  }
}
