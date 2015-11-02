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
 * Aggregates values from {@link HttpServerMetricsImpl} instances and exposes metrics for collection.
 *
 * @author Thomas Segismont
 */
public class HttpServerMetricsSupplier implements MetricSupplier {
  private final String baseName;
  private final Set<HttpServerMetricsImpl> metricsSet = new CopyOnWriteArraySet<>();

  public HttpServerMetricsSupplier(String prefix) {
    baseName = prefix + (prefix.isEmpty() ? "" : ".") + "vertx.http.server.";
  }

  @Override
  public List<SingleMetric> collect() {
    long timestamp = System.currentTimeMillis();

    Map<SocketAddress, Long> processingTime = new HashMap<>();
    Map<SocketAddress, Long> requestCount = new HashMap<>();
    Map<SocketAddress, Long> requests = new HashMap<>();
    Map<SocketAddress, Long> httpConnections = new HashMap<>();
    Map<SocketAddress, Long> wsConnections = new HashMap<>();
    Map<SocketAddress, Long> bytesReceived = new HashMap<>();
    Map<SocketAddress, Long> bytesSent = new HashMap<>();
    Map<SocketAddress, Long> errorCount = new HashMap<>();

    for (HttpServerMetricsImpl httpServerMetrics : metricsSet) {
      SocketAddress serverAddress = httpServerMetrics.getServerAddress();
      merge(processingTime, serverAddress, httpServerMetrics.getProcessingTime());
      merge(requestCount, serverAddress, httpServerMetrics.getRequestCount());
      merge(requests, serverAddress, httpServerMetrics.getRequests());
      merge(httpConnections, serverAddress, httpServerMetrics.getHttpConnections());
      merge(wsConnections, serverAddress, httpServerMetrics.getWsConnections());
      merge(bytesReceived, serverAddress, httpServerMetrics.getBytesReceived());
      merge(bytesSent, serverAddress, httpServerMetrics.getBytesSent());
      merge(errorCount, serverAddress, httpServerMetrics.getErrorCount());
    }

    List<SingleMetric> res = new ArrayList<>();
    res.addAll(metrics("processingTime", timestamp, processingTime, COUNTER));
    res.addAll(metrics("requestCount", timestamp, requestCount, COUNTER));
    res.addAll(metrics("requests", timestamp, requests, GAUGE));
    res.addAll(metrics("httpConnections", timestamp, httpConnections, GAUGE));
    res.addAll(metrics("wsConnections", timestamp, wsConnections, GAUGE));
    res.addAll(metrics("bytesReceived", timestamp, bytesReceived, COUNTER));
    res.addAll(metrics("bytesSent", timestamp, bytesSent, COUNTER));
    res.addAll(metrics("errorCount", timestamp, errorCount, COUNTER));
    return res;
  }

  private void merge(Map<SocketAddress, Long> processingTime, SocketAddress serverAddress, Long value) {
    processingTime.merge(serverAddress, value, Long::sum);
  }

  private List<SingleMetric> metrics(String name, long timestamp, Map<SocketAddress, ? extends Number> values, MetricType type) {
    List<SingleMetric> res = new ArrayList<>(values.size());
    values.forEach((address, count) -> {
      String addressId = address.host() + ":" + address.port();
      res.add(metric(addressId + "." + name, timestamp, count, type));
    });
    return res;
  }

  private SingleMetric metric(String name, long timestamp, Number value, MetricType type) {
    return new SingleMetric(baseName + name, timestamp, value.doubleValue(), type);
  }

  public void register(HttpServerMetricsImpl httpServerMetrics) {
    metricsSet.add(httpServerMetrics);
  }

  public void unregister(HttpServerMetricsImpl httpServerMetrics) {
    metricsSet.remove(httpServerMetrics);
  }
}
