package io.vertx.workshop.trader.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.types.EventBusService;
import io.vertx.ext.discovery.types.MessageSource;
import io.vertx.workshop.common.MicroServiceVerticle;
import io.vertx.workshop.portfolio.PortfolioService;

import java.util.Random;

/**
 * A compulsive trader...
 */
public class CompulsiveTraderVerticle extends MicroServiceVerticle {

  private static final Random RANDOM = new Random();

  @Override
  public void start() {
    super.start();

    String company = config().getString("company");

    Future<MessageConsumer<JsonObject>> marketFuture = Future.future();
    Future<PortfolioService> portfolioFuture = Future.future();

    MessageSource.get(vertx, discovery, new JsonObject().put("name", "market-data"), marketFuture.completer());
    EventBusService.get(vertx, discovery, PortfolioService.class, portfolioFuture.completer());

    CompositeFuture.all(marketFuture, portfolioFuture).setHandler(ar -> {
      if (ar.failed()) {
        System.err.println("One of the required service cannot be retrieved: " + ar.cause());
      } else {
        PortfolioService portfolio = portfolioFuture.result();
        marketFuture.result().handler(message -> {
          JsonObject quote = message.body();
          if (quote.getString("name").equals(company)) {
            if (RANDOM.nextBoolean()) {
              portfolio.sell(2, quote, p -> {
                if (p.succeeded()) {
                  System.out.println("Sold 2 stocks of " + company + "!");
                } else {
                  System.out.println("D'oh, failed to sell 2 stocks of " + company + " : " + p.cause());
                }
              });
            } else {
              portfolio.buy(2, quote, p -> {
                if (p.succeeded()) {
                  System.out.println("Bought 2 stocks of " + company + " !");
                } else {
                  System.out.println("D'oh, failed to buy 2 stocks of " + company + " : " + p.cause());
                }
              });
            }
          }
        });
      }
    });
  }
}
