/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
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
      obj.setBatchDelay(((Number) json.getValue("batchDelay")).intValue());
    }
    if (json.getValue("batchSize") instanceof Number) {
      obj.setBatchSize(((Number) json.getValue("batchSize")).intValue());
    }
    if (json.getValue("host") instanceof String) {
      obj.setHost((String) json.getValue("host"));
    }
    if (json.getValue("port") instanceof Number) {
      obj.setPort(((Number) json.getValue("port")).intValue());
    }
    if (json.getValue("prefix") instanceof String) {
      obj.setPrefix((String) json.getValue("prefix"));
    }
    if (json.getValue("schedule") instanceof Number) {
      obj.setSchedule(((Number) json.getValue("schedule")).intValue());
    }
    if (json.getValue("tenant") instanceof String) {
      obj.setTenant((String) json.getValue("tenant"));
    }
  }

  public static void toJson(VertxHawkularOptions obj, JsonObject json) {
    json.put("batchDelay", obj.getBatchDelay());
    json.put("batchSize", obj.getBatchSize());
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    json.put("port", obj.getPort());
    if (obj.getPrefix() != null) {
      json.put("prefix", obj.getPrefix());
    }
    json.put("schedule", obj.getSchedule());
    if (obj.getTenant() != null) {
      json.put("tenant", obj.getTenant());
    }
  }
}