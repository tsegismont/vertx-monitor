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

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.hawkular.VertxMonitorOptions;
import org.hawkular.metrics.client.common.Batcher;
import org.hawkular.metrics.client.common.SingleMetric;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author Thomas Segismont
 */
public class Sender implements Handler<List<SingleMetric>> {
  private static final Logger LOG = LoggerFactory.getLogger(Sender.class);

  private final Vertx vertx;
  private final VertxMonitorOptions vertxMonitorOptions;
  private final String metricsURI;
  private final int batchSize;
  private final long batchDelay;
  private final List<SingleMetric> queue;
  private final Context context;

  private HttpClient httpClient;
  private long timerId;
  private long sendTime;

  public Sender(Vertx vertx, VertxMonitorOptions vertxMonitorOptions) {
    this.vertx = vertx;
    this.vertxMonitorOptions = vertxMonitorOptions;
    metricsURI = "/hawkular-metrics/" + vertxMonitorOptions.getTenant() + "/metrics/numeric/data";
    batchSize = vertxMonitorOptions.getBatchSize();
    batchDelay = vertxMonitorOptions.getBatchDelay();
    queue = new ArrayList<>(batchSize);
    context = vertx.getOrCreateContext();
    context.runOnContext(v -> init());
  }

  private void init() {
    HttpClientOptions httpClientOptions = new HttpClientOptions().setDefaultHost(vertxMonitorOptions.getHost())
      .setDefaultPort(vertxMonitorOptions.getPort()).setKeepAlive(true).setTryUseCompression(true);
    httpClient = vertx.createHttpClient(httpClientOptions);
    timerId = vertx.setPeriodic(MILLISECONDS.convert(batchDelay, SECONDS), this::flushIfIdle);
    sendTime = System.nanoTime();
  }

  @Override
  public void handle(List<SingleMetric> metrics) {
    context.runOnContext(v -> accept(metrics));
  }

  private void accept(List<SingleMetric> metrics) {
    if (queue.size() + metrics.size() < batchSize) {
      queue.addAll(metrics);
      return;
    }
    List<SingleMetric> temp = new ArrayList<>(queue.size() + metrics.size());
    temp.addAll(queue);
    temp.addAll(metrics);
    queue.clear();
    do {
      List<SingleMetric> subList = temp.subList(0, batchSize);
      send(subList);
      subList.clear();
    } while (temp.size() >= batchSize);
    queue.addAll(temp);
  }

  private void send(List<SingleMetric> metrics) {
    String json = Batcher.metricListToJson(metrics);
    Buffer buffer = Buffer.buffer(json);
    HttpClientRequest req = httpClient.post(
      metricsURI,
      response -> {
        if (response.statusCode() != 200 && LOG.isTraceEnabled()) {
          response.bodyHandler(msg -> LOG.trace("Could not send metrics: " + response.statusCode() + " : "
            + msg.toString()));
        }
      });
    req.putHeader("Content-Length", String.valueOf(buffer.length()));
    req.putHeader("Content-Type", "application/json");
    req.exceptionHandler(err -> LOG.trace("Could not send metrics", err));
    req.write(buffer);
    req.end();
    sendTime = System.nanoTime();
  }

  private void flushIfIdle(Long timerId) {
    if (System.nanoTime() - sendTime > NANOSECONDS.convert(batchDelay, SECONDS) && !queue.isEmpty()) {
      send(queue);
      queue.clear();
    }
  }

  public void stop() {
    vertx.cancelTimer(timerId);
    httpClient.close();
  }
}
