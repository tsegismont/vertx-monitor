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

import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;

/**
 * Implementation of {@link DatagramSocketMetrics} which relays data to {@link DatagramSocketMetricsSupplier}.
 *
 * @author Thomas Segismont
 */
public class DatagramSocketMetricsImpl implements DatagramSocketMetrics {
  private final DatagramSocketMetricsSupplier datagramSocketMetricsSupplier;

  private SocketAddress localAddress;

  public DatagramSocketMetricsImpl(DatagramSocketMetricsSupplier datagramSocketMetricsSupplier) {
    this.datagramSocketMetricsSupplier = datagramSocketMetricsSupplier;
  }

  @Override
  public void listening(SocketAddress localAddress) {
    this.localAddress = localAddress;
  }

  @Override
  public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    datagramSocketMetricsSupplier.incrementBytesReceived(localAddress, numberOfBytes);
  }

  @Override
  public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    datagramSocketMetricsSupplier.incrementBytesSent(remoteAddress, numberOfBytes);
  }

  @Override
  public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) {
    datagramSocketMetricsSupplier.incrementErrorCount();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {
  }
}
