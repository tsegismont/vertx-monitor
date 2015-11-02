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

package io.vertx.ext.hawkular.impl

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import io.vertx.groovy.ext.unit.TestContext
import org.junit.Before
import org.junit.Test

import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool

import static org.junit.Assert.assertEquals

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
  def requestDelay = 11L

  @Before
  void setup(TestContext context) {
    def verticleName = 'verticles/http_server.groovy'
    def instances = 4
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
    waitServerReply()

    def metrics = hawkularMetrics.get(path: 'metrics', headers: [(TENANT_HEADER_NAME): tenantId]).data ?: []
    metrics = metrics.findAll { metric ->
      String id = metric.id
      id.startsWith(metricPrefix)
    }
    assertEquals(HTTP_SERVER_METRICS.size(), metrics.size())

    metrics = metrics.collect { metric ->
      String id = metric.id
      id.substring(metricPrefix.length())
    }

    assertEquals(HTTP_SERVER_METRICS as Set, metrics as Set)
  }


  @Test
  void testHttpServerMetricsValues() {
    def bodyContent = 'pitchoune'
    def sentCount = 68
    (1..sentCount).collect { i ->
      ForkJoinPool.commonPool().submit({
        def httpClient = new RESTClient("http://${testHost}:${testPort}", ContentType.TEXT)
        def status = 0I
        httpClient.post([
          body: bodyContent
        ], { res ->
          status = res.status
        })
        status
      } as Callable<Integer>)
    }.each { task ->
      assertEquals(200, task.join())
    }

    waitServerReply()
    waitServerReply()

    assertGaugeEquals(sentCount * bodyContent.bytes.length, tenantId, "${metricPrefix}bytesReceived")
    assertGaugeEquals(sentCount * RESPONSE_CONTENT.bytes.length, tenantId, "${metricPrefix}bytesSent")
    assertGaugeGreaterThan(sentCount * requestDelay, tenantId, "${metricPrefix}processingTime")
    assertGaugeEquals(sentCount, tenantId, "${metricPrefix}requestCount")
  }
}
