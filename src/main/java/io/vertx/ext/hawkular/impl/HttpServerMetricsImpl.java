/*
 * Copyright 2015 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.hawkular.impl;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import org.hawkular.metrics.client.common.MetricType;
import org.hawkular.metrics.client.common.SingleMetric;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author Thomas Segismont
 */
public class HttpServerMetricsImpl implements HttpServerMetrics<Long, Void, Void>, MetricSupplier {
  // Request info
  private final AtomicLong processingTime = new AtomicLong(0);
  private final AtomicLong requestCount = new AtomicLong(0);
  private final AtomicLong requests = new AtomicLong(0);
  // HTTP Connection info
  private final AtomicLong httpConnections = new AtomicLong(0);
  // Websocket Connection info
  private final AtomicLong wsConnections = new AtomicLong(0);
  // Bytes info
  private final AtomicLong bytesReceived = new AtomicLong(0);
  private final AtomicLong bytesSent = new AtomicLong(0);
  // Other
  private final AtomicLong errorCount = new AtomicLong(0);

  private final String baseName;
  private final Scheduler scheduler;

  public HttpServerMetricsImpl(String prefix, SocketAddress localAddress, Scheduler scheduler) {
    String serverId = localAddress.host() + ":" + localAddress.port();
    baseName = prefix + (prefix.isEmpty() ? "" : ".") + "vertx.http.server." + serverId;
    this.scheduler = scheduler;
    scheduler.register(this);
  }

  @Override
  public Long requestBegin(Void socketMetric, HttpServerRequest request) {
    requests.incrementAndGet();
    return System.nanoTime();
  }

  @Override
  public void responseEnd(Long nanoStart, HttpServerResponse response) {
    long requestProcessingTime = System.nanoTime() - nanoStart;
    requestCount.incrementAndGet();
    processingTime.addAndGet(requestProcessingTime);
    requests.decrementAndGet();
  }

  @Override
  public Void upgrade(Long requestMetric, ServerWebSocket serverWebSocket) {
    return null;
  }

  @Override
  public Void connected(Void socketMetric, ServerWebSocket serverWebSocket) {
    wsConnections.incrementAndGet();
    return null;
  }

  @Override
  public void disconnected(Void serverWebSocketMetric) {
    wsConnections.decrementAndGet();
  }

  @Override
  public Void connected(SocketAddress remoteAddress) {
    httpConnections.incrementAndGet();
    return null;
  }

  @Override
  public void disconnected(Void socketMetric, SocketAddress remoteAddress) {
    httpConnections.decrementAndGet();
  }

  @Override
  public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    bytesReceived.addAndGet(numberOfBytes);
  }

  @Override
  public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    bytesSent.addAndGet(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) {
    errorCount.incrementAndGet();
  }

  @Override
  public List<SingleMetric> collect() {
    long timestamp = System.currentTimeMillis();
    long processingTimeMillis = MILLISECONDS.convert(processingTime.get(), NANOSECONDS);
    return Arrays.asList(
      buildMetric("processingTime", timestamp, processingTimeMillis, MetricType.COUNTER),
      buildMetric("requestCount", timestamp, requestCount.get(), MetricType.COUNTER),
      buildMetric("requests", timestamp, requests.get(), MetricType.GAUGE),
      buildMetric("httpConnections", timestamp, httpConnections.get(), MetricType.GAUGE),
      buildMetric("wsConnections", timestamp, wsConnections.get(), MetricType.GAUGE),
      buildMetric("bytesReceived", timestamp, bytesReceived.get(), MetricType.COUNTER),
      buildMetric("bytesSent", timestamp, bytesSent.get(), MetricType.COUNTER),
      buildMetric("errorCount", timestamp, errorCount.get(), MetricType.COUNTER));
  }

  private SingleMetric buildMetric(String name, long timestamp, Number value, MetricType type) {
    return new SingleMetric(baseName + "." + name, timestamp, value.doubleValue(), type);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {
    scheduler.unregister(this);
  }
}
