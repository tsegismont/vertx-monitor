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

import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.TCPMetrics;
import org.hawkular.metrics.client.common.MetricType;
import org.hawkular.metrics.client.common.SingleMetric;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Thomas Segismont
 */
public class NetServerMetricsImpl implements TCPMetrics<Void>, MetricSupplier {
  // Connection info
  private final AtomicLong connections = new AtomicLong(0);
  // Bytes info
  private final LongAdder bytesReceived = new LongAdder();
  private final LongAdder bytesSent = new LongAdder();
  // Other
  private final LongAdder errorCount = new LongAdder();

  private final String baseName;
  private final Scheduler scheduler;

  public NetServerMetricsImpl(String prefix, SocketAddress localAddress, Scheduler scheduler) {
    String serverId = localAddress.host() + ":" + localAddress.port();
    baseName = prefix + (prefix.isEmpty() ? "" : ".") + "vertx.net.server." + serverId;
    this.scheduler = scheduler;
    scheduler.register(this);
  }

  @Override
  public Void connected(SocketAddress remoteAddress) {
    connections.incrementAndGet();
    return null;
  }

  @Override
  public void disconnected(Void socketMetric, SocketAddress remoteAddress) {
    connections.decrementAndGet();
  }

  @Override
  public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    bytesReceived.add(numberOfBytes);
  }

  @Override
  public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    bytesSent.add(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) {
    errorCount.increment();
  }

  @Override
  public List<SingleMetric> collect() {
    long timestamp = System.currentTimeMillis();
    return Arrays.asList(buildMetric("connections", timestamp, connections.get(), MetricType.GAUGE),
      buildMetric("bytesReceived", timestamp, bytesReceived.sum(), MetricType.COUNTER),
      buildMetric("bytesSent", timestamp, bytesSent.sum(), MetricType.COUNTER),
      buildMetric("errorCount", timestamp, errorCount.sum(), MetricType.COUNTER));
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
