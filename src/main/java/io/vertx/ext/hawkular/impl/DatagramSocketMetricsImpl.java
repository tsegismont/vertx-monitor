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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import io.vertx.ext.hawkular.VertxMonitorOptions;
import org.hawkular.metrics.client.common.MetricType;
import org.hawkular.metrics.client.common.SingleMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Thomas Segismont
 */
public class DatagramSocketMetricsImpl extends ScheduledMetrics implements DatagramSocketMetrics {
  private final String baseName;

  // Bytes info
  private final AtomicLong bytesReceived = new AtomicLong(0);
  private final AtomicLong bytesSent = new AtomicLong(0);
  // Other
  private final AtomicLong errorCount = new AtomicLong(0);

  private volatile String serverId;

  public DatagramSocketMetricsImpl(Vertx vertx, VertxMonitorOptions vertxMonitorOptions,
                                   Handler<List<SingleMetric>> metricHandler) {
    super(vertx, vertxMonitorOptions, metricHandler);
    String prefix = vertxMonitorOptions.getPrefix();
    baseName = prefix + (prefix.isEmpty() ? "" : ".") + "vertx.datagram";
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
  protected List<SingleMetric> collect() {
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
}
