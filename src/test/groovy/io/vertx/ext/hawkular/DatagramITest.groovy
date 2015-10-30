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

import io.vertx.groovy.core.datagram.DatagramSocket
import io.vertx.groovy.ext.unit.TestContext
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * @author Thomas Segismont
 */
class DatagramITest extends BaseITest {
  static final CONTENT = 'some text'
  static final DATAGRAM_METRICS = ['bytesReceived', 'bytesSent', 'errorCount']

  def testHost = '127.0.0.1'
  def testPort = getPort(9192)
  def baseName = "${METRIC_PREFIX}.vertx.datagram."
  def baseNameWithAddress = "${baseName}${testHost}:${testPort}."

  def DatagramSocket client

  @Before
  void setup(TestContext context) {
    def verticleName = 'verticles/datagram_server.groovy'
    def instances = 13
    def config = [
      'host': testHost,
      'port': testPort
    ]
    deployVerticle(verticleName, config, instances, context)
    client = vertx.createDatagramSocket()
  }

  @Test
  void shouldReportDatagramMetrics(TestContext context) {
    def sentCount = 5
    sentCount.times { i -> client.send(CONTENT, testPort, testHost, assertAsyncSuccess(context)) }

    def metrics
    while (true) {
      metrics = hawkularMetrics.get(path: 'metrics', headers: [(TENANT_HEADER_NAME): tenantId]).data ?: []
      metrics = metrics.findAll { metric ->
        String id = metric.id
        id.startsWith(baseName)
      }
      if (!metrics.isEmpty() && DATAGRAM_METRICS.size() == metrics.size()) break;
      sleep(1000) // Give some time for all metrics to be collected and sent
    }
    metrics = metrics.collect { metric ->
      String id = metric.id
      id.startsWith(baseNameWithAddress) ? id.substring(baseNameWithAddress.length()) : id.substring(baseName.length())
    }

    assertTrue(DATAGRAM_METRICS.containsAll(metrics))

    assertGaugeEquals(sentCount * CONTENT.bytes.length, tenantId, "${baseNameWithAddress}bytesReceived")
    assertGaugeEquals(sentCount * CONTENT.bytes.length, tenantId, "${baseNameWithAddress}bytesSent")
    assertGaugeEquals(0, tenantId, "${baseName}errorCount")
  }
}