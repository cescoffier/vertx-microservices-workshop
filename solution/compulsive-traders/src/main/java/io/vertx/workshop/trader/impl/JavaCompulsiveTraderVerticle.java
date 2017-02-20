package io.vertx.workshop.trader.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.workshop.common.MicroServiceVerticle;
import io.vertx.workshop.portfolio.PortfolioService;

/**
 * A compulsive trader...
 */
public class JavaCompulsiveTraderVerticle extends MicroServiceVerticle {

  @Override
  public void start(Future<Void> future) {
    super.start();

    //----
    // Initialize the trader
    String company = TraderUtils.pickACompany();
    int numberOfShares = TraderUtils.pickANumber();
    System.out.println("Java compulsive trader configured for company " + company + " and shares: " + numberOfShares);

    // We need to retrieve two services, create two futures object that will get the services
    Future<MessageConsumer<JsonObject>> marketFuture = Future.future();
    Future<PortfolioService> portfolioFuture = Future.future();
    // Retrieve the services, use the "special" completed to assign the future
    MessageSource.getConsumer(discovery, new JsonObject().put("name", "market-data"), marketFuture);
    EventBusService.getProxy(discovery, PortfolioService.class, portfolioFuture);

    // When done (both services retrieved), execute the handler
    CompositeFuture.all(marketFuture, portfolioFuture).setHandler(ar -> {
      if (ar.failed()) {
        future.fail("One of the required service cannot " +
            "be retrieved: " + ar.cause());
      } else {
        // Our services:
        PortfolioService portfolio = portfolioFuture.result();
        MessageConsumer<JsonObject> marketConsumer = marketFuture.result();

        // Listen the market...
        marketConsumer.handler(message -> {
          JsonObject quote = message.body();
          TraderUtils.dumbTradingLogic(company, numberOfShares, portfolio, quote);
        });

        future.complete();
      }
    });
    // ----
  }


}
