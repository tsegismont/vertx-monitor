package io.vertx.ext.hawkular.impl

import io.vertx.groovy.ext.unit.TestContext
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * @author Thomas Segismont
 */
class EventBusITest extends BaseITest {
  static final EVENT_BUS_METRICS = ['handlers', 'processingTime', 'errorCount', 'bytesWritten', 'bytesRead', 'pending',
                             'pendingLocal', 'pendingRemote', 'publishedMessages', 'publishedLocalMessages',
                             'publishedRemoteMessages', 'sentMessages', 'sentLocalMessages', 'sentRemoteMessages',
                             'receivedMessages', 'receivedLocalMessages', 'receivedRemoteMessages', 'deliveredMessages',
                             'deliveredLocalMessages', 'deliveredRemoteMessages', 'replyFailures']

  def String address = "testSubject"
  def baseName = "${METRIC_PREFIX}.vertx.eventbus."
  def baseNameWithAddress = "${baseName}${address}."
  def eventBus = vertx.eventBus()
  def instances = 3

  @Before
  void setup(TestContext context) {
    def verticleName = 'verticles/event_bus_handler.groovy'
    def config = [:]
    deployVerticle(verticleName, config, instances, context)
  }

  @Test
  void shouldReportEventBusMetrics() {
    def handlerSleep = 13
    def publishedNofail = 6
    publishedNofail.times { i -> eventBus.publish(address, [fail: false, sleep: handlerSleep]) }
    def publishedFail = 4
    publishedFail.times { i -> eventBus.publish(address, [fail: true, sleep: handlerSleep]) }

    waitServerReply()

    def metrics = hawkularMetrics.get(path: 'metrics', headers: [(TENANT_HEADER_NAME): tenantId]).data ?: []
    metrics = metrics.findAll { metric ->
      String id = metric.id
      id.startsWith(baseName)
    }
    metrics = metrics.collect { metric ->
      String id = metric.id
      id.startsWith(baseNameWithAddress) ? id.substring(baseNameWithAddress.length()) : id.substring(baseName.length())
    }

    assertEquals(EVENT_BUS_METRICS as Set, metrics as Set)

    def allPublished = publishedNofail + publishedFail
    assertGaugeEquals(instances, tenantId, "${baseName}handlers")
    assertGaugeGreaterThan(handlerSleep * allPublished, tenantId, "${baseNameWithAddress}processingTime")
    assertGaugeEquals(instances * publishedFail, tenantId, "${baseName}errorCount")
    assertGaugeEquals(allPublished, tenantId, "${baseName}publishedMessages")
    assertGaugeEquals(allPublished, tenantId, "${baseName}publishedLocalMessages")
    assertGaugeEquals(0, tenantId, "${baseName}publishedRemoteMessages")
    assertGaugeEquals(0, tenantId, "${baseName}sentMessages")
    assertGaugeEquals(0, tenantId, "${baseName}sentLocalMessages")
    assertGaugeEquals(0, tenantId, "${baseName}sentRemoteMessages")
    assertGaugeEquals(allPublished, tenantId, "${baseName}receivedMessages")
    assertGaugeEquals(allPublished, tenantId, "${baseName}receivedLocalMessages")
    assertGaugeEquals(0, tenantId, "${baseName}receivedRemoteMessages")
    assertGaugeEquals(allPublished, tenantId, "${baseName}deliveredMessages")
    assertGaugeEquals(allPublished, tenantId, "${baseName}deliveredLocalMessages")
    assertGaugeEquals(0, tenantId, "${baseName}deliveredRemoteMessages")
  }
}
