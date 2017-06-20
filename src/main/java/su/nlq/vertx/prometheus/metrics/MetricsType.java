package su.nlq.vertx.prometheus.metrics;

public enum MetricsType {
  NetServer,
  NetClient,
  HTTPServer,
  HTTPClient,
  DatagramSocket,
  EventBus,
  Pools,
  Verticles,
  Timers
}
