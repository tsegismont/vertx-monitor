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

package io.vertx.ext.hawkular

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import io.vertx.ext.unit.junit.Timeout
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.unit.TestContext
import io.vertx.groovy.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.runner.RunWith

import static java.util.concurrent.TimeUnit.MINUTES

@RunWith(VertxUnitRunner.class)
abstract class BaseITest {
  public static final PORT_OFFSET = Integer.getInteger('test.port.offset', 0);
  public static final SERVER_URL = System.getProperty('test.hawkular.server.base-url') ?:
    'http://127.0.0.1:8080/hawkular/metrics/'
  public static final SERVER_URL_PROPS = new URI(SERVER_URL)
  public static final TENANT_HEADER_NAME = "Hawkular-Tenant"
  public static final METRIC_PREFIX = 'mars01.host13'
  public static final SCHEDULE = 1I
  public static final DELTA = 0.001D

  protected static RESTClient hawkularMetrics

  @Rule
  public Timeout timeout = new Timeout(1, MINUTES);

  protected def tenantId = TenantGenerator.instance.nextTenantId()
  protected def vertxOptions = createMetricsOptions(tenantId)
  protected def vertx = Vertx.vertx(vertxOptions);

  @BeforeClass
  static void setup() {
    hawkularMetrics = new RESTClient(SERVER_URL, ContentType.JSON)
  }

  @After
  void tearDown(TestContext context) {
    def async = context.async()
    vertx.close({ res ->
      if (res.succeeded()) {
        async.complete()
      } else {
        context.fail(res.cause())
      }
    })
  }

  static def Map createMetricsOptions(String tenantId) {
    def vertxOptions = [
      metricsOptions: [
        enabled    : true,
        httpOptions: [defaultHost: SERVER_URL_PROPS.host, defaultPort: SERVER_URL_PROPS.port],
        tenant     : tenantId,
        prefix     : METRIC_PREFIX,
        schedule   : SCHEDULE,
      ]
    ]
    vertxOptions
  }

  protected static def int getPort(int defaultValue) {
    defaultValue + PORT_OFFSET
  }

  protected static def void assertGaugeEquals(Double expected, String tenantId, String gauge) {
    while (true) {
      double actual = getGaugeValue(tenantId, gauge)
      if (Double.compare(expected, actual) == 0 || Math.abs(expected - actual) <= DELTA) {
        return
      }
      sleep(1000) // Maybe an old value? Give more time for metric to be collected
    }
  }

  protected def static double getGaugeValue(String tenantId, String gauge) {
    while (true) {
      def data = hawkularMetrics.get([
        path   : "gauges/${gauge}/data",
        headers: [(TENANT_HEADER_NAME): tenantId]
      ]).data ?: []
      if (!data.isEmpty()) return data[data.size() - 1].value as Double
      sleep(1000) // Give more time for metric to be collected
    }
  }

  protected static def void assertGaugeGreaterThan(Double expected, String tenantId, String gauge) {
    while (true) {
      double actual = getGaugeValue(tenantId, gauge)
      if (Double.compare(actual, expected) >= 0) return
      sleep(1000) // Maybe an old value? Give more time for metric to be collected
    }
  }
}
