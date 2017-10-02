package io.vertx.ext.prometheus.server;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;

public enum ExpositionFormat {

  Text {
    @Override
    @NotNull Handler<RoutingContext> handler(@NotNull CollectorRegistry registry) {
      return new MetricsTextHandler(registry);
    }
  },

  Protobuf {
    @Override
    @NotNull Handler<RoutingContext> handler(@NotNull CollectorRegistry registry) {
      return new MetricsProtobufHandler(registry);
    }
  };

  @NotNull
  abstract Handler<RoutingContext> handler(@NotNull CollectorRegistry registry);
}
