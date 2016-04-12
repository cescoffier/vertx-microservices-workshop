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

package io.vertx.workshop.portfolio;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link io.vertx.workshop.portfolio.Portfolio}.
 *
 * NOTE: This class has been automatically generated from the {@link io.vertx.workshop.portfolio.Portfolio} original class using Vert.x codegen.
 */
public class PortfolioConverter {

  public static void fromJson(JsonObject json, Portfolio obj) {
    if (json.getValue("cash") instanceof Number) {
      obj.setCash(((Number)json.getValue("cash")).doubleValue());
    }
    if (json.getValue("shares") instanceof JsonObject) {
      java.util.Map<String, java.lang.Integer> map = new java.util.LinkedHashMap<>();
      json.getJsonObject("shares").forEach(entry -> {
        if (entry.getValue() instanceof Number)
          map.put(entry.getKey(), ((Number)entry.getValue()).intValue());
      });
      obj.setShares(map);
    }
  }

  public static void toJson(Portfolio obj, JsonObject json) {
    json.put("cash", obj.getCash());
    if (obj.getShares() != null) {
      JsonObject map = new JsonObject();
      obj.getShares().forEach((key,value) -> map.put(key, value));
      json.put("shares", map);
    }
  }
}