# vertx-prometheus-metrics

[![Build Status](https://img.shields.io/travis/nolequen/vertx-prometheus-metrics.svg?branch=master&style=flat-square)](https://travis-ci.org/nolequen/vertx-prometheus-metrics)
[![Maven Central](https://img.shields.io/maven-central/v/su.nlq/vertx-prometheus-metrics.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/su.nlq/vertx-prometheus-metrics)
[![GitHub release](https://img.shields.io/github/release/nolequen/vertx-prometheus-metrics.svg?style=flat-square)](https://github.com/nolequen/vertx-prometheus-metrics/releases/latest)
[![Dependency Status](https://www.versioneye.com/user/projects/596d0ea90fb24f00558fe198/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/596d0ea90fb24f00558fe198)
[![Codacy Badge](https://img.shields.io/codacy/03b7a792c7e44d41a19596665ba12d27.svg?style=flat-square)](https://www.codacy.com/app/nolequen/vertx-prometheus-metrics?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=nolequen/vertx-prometheus-metrics&amp;utm_campaign=Badge_Grade)
[![License](http://img.shields.io/:license-apache-brightgreen.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

[Prometheus](https://prometheus.io/) implementation of the [Vert.x Metrics SPI](http://vertx.io/docs/vertx-core/java/index.html#_metrics_spi).

## Usage

You can find latest release on Maven Central.

* Maven:
```xml
<dependency>
  <groupId>su.nlq</groupId>
  <artifactId>vertx-prometheus-metrics</artifactId>
  <version>0.13</version>
</dependency>
```

* Gradle:
```groovy
compile group: 'su.nlq', name: 'vertx-prometheus-metrics', version: '0.13'
```

Now you can enable vertx metrics:
```java
final Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
    new VertxPrometheusOptions().setEnabled(true)
));
```

## Options

There some specific options you can use:

* Enable or disable embedded Prometheus server to expose metrics for Prometheus consumption via HTTP or check its state (enabled by default)
* Specify host and port of the embedded server (`localhost:9090` by default)
* Enable or disable specific `MetricsType` or check their state (all metrics are enabled by default)
* Specify which Prometheus `CollectorRegistry` (default one is used unless otherwise specified)

## Metrics

to be described...
