package io.vertx.ext.prometheus;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.TestContext;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.time.Duration;

public final class HTTPServerPrometheusMetricsTest extends PrometheusMetricsTestCase {

  private static final @NotNull String localhost = "127.0.0.1";

  @Test
  public void testMultipleServers(@NotNull TestContext context) {
    final int port1 = 10010;
    startServer(port1);

    final int port2 = 10020;
    startServer(port2);

    final String uri1 = "/clientRequest1";
    request(port1, uri1);

    final String uri2 = "/clientRequest2";
    request(port2, uri2);

    await(response(buffer -> {
      final String body = buffer.toString();
      context.assertTrue(body.contains("vertx_httpserver_responses{local_address=\"" + localhost + ':' + port1 + "\",code=\"200\",} 1.0"));
      context.assertTrue(body.contains("vertx_httpserver_responses{local_address=\"" + localhost + ':' + port2 + "\",code=\"200\",} 1.0\n"));

      context.assertTrue(body.contains("vertx_httpclient_requests{local_address=\"" + localhost + "\",method=\"GET\",path=\"" + uri1 + "\",state=\"total\",} 1.0\n"));
      context.assertTrue(body.contains("vertx_httpclient_requests{local_address=\"" + localhost + "\",method=\"GET\",path=\"" + uri2 + "\",state=\"total\",} 1.0\n"));
    }));
  }

  private void request(int port1, @NotNull String uri) {
    final HttpClient httpClient = vertx().createHttpClient(new HttpClientOptions().setLocalAddress(localhost));
    await(async -> httpClient.getNow(port1, localhost, uri, response -> async.countDown()));
  }

  private void startServer(int port) {
    await(Duration.ofSeconds(2), async ->
        vertx().createHttpServer(new HttpServerOptions().setPort(port).setHost(localhost))
            .requestHandler(event -> event.response().end("test response"))
            .listen(event -> async.countDown())
    );
  }
}