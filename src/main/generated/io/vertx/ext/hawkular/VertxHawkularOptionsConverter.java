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

package io.vertx.ext.hawkular;

import io.vertx.core.json.JsonObject;

/**
 * Converter for {@link io.vertx.ext.hawkular.VertxHawkularOptions}.
 *
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.hawkular.VertxHawkularOptions} original class using Vert.x codegen.
 */
public class VertxHawkularOptionsConverter {

  public static void fromJson(JsonObject json, VertxHawkularOptions obj) {
    if (json.getValue("batchDelay") instanceof Number) {
      obj.setBatchDelay(((Number)json.getValue("batchDelay")).intValue());
    }
    if (json.getValue("batchSize") instanceof Number) {
      obj.setBatchSize(((Number)json.getValue("batchSize")).intValue());
    }
    if (json.getValue("enabled") instanceof Boolean) {
      obj.setEnabled((Boolean)json.getValue("enabled"));
    }
    if (json.getValue("httpOptions") instanceof JsonObject) {
      obj.setHttpOptions(new io.vertx.core.http.HttpClientOptions((JsonObject)json.getValue("httpOptions")));
    }
    if (json.getValue("metricsServiceUri") instanceof String) {
      obj.setMetricsServiceUri((String)json.getValue("metricsServiceUri"));
    }
    if (json.getValue("prefix") instanceof String) {
      obj.setPrefix((String)json.getValue("prefix"));
    }
    if (json.getValue("schedule") instanceof Number) {
      obj.setSchedule(((Number)json.getValue("schedule")).intValue());
    }
    if (json.getValue("tenant") instanceof String) {
      obj.setTenant((String)json.getValue("tenant"));
    }
  }

  public static void toJson(VertxHawkularOptions obj, JsonObject json) {
    json.put("batchDelay", obj.getBatchDelay());
    json.put("batchSize", obj.getBatchSize());
    json.put("enabled", obj.isEnabled());
    if (obj.getMetricsServiceUri() != null) {
      json.put("metricsServiceUri", obj.getMetricsServiceUri());
    }
    if (obj.getPrefix() != null) {
      json.put("prefix", obj.getPrefix());
    }
    json.put("schedule", obj.getSchedule());
    if (obj.getTenant() != null) {
      json.put("tenant", obj.getTenant());
    }
  }
}