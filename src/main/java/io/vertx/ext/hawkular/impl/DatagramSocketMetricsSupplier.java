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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

import static org.hawkular.metrics.client.common.MetricType.*;

/**
 * Aggregates values from {@link DatagramSocketMetricsImpl} instances and exposes metrics for collection.
 *
 * @author Thomas Segismont
 */
public class DatagramSocketMetricsSupplier implements MetricSupplier {
  private final String baseName;
  private final ConcurrentMap<SocketAddress, Values> metrics;
  private final LongAdder errors = new LongAdder();

  public DatagramSocketMetricsSupplier(String prefix) {
    baseName = prefix + (prefix.isEmpty() ? "" : ".") + "vertx.datagram.";
    metrics = new ConcurrentHashMap<>(0);
  }

  @Override
  public List<SingleMetric> collect() {
    long timestamp = System.currentTimeMillis();
    List<SingleMetric> res = new ArrayList<>(1 + 2 * metrics.size());
    res.add(new SingleMetric(baseName + "errorCount", timestamp, Long.valueOf(errors.sum()).doubleValue(), COUNTER));
    metrics.entrySet().forEach(e -> {
      SocketAddress address = e.getKey();
      Values values = e.getValue();
      res.addAll(values.toMetricList(timestamp, address));
    });
    return res;
  }

  public void incrementBytesReceived(SocketAddress address, long bytes) {
    getValuesForAddress(address).received.add(bytes);
  }

  public void incrementBytesSent(SocketAddress address, long bytes) {
    getValuesForAddress(address).sent.add(bytes);
  }


  public void incrementErrorCount() {
    errors.increment();
  }

  private Values getValuesForAddress(SocketAddress address) {
    return metrics.computeIfAbsent(address, a -> new Values(baseName));
  }

  private static class Values {
    final String baseName;
    final LongAdder received = new LongAdder();
    final LongAdder sent = new LongAdder();

    Values(String baseName) {
      this.baseName = baseName;
    }

    List<SingleMetric> toMetricList(long timestamp, SocketAddress address) {
      String addressId = address.host() + ":" + address.port();
      return Arrays.asList(metric(addressId + ".bytesReceived", timestamp, received.sum(), COUNTER),
        metric(addressId + ".bytesSent", timestamp, sent.sum(), COUNTER));
    }

    SingleMetric metric(String name, long timestamp, Number value, MetricType type) {
      return new SingleMetric(baseName + name, timestamp, value.doubleValue(), type);
    }
  }
}
