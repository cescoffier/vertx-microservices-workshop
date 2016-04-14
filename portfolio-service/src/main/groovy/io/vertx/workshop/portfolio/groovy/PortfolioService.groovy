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
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.workshop.portfolio.Portfolio
import io.vertx.core.Handler
/**
 * A service managing a portfolio.
 * <p>
 * This service is an event bus service (a.k.a service proxies, or async RPC). The client and server are generated at
 * compile time.
 * <p>
 * All method are asynchronous and so ends with a  parameter.
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
  /**
   * Gets the portfolio.
   * @param resultHandler the result handler called when the portfolio has been retrieved. The async result indicates whether the call was successful or not.
   */
  public void getPortfolio(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    delegate.getPortfolio(resultHandler != null ? new Handler<AsyncResult<io.vertx.workshop.portfolio.Portfolio>>() {
      public void handle(AsyncResult<io.vertx.workshop.portfolio.Portfolio> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Map<String, Object>)InternalHelper.wrapObject(ar.result()?.toJson())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  /**
   * Buy `amount` shares of the given shares (quote).
   * @param amount the amount
   * @param quote the last quote
   * @param resultHandler the result handler with the updated portfolio. If the action cannot be executed, the async result is market as a failure (not enough money, not enough shares available...)
   */
  public void buy(int amount, Map<String, Object> quote, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    delegate.buy(amount, quote != null ? new io.vertx.core.json.JsonObject(quote) : null, resultHandler != null ? new Handler<AsyncResult<io.vertx.workshop.portfolio.Portfolio>>() {
      public void handle(AsyncResult<io.vertx.workshop.portfolio.Portfolio> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Map<String, Object>)InternalHelper.wrapObject(ar.result()?.toJson())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  /**
   * Sell `amount` shares of the given shares (quote).
   * @param amount the amount
   * @param quote the last quote
   * @param resultHandler the result handler with the updated portfolio. If the action cannot be executed, the async result is market as a failure (not enough share...)
   */
  public void sell(int amount, Map<String, Object> quote, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    delegate.sell(amount, quote != null ? new io.vertx.core.json.JsonObject(quote) : null, resultHandler != null ? new Handler<AsyncResult<io.vertx.workshop.portfolio.Portfolio>>() {
      public void handle(AsyncResult<io.vertx.workshop.portfolio.Portfolio> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture((Map<String, Object>)InternalHelper.wrapObject(ar.result()?.toJson())));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  /**
   * Evaluates the current value of the portfolio.
   * @param resultHandler the result handler with the valuation
   */
  public void evaluate(Handler<AsyncResult<Double>> resultHandler) {
    delegate.evaluate(resultHandler);
  }
}
