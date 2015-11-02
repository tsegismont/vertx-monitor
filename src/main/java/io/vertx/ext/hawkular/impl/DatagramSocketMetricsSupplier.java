/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.ext.hawkular.impl;

import io.vertx.core.net.SocketAddress;
import org.hawkular.metrics.client.common.MetricType;
import org.hawkular.metrics.client.common.SingleMetric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.hawkular.metrics.client.common.MetricType.*;

/**
 * Aggregates values from {@link DatagramSocketMetricsImpl} instances and exposes metrics for collection.
 *
 * @author Thomas Segismont
 */
public class DatagramSocketMetricsSupplier implements MetricSupplier {
  private final String baseName;
  private final Set<DatagramSocketMetricsImpl> metricsSet = new CopyOnWriteArraySet<>();

  public DatagramSocketMetricsSupplier(String prefix) {
    baseName = prefix + (prefix.isEmpty() ? "" : ".") + "vertx.datagram.";
  }

  @Override
  public List<SingleMetric> collect() {
    long timestamp = System.currentTimeMillis();
    Map<SocketAddress, Long> received = new HashMap<>();
    Map<SocketAddress, Long> sent = new HashMap<>();
    long errorCount = 0;
    for (DatagramSocketMetricsImpl datagramSocketMetrics : metricsSet) {
      SocketAddress serverAddress = datagramSocketMetrics.getServerAddress();
      if (serverAddress != null) {
        received.merge(serverAddress, datagramSocketMetrics.getBytesReceived(), Long::sum);
      }
      datagramSocketMetrics.getBytesSent().forEach((address, bytes) -> sent.merge(address, bytes, Long::sum));
      errorCount += datagramSocketMetrics.getErrorCount();
    }
    List<SingleMetric> res = new ArrayList<>(received.size() + sent.size() + 1);
    received.forEach((address, count) -> {
      String addressId = address.host() + ":" + address.port();
      res.add(metric(addressId + ".bytesReceived", timestamp, count, COUNTER));
    });
    sent.forEach((address, count) -> {
      String addressId = address.host() + ":" + address.port();
      res.add(metric(addressId + ".bytesSent", timestamp, count, COUNTER));
    });
    res.add(new SingleMetric(baseName + "errorCount", timestamp, Long.valueOf(errorCount).doubleValue(), COUNTER));
    return res;
  }

  private SingleMetric metric(String name, long timestamp, Number value, MetricType type) {
    return new SingleMetric(baseName + name, timestamp, value.doubleValue(), type);
  }

  public void register(DatagramSocketMetricsImpl datagramSocketMetrics) {
    metricsSet.add(datagramSocketMetrics);
  }

  public void unregister(DatagramSocketMetricsImpl datagramSocketMetrics) {
    metricsSet.remove(datagramSocketMetrics);
  }
}
