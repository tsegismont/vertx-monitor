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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import org.hawkular.metrics.client.common.Batcher;
import org.hawkular.metrics.client.common.SingleMetric;
import org.hawkular.vertx.monitor.VertxMonitorOptions;

/**
 * @author Thomas Segismont
 */
public class Sender {
    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);

    private final Vertx vertx;
    private final VertxMonitorOptions vertxMonitorOptions;
    private final BlockingQueue<SingleMetric> metricQueue;
    private final String metricsURI;
    private final ExecutorService executorService;
    private final int batchSize;

    private HttpClient httpClient;

    public Sender(Vertx vertx, VertxMonitorOptions vertxMonitorOptions, BlockingQueue<SingleMetric> metricQueue) {
        this.vertx = vertx;
        this.vertxMonitorOptions = vertxMonitorOptions;
        this.metricQueue = metricQueue;
        metricsURI = "/hawkular-metrics/" + vertxMonitorOptions.getTenant() + "/metrics/numeric/data";
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName("vertx-monitor-sender");
            return thread;
        });
        batchSize = vertxMonitorOptions.getBatchSize();
    }

    public void init() {
        HttpClientOptions httpClientOptions = new HttpClientOptions().setDefaultHost(vertxMonitorOptions.getHost())
            .setDefaultPort(vertxMonitorOptions.getPort()).setKeepAlive(true).setTryUseCompression(true);
        httpClient = vertx.createHttpClient(httpClientOptions);
        executorService.submit(this::send);
    }

    private void send() {
        while (!executorService.isShutdown()) {
            List<SingleMetric> batch = getNextBatch();

            String json = Batcher.metricListToJson(batch);
            Buffer buffer = Buffer.buffer(json);
            HttpClientRequest req = httpClient.post(
                metricsURI,
                response -> {
                    if (response.statusCode() != 200 && LOG.isTraceEnabled()) {
                        response.bodyHandler(msg -> LOG.trace("Could not send metrics: " + response.statusCode()
                            + " : " + msg.toString()));
                    }
                });
            req.putHeader("Content-Length", String.valueOf(buffer.length()));
            req.putHeader("Content-Type", "application/json");
            req.exceptionHandler(err -> LOG.trace("Could not send metrics", err));
            req.write(buffer);
            req.end();
        }
    }

    private List<SingleMetric> getNextBatch() {
        List<SingleMetric> list = new ArrayList<>(batchSize);
        metricQueue.drainTo(list, batchSize);
        if (list.size() < batchSize) {
            try {
                SingleMetric next = metricQueue.poll(1, SECONDS);
                if (next != null) {
                    metricQueue.add(next);
                }
                if (list.size() < batchSize) {
                    metricQueue.drainTo(list, batchSize - list.size());
                }
            } catch (InterruptedException ignored) {
            }
        }
        return list;
    }

    public void stop() {
        executorService.shutdownNow();
    }
}
