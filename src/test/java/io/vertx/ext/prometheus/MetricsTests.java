package io.vertx.ext.prometheus;

public final class MetricsTests {

  //todo: enable/disable metrics


//  @Test
//  public void shouldExposeEventBusMetrics(@NotNull TestContext context) {
//    vertx = Vertx.vertx(new VertxOptions()
//        .setMetricsOptions(new VertxPrometheusOptions().setEnabled(true)
//            .startDedicatedServer(8080)));
//
//    // Send something on the eventbus and wait til it's received
//    final Async asyncEB = context.async();
//    vertx.eventBus().consumer("test-eb", msg -> asyncEB.complete());
//    vertx.eventBus().publish("test-eb", "test message");
//    asyncEB.await(2000);
//
//    // Read metrics on HTTP endpoint for eventbus metrics
//    final Async async = context.async();
//    final HttpClientRequest req = vertx.createHttpClient()
//        .get(8080, "localhost", "/metrics")
//        .handler(res -> {
//          context.assertEquals(200, res.statusCode());
//          res.bodyHandler(body -> {
//            final String str = body.toString();
//            context.assertTrue(str.contains("vertx_eventbus_published{address=\"test-eb\",origin=\"local\",} 1.0"));
//            context.assertTrue(str.contains("vertx_eventbus_received{address=\"test-eb\",origin=\"local\",} 1.0"));
//            context.assertTrue(str.contains("vertx_eventbus_handlers{address=\"test-eb\",} 1.0"));
//            context.assertTrue(str.contains("vertx_eventbus_delivered{address=\"test-eb\",origin=\"local\",} 1.0"));
//            context.assertTrue(str.contains("vertx_eventbus_processing_time_count{address=\"test-eb\",} 1.0"));
//            async.complete();
//          });
//        });
//    req.end();
//  }
}
