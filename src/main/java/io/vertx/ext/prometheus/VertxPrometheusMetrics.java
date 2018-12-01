package io.vertx.ext.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.vertx.core.Closeable;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.impl.DummyVertxMetrics;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.*;
import io.vertx.ext.prometheus.metrics.*;
import io.vertx.ext.prometheus.metrics.factories.CounterFactory;
import io.vertx.ext.prometheus.metrics.factories.GaugeFactory;
import io.vertx.ext.prometheus.metrics.factories.HistogramFactory;
import io.vertx.ext.prometheus.server.MetricsServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.vertx.ext.prometheus.MetricsType.*;

public final class VertxPrometheusMetrics extends DummyVertxMetrics {
  private final @NotNull VertxPrometheusOptions options;
  private final @NotNull VerticleMetrics verticleMetrics;
  private final @NotNull TimerMetrics timerMetrics;
  private final @NotNull GaugeFactory gauges;
  private final @NotNull CounterFactory counters;
  private final @NotNull HistogramFactory histograms;

  private @Nullable Closeable server;

  public VertxPrometheusMetrics(@NotNull VertxPrometheusOptions options) {
    this.options = options;
    this.verticleMetrics = options.isEnabled(Verticles) ? new VerticlePrometheusMetrics(options.getRegistry()) : new VerticleDummyMetrics();
    this.timerMetrics = options.isEnabled(Timers) ? new TimerPrometheusMetrics(options.getRegistry()) : new TimerDummyMetrics();
    this.gauges = new GaugeFactory(options.getRegistry());
    this.counters = new CounterFactory(options.getRegistry());
    this.histograms = new HistogramFactory(options.getRegistry());
  }

  @Override
  public void vertxCreated(@NotNull Vertx vertx) {
    if (options.isEmbeddedServerEnabled()) {
      server = MetricsServer
          .create(vertx)
          .apply(options.getRegistry(), options.getFormat())
          .apply(options.getAddress());
    }
  }

  @Override
  public void verticleDeployed(@NotNull Verticle verticle) {
    verticleMetrics.deployed(verticle);
  }

  @Override
  public void verticleUndeployed(@NotNull Verticle verticle) {
    verticleMetrics.undeployed(verticle);
  }

  @Override
  public void timerCreated(long id) {
    timerMetrics.created(id);
  }

  @Override
  public void timerEnded(long id, boolean cancelled) {
    timerMetrics.ended(id, cancelled);
  }

  @Override
  public @NotNull EventBusMetrics createEventBusMetrics() {
    return options.isEnabled(EventBus)
        ? new EventBusPrometheusMetrics(options.getRegistry())
        : super.createEventBusMetrics();
  }

  @Override
  public @NotNull HttpClientMetrics<?, ?, ?, ?, ?> createHttpClientMetrics(@NotNull HttpClientOptions httpClientOptions) {
    return options.isEnabled(HTTPClient)
        ? new HTTPClientPrometheusMetrics(options.getRegistry(), getLocalAddress(httpClientOptions.getLocalAddress()), gauges, counters, histograms)
        : super.createHttpClientMetrics(httpClientOptions);
  }

  @Override
  public @NotNull DatagramSocketMetrics createDatagramSocketMetrics(@NotNull DatagramSocketOptions datagramSocketOptions) {
    return options.isEnabled(DatagramSocket)
        ? new DatagramSocketPrometheusMetrics(options.getRegistry(), counters)
        : super.createDatagramSocketMetrics(datagramSocketOptions);
  }

  @Override
  public @NotNull HttpServerMetrics<?, ?, ?> createHttpServerMetrics(@NotNull HttpServerOptions httpServerOptions, @NotNull SocketAddress localAddress) {
    return options.isEnabled(HTTPServer)
        ? new HTTPServerPrometheusMetrics(options.getRegistry(), localAddress, gauges, counters, histograms)
        : super.createHttpServerMetrics(httpServerOptions, localAddress);
  }

