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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

/**
 * @author Thomas Segismont
 */
public class HttpServerMetricsImpl implements HttpServerMetrics<Long, Void, Void> {
    private final String serverId;
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

    public HttpServerMetricsImpl(SocketAddress localAddress) {
        serverId = localAddress.host() + "@" + localAddress.port();
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
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void close() {
    }

    public String getServerId() {
        return serverId;
    }

    public long getProcessingTime() {
        return processingTime.get();
    }

    public long getRequestCount() {
        return requestCount.get();
    }

    public long getRequests() {
        return requests.get();
    }

    public long getHttpConnections() {
        return httpConnections.get();
    }

    public long getWsConnections() {
        return wsConnections.get();
    }

    public long getBytesReceived() {
        return bytesReceived.get();
    }

    public long getBytesSent() {
        return bytesSent.get();
    }

    public long getErrorCount() {
        return errorCount.get();
    }

    public Map<String, Number> getRuntimeInfo() {
        Map<String, Number> info = new HashMap<>();
        info.put("processingTime", getProcessingTime());
        info.put("requestCount", getRequestCount());
        info.put("requests", getRequests());
        info.put("httpConnections", getHttpConnections());
        info.put("wsConnections", getWsConnections());
        info.put("bytesReceived", getBytesReceived());
        info.put("bytesSent", getBytesSent());
        info.put("errorCount", getErrorCount());
        return info;
    }

    @Override
    public String toString() {
        return "HttpServerMetricsImpl[" + "serverId='" + serverId + '\'' + ", processingTime=" + processingTime
            + ", requestCount=" + requestCount + ", requests=" + requests + ", httpConnections=" + httpConnections
            + ", wsConnections=" + wsConnections + ", bytesReceived=" + bytesReceived + ", bytesSent=" + bytesSent
            + ", errorCount=" + errorCount + ']';
    }
}
