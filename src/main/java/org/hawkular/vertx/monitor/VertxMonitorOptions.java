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
package org.hawkular.vertx.monitor;

import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;

/**
 * @author Thomas Segismont
 */
public class VertxMonitorOptions extends MetricsOptions {
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_TENANT = "default";
    public static final int DEFAULT_SCHEDULE = 1;
    public static final String DEFAULT_PREFIX = "";

    private String host;
    private int port;
    private String tenant;
    private int schedule;
    private String prefix;

    public VertxMonitorOptions() {
        host = DEFAULT_HOST;
        port = DEFAULT_PORT;
        tenant = DEFAULT_TENANT;
        schedule = DEFAULT_SCHEDULE;
        prefix = DEFAULT_PREFIX;
    }

    public VertxMonitorOptions(VertxMonitorOptions other) {
        super(other);
        host = other.host;
        port = other.port;
        tenant = other.tenant;
        schedule = other.schedule;
        prefix = other.prefix;
    }

    public VertxMonitorOptions(JsonObject json) {
        super(json);
        host = json.getString("host", DEFAULT_HOST);
        port = json.getInteger("port", DEFAULT_PORT);
        tenant = json.getString("tenant", DEFAULT_TENANT);
        schedule = json.getInteger("schedule", DEFAULT_SCHEDULE);
        prefix = json.getString("prefix", DEFAULT_PREFIX);
    }

    public String getHost() {
        return host;
    }

    public VertxMonitorOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public VertxMonitorOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public String getTenant() {
        return tenant;
    }

    public VertxMonitorOptions setTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    public int getSchedule() {
        return schedule;
    }

    public VertxMonitorOptions setSchedule(int schedule) {
        this.schedule = schedule;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public VertxMonitorOptions setPrefix(String prefix) {
        this.prefix = prefix == null ? DEFAULT_PREFIX : prefix;
        return this;
    }
}
