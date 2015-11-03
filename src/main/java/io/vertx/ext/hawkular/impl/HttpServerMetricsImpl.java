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

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Thomas Segismont
 */
public class HttpServerMetricsImpl implements HttpServerMetrics<Long, Void, Void> {
  // Request info
  private final LongAdder processingTime = new LongAdder();
  private final LongAdder requestCount = new LongAdder();
  private final AtomicLong requests = new AtomicLong(0);
  // HTTP Connection info
  private final AtomicLong httpConnections = new AtomicLong(0);
  // Websocket Connection info
  private final AtomicLong wsConnections = new AtomicLong(0);
  // Bytes info
  private final LongAdder bytesReceived = new LongAdder();
  private final LongAdder bytesSent = new LongAdder();
  // Other
  private final LongAdder errorCount = new LongAdder();

  private final SocketAddress localAddress;
  private final HttpServerMetricsSupplier httpServerMetricsSupplier;

  public HttpServerMetricsImpl(SocketAddress localAddress, HttpServerMetricsSupplier httpServerMetricsSupplier) {
    this.localAddress = localAddress;
    this.httpServerMetricsSupplier = httpServerMetricsSupplier;
    httpServerMetricsSupplier.register(this);
  }

  @Override
  public Long requestBegin(Void socketMetric, HttpServerRequest request) {
    requests.incrementAndGet();
    return System.nanoTime();
  }

  @Override
  public void responseEnd(Long nanoStart, HttpServerResponse response) {
    long requestProcessingTime = System.nanoTime() - nanoStart;
    processingTime.add(requestProcessingTime);
    requestCount.increment();
    requests.decrementAndGet();
  }

  @Override
  public Void upgrade(Long requestMetric, ServerWebSocket serverWebSocket) {
    return null;
  }

  @Override
  public Void connected(Void socketMetric, ServerWebSocket serverWebSocket) {
    wsConnections.incrementAndGet();
    return null;
  }

  @Override
  public void disconnected(Void serverWebSocketMetric) {
    wsConnections.decrementAndGet();
  }

  @Override
  public Void connected(SocketAddress remoteAddress) {
    httpConnections.incrementAndGet();
    return null;
  }

  @Override
  public void disconnected(Void socketMetric, SocketAddress remoteAddress) {
    httpConnections.decrementAndGet();
  }

  @Override
  public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    bytesReceived.add(numberOfBytes);
  }

  @Override
  public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    bytesSent.add(numberOfBytes);
  }

  @Override
  public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) {
    errorCount.increment();
  }

  /**
   * @return the local {@link SocketAddress} of the {@link io.vertx.core.http.HttpServer}
   */
  public SocketAddress getServerAddress() {
    return localAddress;
  }

  /**
   * @return cumulated processing time of http requests
   */
  public Long getProcessingTime() {
    return processingTime.sum();
  }

  /**
   * @return total number of processed http requests
   */
  public Long getRequestCount() {
    return requestCount.sum();
  }

  /**
   * @return number of http requests currently processed
   */
  public Long getRequests() {
    return requests.get();
  }

  /**
   * @return number of http connections currently opened
   */
  public Long getHttpConnections() {
    return httpConnections.get();
  }

  /**
   * @return number of websocket connections currently opened
   */
  public Long getWsConnections() {
    return wsConnections.get();
  }

  /**
   * @return total number of bytes received
   */
  public Long getBytesReceived() {
    return bytesReceived.sum();
  }

  /**
   * @return total number of bytes sent
   */
  public Long getBytesSent() {
    return bytesSent.sum();
  }

  /**
   * @return total number of errors
   */
  public Long getErrorCount() {
    return errorCount.sum();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {
    httpServerMetricsSupplier.unregister(this);
  }
}
