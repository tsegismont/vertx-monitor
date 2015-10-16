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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.ext.hawkular.VertxHawkularOptions;
import org.hawkular.metrics.client.common.SingleMetric;

import java.util.Collections;
import java.util.List;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author Thomas Segismont
 */
public abstract class ScheduledMetrics implements Metrics {
  private final Vertx vertx;
  private final Handler<List<SingleMetric>> metricHandler;
  private final long timerId;

  public ScheduledMetrics(Vertx vertx, VertxHawkularOptions vertxHawkularOptions,
                          Handler<List<SingleMetric>> metricHandler) {
    this.vertx = vertx;
    this.metricHandler = metricHandler;
    long schedule = MILLISECONDS.convert(vertxHawkularOptions.getSchedule(), SECONDS);
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
