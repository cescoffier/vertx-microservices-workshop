package io.vertx.workshop.portfolio;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;


/**
 * A service managing a portfolio.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
@ProxyGen
public interface PortfolioService {

  void getPortfolio(Handler<AsyncResult<Portfolio>> resultHandler);

  void buy(int amount, JsonObject quote, Handler<AsyncResult<Portfolio>> resultHandler);

  void sell(int amount, JsonObject quote, Handler<AsyncResult<Portfolio>> resultHandler);

  void evaluate(Handler<AsyncResult<Double>> resultHandler);

  String ADDRESS = "service.portfolio";
  String EVENT_ADDRESS = "portfolio";

  static PortfolioService getProxy(Vertx vertx) {
    return ProxyHelper.createProxy(PortfolioService.class, vertx, ADDRESS);
  }
}
