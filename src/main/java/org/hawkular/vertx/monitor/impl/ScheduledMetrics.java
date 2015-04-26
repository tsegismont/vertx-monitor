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
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collections;
import java.util.List;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.spi.metrics.Metrics;

import org.hawkular.metrics.client.common.SingleMetric;
import org.hawkular.vertx.monitor.VertxMonitorOptions;

/**
 * @author Thomas Segismont
 */
public abstract class ScheduledMetrics implements Metrics {
    private final Vertx vertx;
    private final Handler<List<SingleMetric>> metricHandler;
    private final long timerId;

    public ScheduledMetrics(Vertx vertx, VertxMonitorOptions vertxMonitorOptions,
        Handler<List<SingleMetric>> metricHandler) {
        this.vertx = vertx;
        this.metricHandler = metricHandler;
        long schedule = MILLISECONDS.convert(vertxMonitorOptions.getSchedule(), SECONDS);
        timerId = vertx.setPeriodic(schedule, this::collectAndQueue);
    }

    private void collectAndQueue(Long timerId) {
        List<SingleMetric> metrics = collect();
        if (metrics != null && !metrics.isEmpty()) {
            metricHandler.handle(metrics);
        }
    }

    protected List<SingleMetric> collect() {
        return Collections.emptyList();
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
