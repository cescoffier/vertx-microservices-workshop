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

package io.vertx.workshop.portfolio.groovy;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.workshop.portfolio.Portfolio
import io.vertx.core.Handler
/**
 * A service managing a portfolio.
*/
@CompileStatic
public class PortfolioService {
  private final def io.vertx.workshop.portfolio.PortfolioService delegate;
  public PortfolioService(Object delegate) {
    this.delegate = (io.vertx.workshop.portfolio.PortfolioService) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void getPortfolio(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.getPortfolio(new Handler<AsyncResult<io.vertx.workshop.portfolio.Portfolio>>() {
      public void handle(AsyncResult<io.vertx.workshop.portfolio.Portfolio> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()?.toJson()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void buy(int amount, Map<String, Object> quote, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.buy(amount, quote != null ? new io.vertx.core.json.JsonObject(quote) : null, new Handler<AsyncResult<io.vertx.workshop.portfolio.Portfolio>>() {
      public void handle(AsyncResult<io.vertx.workshop.portfolio.Portfolio> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()?.toJson()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void sell(int amount, Map<String, Object> quote, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.sell(amount, quote != null ? new io.vertx.core.json.JsonObject(quote) : null, new Handler<AsyncResult<io.vertx.workshop.portfolio.Portfolio>>() {
      public void handle(AsyncResult<io.vertx.workshop.portfolio.Portfolio> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()?.toJson()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void evaluate(Handler<AsyncResult<Double>> resultHandler) {
    this.delegate.evaluate(resultHandler);
  }
  public static PortfolioService getProxy(Vertx vertx) {
    def ret= InternalHelper.safeCreate(io.vertx.workshop.portfolio.PortfolioService.getProxy((io.vertx.core.Vertx)vertx.getDelegate()), io.vertx.workshop.portfolio.groovy.PortfolioService.class);
    return ret;
  }
}
