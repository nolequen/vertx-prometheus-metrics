package io.vertx.ext.prometheus;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author jansorg
 */
public class HTTPServerPrometheusMetricsTest extends PrometheusMetricsTestCase {
  @Test
  public void testMultipleServers(TestContext context) {
    await(2, TimeUnit.SECONDS, async -> vertx().createHttpServer(new HttpServerOptions().setPort(10010).setHost("127.0.0.1"))
        .requestHandler(event -> event.response().end("Response server 1"))
        .listen(event -> async.countDown()));

    await(2, TimeUnit.SECONDS, async -> vertx().createHttpServer(new HttpServerOptions().setPort(10020).setHost("127.0.0.1"))
        .requestHandler(event -> event.response().end("Response server 1"))
        .listen(event -> async.countDown()));

    //request from both servers using different clients
    HttpClient client1 = vertx().createHttpClient(new HttpClientOptions().setLocalAddress("127.0.0.1"));
    await(getAsync -> client1.getNow(10010, "127.0.0.1", "/clientRequest1", response -> getAsync.countDown()));

    HttpClient client2 = vertx().createHttpClient(new HttpClientOptions().setLocalAddress("127.0.0.1"));
    await(getAsync -> client2.getNow(10020, "127.0.0.1", "/clientRequest2", response -> getAsync.countDown()));

    //make sure that metrics of all servers and clients are contained in the data
    await(response(buffer -> {
      String body = buffer.toString();
      //server metrics
      context.assertTrue(body.contains("vertx_httpserver_responses{local_address=\"127.0.0.1:10010\",code=\"200\",} 1.0"));
      context.assertTrue(body.contains("vertx_httpserver_responses{local_address=\"127.0.0.1:10020\",code=\"200\",} 1.0\n"));

      //client metrics
      context.assertTrue(body.contains("vertx_httpclient_requests{local_address=\"127.0.0.1\",method=\"GET\",path=\"/clientRequest1\",state=\"total\",} 1.0\n"));
      context.assertTrue(body.contains("vertx_httpclient_requests{local_address=\"127.0.0.1\",method=\"GET\",path=\"/clientRequest2\",state=\"total\",} 1.0\n"));
    }));
  }

}