  @Override
  public @NotNull PoolMetrics<?> createPoolMetrics(@NotNull String poolType, @NotNull String poolName, int maxPoolSize) {
    return options.isEnabled(Pools)
        ? new PoolPrometheusMetrics(options.getRegistry(), poolType, poolName, maxPoolSize)
        : super.createPoolMetrics(poolType, poolName, maxPoolSize);
  }

  @Override
  public @NotNull TCPMetrics<?> createNetClientMetrics(@NotNull NetClientOptions netClientOptions) {
    return options.isEnabled(HTTPClient)
        ? new NetClientPrometheusMetrics(options.getRegistry(), getLocalAddress(netClientOptions.getLocalAddress()), gauges, counters)
        : super.createNetClientMetrics(netClientOptions);
  }

  @Override
  public @NotNull TCPMetrics<?> createNetServerMetrics(@NotNull NetServerOptions netServerOptions, @NotNull SocketAddress localAddress) {
    return options.isEnabled(NetServer)
        ? new NetServerPrometheusMetrics(options.getRegistry(), localAddress, gauges, counters)
        : super.createNetServerMetrics(netServerOptions, localAddress);
  }

  @Override
  public boolean isMetricsEnabled() {
    return true;
  }

  @Override
  public void close() {
    if (server != null) {
      server.close(event -> { /* do nothing */ });
    }

    gauges.close();
    counters.close();
    histograms.close();
  }

  private static @NotNull String getLocalAddress(@Nullable String address) {
    return address == null ? "unknown" : address;
  }

  private interface VerticleMetrics {
    void deployed(@NotNull Verticle verticle);

    void undeployed(@NotNull Verticle verticle);
  }

  private interface TimerMetrics {
    void created(long id);

    void ended(long id, boolean cancelled);
  }

  private static final class VerticlePrometheusMetrics extends PrometheusMetrics implements VerticleMetrics {
    private static final @NotNull Gauge collector =
        Gauge.build("vertx_verticle_number", "Deployed verticles number").labelNames("class").create();

    public VerticlePrometheusMetrics(@NotNull CollectorRegistry registry) {
      super(registry);
      register(collector);
    }

    @Override
    public void deployed(@NotNull Verticle verticle) {
      collector.labels(verticle.getClass().getName()).inc();
    }

    @Override
    public void undeployed(@NotNull Verticle verticle) {
      collector.labels(verticle.getClass().getName()).dec();
    }
  }

  private static final class TimerPrometheusMetrics extends PrometheusMetrics implements TimerMetrics {
    private static final @NotNull Gauge collector =
        Gauge.build("vertx_timers_number", "Timers number").labelNames("state").create();

    public TimerPrometheusMetrics(@NotNull CollectorRegistry registry) {
      super(registry);
      register(collector);
    }

    @Override
    public void created(long id) {
      collector.labels("created").inc();
      collector.labels("active").inc();
    }

    @Override
    public void ended(long id, boolean cancelled) {
      if (cancelled) {
        collector.labels("cancelled").inc();
      }
      collector.labels("destroyed").inc();
      collector.labels("active").dec();
    }
  }

  private final class VerticleDummyMetrics implements @NotNull VerticleMetrics {

    @Override
    public void deployed(@NotNull Verticle verticle) {
      VertxPrometheusMetrics.super.verticleDeployed(verticle);
    }

    @Override
    public void undeployed(@NotNull Verticle verticle) {
      VertxPrometheusMetrics.super.verticleUndeployed(verticle);
    }
  }

  private final class TimerDummyMetrics implements @NotNull TimerMetrics {

    @Override
    public void created(long id) {
      VertxPrometheusMetrics.super.timerCreated(id);
    }

    @Override
    public void ended(long id, boolean cancelled) {
      VertxPrometheusMetrics.super.timerEnded(id, cancelled);
    }
  }
}
