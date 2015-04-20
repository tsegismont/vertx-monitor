package org.hawkular.vertx.monitor.impl;

import java.util.Collections;
import java.util.List;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.metrics.Metrics;

import org.hawkular.metrics.client.common.Batcher;
import org.hawkular.metrics.client.common.SingleMetric;

/**
 * @author Thomas Segismont
 */
public abstract class ScheduledMetrics implements Metrics {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledMetrics.class);

    private final Vertx vertx;
    private final HttpClient httpClient;
    private final String metricsURI;
    private final long timerId;

    public ScheduledMetrics(Vertx vertx, HttpClient httpClient, String metricsURI) {
        this.vertx = vertx;
        this.httpClient = httpClient;
        this.metricsURI = metricsURI;
        timerId = vertx.setPeriodic(1000, this::collectAndSend);
    }

    private void collectAndSend(Long timerId) {
        List<SingleMetric> metrics = collect();
        if (metrics != null && !metrics.isEmpty()) {
            send(metrics);
        }
    }

    protected List<SingleMetric> collect() {
        return Collections.emptyList();
    }

    private void send(List<SingleMetric> metrics) {
        String json = Batcher.metricListToJson(metrics);
        Buffer buffer = Buffer.buffer(json);
        HttpClientRequest req = httpClient.post(metricsURI, response -> {
            if (response.statusCode() != 200 && LOG.isTraceEnabled()) {
                response.bodyHandler(msg -> {
                    LOG.trace("Could not send metrics: " + response.statusCode() + " : " + msg.toString());
                });
            }
        });
        req.putHeader("Content-Length", "" + buffer.length());
        req.putHeader("Content-Type", "application/json");
        req.exceptionHandler(err -> LOG.trace("Could not send metrics", err));
        req.write(buffer);
        req.end();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void close() {
        vertx.cancelTimer(timerId);
    }
}
