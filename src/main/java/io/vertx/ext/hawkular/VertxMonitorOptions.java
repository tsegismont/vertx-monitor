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
package io.vertx.ext.hawkular;

import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;

/**
 * @author Thomas Segismont
 */
public class VertxMonitorOptions extends MetricsOptions {
  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 8080;
  public static final String DEFAULT_TENANT = "default";
  public static final int DEFAULT_SCHEDULE = 1;
  public static final String DEFAULT_PREFIX = "";
  public static final int DEFAULT_BATCH_SIZE = 50;
  public static final int DEFAULT_BATCH_DELAY = 1;

  private String host;
  private int port;
  private String tenant;
  private int schedule;
  private String prefix;
  private int batchSize;
  private int batchDelay;

  public VertxMonitorOptions() {
    host = DEFAULT_HOST;
    port = DEFAULT_PORT;
    tenant = DEFAULT_TENANT;
    schedule = DEFAULT_SCHEDULE;
    prefix = DEFAULT_PREFIX;
    batchSize = DEFAULT_BATCH_SIZE;
    batchDelay = DEFAULT_BATCH_DELAY;
  }

  public VertxMonitorOptions(VertxMonitorOptions other) {
    super(other);
    host = other.host;
    port = other.port;
    tenant = other.tenant;
    schedule = other.schedule;
    prefix = other.prefix;
    batchSize = other.batchSize;
    batchDelay = other.batchDelay;
  }

  public VertxMonitorOptions(JsonObject json) {
    super(json);
    host = json.getString("host", DEFAULT_HOST);
    port = json.getInteger("port", DEFAULT_PORT);
    tenant = json.getString("tenant", DEFAULT_TENANT);
    schedule = json.getInteger("schedule", DEFAULT_SCHEDULE);
    prefix = json.getString("prefix", DEFAULT_PREFIX);
    batchSize = json.getInteger("batchSize", DEFAULT_BATCH_SIZE);
    batchDelay = json.getInteger("batchDelay", DEFAULT_BATCH_DELAY);
  }

  public String getHost() {
    return host;
  }

  public VertxMonitorOptions setHost(String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public VertxMonitorOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public String getTenant() {
    return tenant;
  }

  public VertxMonitorOptions setTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  public int getSchedule() {
    return schedule;
  }

  public VertxMonitorOptions setSchedule(int schedule) {
    this.schedule = schedule;
    return this;
  }

  public String getPrefix() {
    return prefix;
  }

  public VertxMonitorOptions setPrefix(String prefix) {
    this.prefix = prefix == null ? DEFAULT_PREFIX : prefix;
    return this;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public VertxMonitorOptions setBatchSize(int batchSize) {
    this.batchSize = batchSize;
    return this;
  }

  public int getBatchDelay() {
    return batchDelay;
  }

  public VertxMonitorOptions setBatchDelay(int batchDelay) {
    this.batchDelay = batchDelay;
    return this;
  }
}
