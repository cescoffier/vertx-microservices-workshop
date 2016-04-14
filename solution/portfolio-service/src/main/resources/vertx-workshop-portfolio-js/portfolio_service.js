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

/** @module vertx-workshop-portfolio-js/portfolio_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JPortfolioService = io.vertx.workshop.portfolio.PortfolioService;
var Portfolio = io.vertx.workshop.portfolio.Portfolio;

/**
 A service managing a portfolio.
 <p>
 This service is an event bus service (a.k.a service proxies, or async RPC). The client and server are generated at
 compile time.
 <p>
 @class
*/
var PortfolioService = function(j_val) {

  var j_portfolioService = j_val;
  var that = this;

  /**
   Gets the portfolio.

   @public
   @param resultHandler {function} the result handler called when the portfolio has been retrieved. The async result indicates whether the call was successful or not. 
   */
  this.getPortfolio = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_portfolioService["getPortfolio(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Buy `amount` shares of the given shares (quote).

   @public
   @param amount {number} the amount 
   @param quote {Object} the last quote 
   @param resultHandler {function} the result handler with the updated portfolio. If the action cannot be executed, the async result is market as a failure (not enough money, not enough shares available...) 
   */
  this.buy = function(amount, quote, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_portfolioService["buy(int,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](amount, utils.convParamJsonObject(quote), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Sell `amount` shares of the given shares (quote).

   @public
   @param amount {number} the amount 
   @param quote {Object} the last quote 
   @param resultHandler {function} the result handler with the updated portfolio. If the action cannot be executed, the async result is market as a failure (not enough share...) 
   */
  this.sell = function(amount, quote, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_portfolioService["sell(int,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](amount, utils.convParamJsonObject(quote), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Evaluates the current value of the portfolio.

   @public
   @param resultHandler {function} the result handler with the valuation 
   */
  this.evaluate = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_portfolioService["evaluate(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_portfolioService;
};

// We export the Constructor function
module.exports = PortfolioService;