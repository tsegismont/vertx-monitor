/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.ext.hawkular.impl;

import java.util.concurrent.atomic.LongAdder;

import static java.util.concurrent.TimeUnit.*;

/**
 * Holds measurements for all handlers of an event bus address. An instance is created when the first handler is
 * registered, then handlers are counted with {@link #incrementHandlersCount()} and {@link #decrementHandlersCount()}.
 *
 * @author Thomas Segismont
 */
public class HandlersMeasurements {
  private final LongAdder processingTime;
  private final int handlersCount;

  /**
   * Creates a new instance with a handlers reference of 1.
   */
  public HandlersMeasurements() {
    processingTime = new LongAdder();
    handlersCount = 1;
  }

  private HandlersMeasurements(LongAdder processingTime, int handlersCount) {
    this.processingTime = processingTime;
    this.handlersCount = handlersCount;
  }

  /**
   * Increments total processing time.
   *
   * @param time processing time to add, in nanoseconds
   */
  public void addProcessingTime(long time) {
    processingTime.add(time);
  }

  /**
   * @return the total processing time, in milliseconds
   */
  public long processingTime() {
    return MILLISECONDS.convert(processingTime.sum(), NANOSECONDS);
  }

  /**
   * @return number of handlers of a same address
   */
  public int handlersCount() {
    return handlersCount;
  }

  /**
   * @return a new instance of this class, with same processing time and a handler count incremented by 1
   */
  public HandlersMeasurements incrementHandlersCount() {
    return new HandlersMeasurements(processingTime, handlersCount + 1);
  }

  /**
   * @return a new instance of this class, with same processing time and a handler count decremented by 1
   */
  public HandlersMeasurements decrementHandlersCount() {
    return new HandlersMeasurements(processingTime, handlersCount - 1);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HandlersMeasurements that = (HandlersMeasurements) o;
    return handlersCount == that.handlersCount;
  }

  @Override
  public int hashCode() {
    return handlersCount;
  }
}
