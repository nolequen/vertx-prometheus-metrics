package io.vertx.ext.prometheus;

public enum MetricsType {

  /**
   * Net server metrics.
   */
  NetServer,

  /**
   * Net client metrics.
   */
  NetClient,

  /**
   * HTTP server metrics.
   */
  HTTPServer,

  /**
   * HTTP client metrics.
   */
  HTTPClient,

  /**
   * Datagram socket metrics.
   */
  DatagramSocket,

  /**
   * Event bus metrics.
   */
  EventBus,

  /**
   * Pools metrics.
   */
  Pools,

  /**
   * Verticle metrics.
   */
  Verticles,

  /**
   * Timers metrics.
   */
  Timers
}
