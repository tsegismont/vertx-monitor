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
import io.vertx.groovy.ext.unit.TestContext
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

/**
 * @author Thomas Segismont
 */
class HttpServerITest extends BaseITest {
  static final RESPONSE_CONTENT = 'some text'
  static final HTTP_SERVER_METRICS = ['bytesReceived', 'bytesSent', 'errorCount', 'httpConnections', 'processingTime',
                                      'requestCount', 'requests', 'wsConnections']

  def testHost = '127.0.0.1'
  def testPort = getPort(9191)
  def metricPrefix = "${METRIC_PREFIX}.vertx.http.server.${testHost}:${testPort}."
  def requestDelay = 120L

  @Before
  void setup(TestContext context) {
    def verticleName = 'verticles/http_server.groovy'
    def instances = 1
    def config = [
      'host'        : testHost,
      'port'        : testPort,
      'requestDelay': requestDelay,
      'content'     : RESPONSE_CONTENT
    ]
    deployVerticle(verticleName, config, instances, context)
  }

  @Test
  void shouldReportHttpServerMetrics() {
    def metrics
    while (true) {
      metrics = hawkularMetrics.get(path: 'metrics', headers: [(TENANT_HEADER_NAME): tenantId]).data ?: []
      metrics = metrics.findAll { metric ->
        String id = metric.id
        id.startsWith(metricPrefix)
      }
      if (!metrics.isEmpty() && HTTP_SERVER_METRICS.size() == metrics.size()) break;
      sleep(1000) // Give some time for all metrics to be collected and sent
    }
    metrics = metrics.collect { metric ->
      String id = metric.id
      id.substring(metricPrefix.length())
    }

    assertTrue(HTTP_SERVER_METRICS.containsAll(metrics))
  }

  @Test
  void testHttpServerMetricsValues(TestContext context) {
    def bodyContent = 'pitchoune'
    def httpClient = new RESTClient("http://${testHost}:${testPort}", ContentType.TEXT)
    httpClient.post([
      body: bodyContent
    ], { res ->
      assertEquals(200, res.status)
    })

    assertGaugeEquals(bodyContent.bytes.length, tenantId, "${metricPrefix}bytesReceived")
    assertGaugeEquals(RESPONSE_CONTENT.bytes.length, tenantId, "${metricPrefix}bytesSent")
    assertGaugeGreaterThan(requestDelay, tenantId, "${metricPrefix}processingTime")
    assertGaugeEquals(1, tenantId, "${metricPrefix}requestCount")
  }
}
