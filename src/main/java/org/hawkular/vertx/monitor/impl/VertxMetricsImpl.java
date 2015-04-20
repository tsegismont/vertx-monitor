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

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.impl.DummyVertxMetrics;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.TCPMetrics;

import org.hawkular.vertx.monitor.VertxMonitorOptions;

/**
 * @author Thomas Segismont
 */
public class VertxMetricsImpl extends DummyVertxMetrics {
    private final Vertx vertx;
    private final VertxMonitorOptions vertxMonitorOptions;
    private final String host;
    private final int port;

    private HttpClient httpClient;

    public VertxMetricsImpl(Vertx vertx, VertxMonitorOptions vertxMonitorOptions) {
        this.vertx = vertx;
        this.vertxMonitorOptions = vertxMonitorOptions;
        host = vertxMonitorOptions.getHost();
        port = vertxMonitorOptions.getPort();
        vertx.runOnContext(this::init);
    }

    private void init(Void aVoid) {
        HttpClientOptions httpClientOptions = new HttpClientOptions().setDefaultHost(host).setDefaultPort(port)
            .setKeepAlive(true).setTryUseCompression(true);
        httpClient = vertx.createHttpClient(httpClientOptions);
    }

    @Override
    public HttpServerMetrics<Long, Void, Void> createMetrics(HttpServer server, SocketAddress localAddress,
        HttpServerOptions options) {
        return new HttpServerMetricsImpl(vertx, vertxMonitorOptions, localAddress, httpClient);
    }

    @Override
    public TCPMetrics createMetrics(NetServer server, SocketAddress localAddress, NetServerOptions options) {
        return new NetServerMetricsImpl(vertx, vertxMonitorOptions, localAddress, httpClient);
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
        httpClient.close();
    }
}
