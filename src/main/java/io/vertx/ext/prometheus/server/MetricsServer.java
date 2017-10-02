package io.vertx.ext.prometheus.server;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.Closeable;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Router;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public enum MetricsServer {
  ;
  private static final @NotNull Logger log = LoggerFactory.getLogger(MetricsServer.class);

  public static @NotNull Function<CollectorRegistry, Function<SocketAddress, Closeable>> create(@NotNull Vertx vertx) {
    final HttpServerOptions options = new HttpServerOptions().setCompressionSupported(true).setCompressionLevel(6);
    final HttpServer server = vertx.createHttpServer(options);
    final Router router = Router.router(vertx);
    return registry -> {
      router.route("/metrics").handler(new MetricsHandler(registry));
      return address -> {
        server.requestHandler(router::accept).listen(address.port(), address.host());
        log.info("Prometheus metrics server started at " + address);
        return server::close;
      };
    };
  }
}
