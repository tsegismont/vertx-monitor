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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;

/**
 * Vert.x Hawkular monitoring configuration.
 *
 * @author Thomas Segismont
 */
@DataObject(generateConverter = true, inheritConverter = true)
public class VertxHawkularOptions extends MetricsOptions {
  /**
   * The default Hawkular server host = localhost.
   */
  public static final String DEFAULT_HOST = "localhost";

  /**
   * The default Hawkular server port = 8080.
   */
  public static final int DEFAULT_PORT = 8080;

  /**
   * The default Hawkular Metrics service URI = /hawkular/metrics.
   */
  public static final String DEFAULT_METRICS_URI = "/hawkular/metrics";

  /**
   * The default Hawkular tenant = default.
   */
  public static final String DEFAULT_TENANT = "default";

  /**
   * Default value for metric collection interval (in seconds) = 1.
   */
  public static final int DEFAULT_SCHEDULE = 1;

  /**
   * The default metric name prefix (empty).
   */
  public static final String DEFAULT_PREFIX = "";

  /**
   * Default value for the maximum number of metrics in a batch = 50.
   */
  public static final int DEFAULT_BATCH_SIZE = 50;

  /**
   * Default value for the maximum delay between two consecutive batches (in seconds) = 1.
   */
  public static final int DEFAULT_BATCH_DELAY = 1;

  private HttpClientOptions httpOptions;
  private String metricsServiceUri;
  private String tenant;
  private int schedule;
  private String prefix;
  private int batchSize;
  private int batchDelay;

  public VertxHawkularOptions() {
    httpOptions = new HttpClientOptions().setDefaultHost(DEFAULT_HOST).setDefaultPort(DEFAULT_PORT);
    metricsServiceUri = DEFAULT_METRICS_URI;
    tenant = DEFAULT_TENANT;
    schedule = DEFAULT_SCHEDULE;
    prefix = DEFAULT_PREFIX;
    batchSize = DEFAULT_BATCH_SIZE;
    batchDelay = DEFAULT_BATCH_DELAY;
  }

  public VertxHawkularOptions(VertxHawkularOptions other) {
    super(other);
    httpOptions = other.httpOptions;
    metricsServiceUri = other.metricsServiceUri;
    tenant = other.tenant;
    schedule = other.schedule;
    prefix = other.prefix;
    batchSize = other.batchSize;
    batchDelay = other.batchDelay;
  }

  public VertxHawkularOptions(JsonObject json) {
    this();
    VertxHawkularOptionsConverter.fromJson(json, this);
  }

  /**
   * @return the configuration of the Hawkular Metrics HTTP client
   */
  public HttpClientOptions getHttpOptions() {
    return httpOptions;
  }

  /**
   * Set the configuration of the Hawkular Metrics HTTP client.
   */
  public VertxHawkularOptions setHttpOptions(HttpClientOptions httpOptions) {
    this.httpOptions = httpOptions;
    return this;
  }

  /**
   * @return the Hawkular Metrics service URI
   */
  public String getMetricsServiceUri() {
    return metricsServiceUri;
  }

  /**
   * Set the Hawkular Metrics service URI.
   */
  public VertxHawkularOptions setMetricsServiceUri(String metricsServiceUri) {
    this.metricsServiceUri = metricsServiceUri;
    return this;
  }

  /**
   * @return the Hawkular tenant
   */
  public String getTenant() {
    return tenant;
  }

  /**
   * Set the Hawkular tenant.
   */
  public VertxHawkularOptions setTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  /**
   * @return the metric collection interval (in seconds)
   */
  public int getSchedule() {
    return schedule;
  }

  /**
   * Set the metric collection interval (in seconds).
   */
  public VertxHawkularOptions setSchedule(int schedule) {
    this.schedule = schedule;
    return this;
  }

  /**
   * @return the metric name prefix
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * Set the metric name prefix.
   */
  public VertxHawkularOptions setPrefix(String prefix) {
    this.prefix = prefix == null ? DEFAULT_PREFIX : prefix;
    return this;
  }

  /**
   * @return the maximum number of metrics in a batch
   */
  public int getBatchSize() {
    return batchSize;
  }

  /**
   * Set the maximum number of metrics in a batch.
   */
  public VertxHawkularOptions setBatchSize(int batchSize) {
    this.batchSize = batchSize;
    return this;
  }

  /**
   * @return the maximum delay between two consecutive batches
   */
  public int getBatchDelay() {
    return batchDelay;
  }

  /**
   * Set the maximum delay between two consecutive batches (in seconds).
   */
  public VertxHawkularOptions setBatchDelay(int batchDelay) {
    this.batchDelay = batchDelay;
    return this;
  }
}
