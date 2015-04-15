/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.vertx.monitor.impl;

import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;

/**
 * @author Thomas Segismont
 */
public class EventBusMetricsImpl implements EventBusMetrics<Void> {
    @Override
    public Void handlerRegistered(String address, boolean replyHandler) {
        return null;
    }

    @Override
    public void handlerUnregistered(Void handler) {
    }

    @Override
    public void beginHandleMessage(Void handler, boolean local) {
    }

    @Override
    public void endHandleMessage(Void handler, Throwable failure) {
    }

    @Override
    public void messageSent(String address, boolean publish, boolean local, boolean remote) {
    }

    @Override
    public void messageReceived(String address, boolean publish, boolean local, int handlers) {
    }

    @Override
    public void messageWritten(String address, int numberOfBytes) {
    }

    @Override
    public void messageRead(String address, int numberOfBytes) {
    }

    @Override
    public void replyFailure(String address, ReplyFailure failure) {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void close() {
    }
}
