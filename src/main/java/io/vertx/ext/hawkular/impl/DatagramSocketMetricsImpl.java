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
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import org.hawkular.metrics.client.common.MetricType;
import org.hawkular.metrics.client.common.SingleMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * @author Thomas Segismont
 */
public class DatagramSocketMetricsImpl implements DatagramSocketMetrics, Supplier<List<SingleMetric>> {
  // Bytes info
  private final AtomicLong bytesReceived = new AtomicLong(0);
  private final AtomicLong bytesSent = new AtomicLong(0);
  // Other
  private final AtomicLong errorCount = new AtomicLong(0);

  private final String baseName;
  private final Scheduler scheduler;

  private volatile String serverId;

  public DatagramSocketMetricsImpl(String prefix, Scheduler scheduler) {
    baseName = prefix + (prefix.isEmpty() ? "" : ".") + "vertx.datagram";
    this.scheduler = scheduler;
    scheduler.register(this);
  }

  @Override
  public void listening(SocketAddress localAddress) {
    serverId = localAddress.host() + ":" + localAddress.port();
    bytesReceived.set(0);
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
  public List<SingleMetric> get() {
    long timestamp = System.currentTimeMillis();
    List<SingleMetric> metricList = new ArrayList<>(3);
    metricList.add(buildMetric("bytesSent", timestamp, bytesSent.get(), MetricType.COUNTER));
    metricList.add(buildMetric("errorCount", timestamp, errorCount.get(), MetricType.COUNTER));
    String sid = serverId;
    if (sid != null) {
      metricList.add(buildMetric(sid + ".bytesReceived", timestamp, bytesReceived.get(), MetricType.COUNTER));
    }
    return metricList;
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
