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
package org.hawkular.vertx.monitor.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import io.vertx.core.spi.metrics.EventBusMetrics;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;

import org.hawkular.vertx.monitor.VertxMonitorOptions;

/**
 * @author Thomas Segismont
 */
public class VertxMetricsImpl implements VertxMetrics {
    private final Vertx vertx;
    private final VertxMonitorOptions vertxMonitorOptions;

    List<HttpServerMetrics> httpServerMetricsList = new CopyOnWriteArrayList<>();

    public VertxMetricsImpl(Vertx vertx, VertxMonitorOptions vertxMonitorOptions) {
        this.vertx = vertx;
        this.vertxMonitorOptions = vertxMonitorOptions;
        vertx.runOnContext((Void) -> {
            vertx.setPeriodic(MILLISECONDS.convert(vertxMonitorOptions.getSchedule(), SECONDS), this::collect);
        });
    }

    private void collect(Long timerId) {
        httpServerMetricsList.forEach(m -> System.out.println(new Date() + " " + Thread.currentThread() + " " + m));
    }

    @Override
    public void verticleDeployed(Verticle verticle) {
    }

    @Override
    public void verticleUndeployed(Verticle verticle) {
    }

    @Override
    public void timerCreated(long id) {
    }

    @Override
    public void timerEnded(long id, boolean cancelled) {
    }

    @Override
    public EventBusMetrics<Void> createMetrics(EventBus eventBus) {
        return new EventBusMetricsImpl();
    }

    @Override
    public HttpServerMetrics<Long, Void, Void> createMetrics(HttpServer server, SocketAddress localAddress,
        HttpServerOptions options) {
        HttpServerMetricsImpl httpServerMetrics = new HttpServerMetricsImpl(localAddress);
        httpServerMetricsList.add(httpServerMetrics);
        return httpServerMetrics;
    }

    @Override
    public HttpClientMetrics<Void, Void, Void> createMetrics(HttpClient client, HttpClientOptions options) {
        return new HttpClientMetricsImpl();
    }

    @Override
    public TCPMetrics<Void> createMetrics(NetServer server, SocketAddress localAddress, NetServerOptions options) {
        return new NetServerMetricsImpl();
    }

    @Override
    public TCPMetrics<?> createMetrics(NetClient client, NetClientOptions options) {
        return new NetClientMetricsImpl();
    }

    @Override
    public DatagramSocketMetrics createMetrics(DatagramSocket socket, DatagramSocketOptions options) {
        return new DatagramSocketMetricsImpl();
    }

    @Override
    public boolean isMetricsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void close() {
    }
}
