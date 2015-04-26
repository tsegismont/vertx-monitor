/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.vertx.monitor.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

import org.hawkular.metrics.client.common.MetricType;
import org.hawkular.metrics.client.common.SingleMetric;
import org.hawkular.vertx.monitor.VertxMonitorOptions;

/**
 * @author Thomas Segismont
 */
public class HttpServerMetricsImpl extends ScheduledMetrics implements HttpServerMetrics<Long, Void, Void> {
    private final String baseName;

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

    public HttpServerMetricsImpl(Vertx vertx, VertxMonitorOptions vertxMonitorOptions, SocketAddress localAddress,
        Handler<List<SingleMetric>> metricHandler) {
        super(vertx, vertxMonitorOptions, metricHandler);
        String serverId = localAddress.host() + ":" + localAddress.port();
        String prefix = vertxMonitorOptions.getPrefix();
        baseName = prefix + (prefix.isEmpty() ? "" : ".") + "vertx.http.server." + serverId;
    }

    @Override
    public Long requestBegin(Void socketMetric, HttpServerRequest request) {
        requests.incrementAndGet();
        return System.nanoTime();
    }

    @Override
    public void responseEnd(Long nanoStart, HttpServerResponse response) {
        long requestProcessingTime = MILLISECONDS.convert(System.nanoTime() - nanoStart, NANOSECONDS);
        requestCount.incrementAndGet();
        processingTime.addAndGet(requestProcessingTime);
        requests.decrementAndGet();
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
    protected List<SingleMetric> collect() {
        long timestamp = System.currentTimeMillis();
        return Arrays.asList(buildMetric("processingTime", timestamp, processingTime.get(), MetricType.COUNTER),
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
}
