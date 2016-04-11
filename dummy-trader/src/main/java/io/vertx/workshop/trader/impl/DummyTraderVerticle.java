package io.vertx.workshop.trader.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.types.MessageSource;
import io.vertx.workshop.common.MicroServiceVerticle;
import io.vertx.workshop.portfolio.PortfolioService;

import java.util.Random;

/**
 * a very dummy trader...
 */
public class DummyTraderVerticle extends MicroServiceVerticle {


  @Override
  public void start() throws Exception {
    super.start();
    Random random = new Random();
    PortfolioService portfolio = PortfolioService.getProxy(vertx);

    MessageSource.<JsonObject>get(vertx, discovery, new JsonObject().put("name", "quotes"), ar -> {
      if (ar.failed()) {
        System.err.println("No quote service, is the quote generator started ?");
      } else {
        System.out.println("Setting handler on " + ar.result().address());
        ar.result().handler(message -> {
          JsonObject quote = message.body();
          if (quote.getString("name").equals("Divinator")) {
            if (random.nextBoolean()) {
              portfolio.sell(2, quote, p -> {
                if (p.succeeded()) {
                  System.out.println("Sold 2 stocks of Divinator !");
                } else {
                  System.out.println("D'oh, failed to sell 2 stocks of Divinator " + p.cause());
                }
              });
            } else {
              portfolio.buy(2, quote, p -> {
                if (p.succeeded()) {
                  System.out.println("Bought 2 stocks of Divinator !");
                } else {
                  System.out.println("D'oh, failed to buy 2 stocks of Divinator " + p.cause());
                }
              });
            }
          }
        });
      }
    });
  }
}
