package io.vertx.ext.hawkular.impl;

import org.hawkular.metrics.client.common.SingleMetric;

import java.util.List;

/**
 * Contract for objects supplying metrics.
 *
 * @author Thomas Segismont
 */
public interface MetricSupplier {
  /**
   * @return a list of metrics to send to the Hawkular server
   */
  List<SingleMetric> collect();
}
