package io.vertx.workshop.portfolio.impl;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.types.HttpEndpoint;
import io.vertx.workshop.portfolio.Portfolio;
import io.vertx.workshop.portfolio.PortfolioService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The portfolio service implementation.
 */
public class PortfolioServiceImpl implements PortfolioService {

  private final Vertx vertx;
  private final Portfolio portfolio;
  private final DiscoveryService discovery;

  public PortfolioServiceImpl(Vertx vertx, DiscoveryService discovery, double initialCash) {
    this.vertx = vertx;
    this.portfolio = new Portfolio().setCash(initialCash);
    this.discovery = discovery;
  }

  @Override
  public void getPortfolio(Handler<AsyncResult<Portfolio>> resultHandler) {
    // TODO
    // ----
    resultHandler.handle(Future.succeededFuture(portfolio));
    // ----
  }

  private void sendActionOnTheEventBus(String action, int amount, JsonObject quote, int newAmount) {
    // TODO
    // ----
    vertx.eventBus().publish(EVENT_ADDRESS, new JsonObject()
        .put("action", action)
        .put("quote", quote)
        .put("date", System.currentTimeMillis())
        .put("amount", amount)
        .put("owned", newAmount)
    );
    // ----
  }

  @Override
  public void evaluate(Handler<AsyncResult<Double>> resultHandler) {
    // TODO
    // ----

    //TODO improve composition here.

    System.out.println("Evaluating portfolio");

    HttpEndpoint.get(vertx, discovery, new JsonObject().put("name", "CONSOLIDATION"), client -> {
      if (client.failed()) {
        resultHandler.handle(Future.failedFuture(client.cause()));
      } else {
        // We have the client, time to call it
        List<Future> futures = new ArrayList<>();
        Set<Map.Entry<String, Integer>> entries = portfolio.getShares().entrySet();
        for (Map.Entry<String, Integer> entry : entries) {
          Future<Double> future = Future.future();
          futures.add(future);
          client.result().getNow("/?name=" + encode(entry.getKey()), response -> {
            if (response.statusCode() == 200) {
              response.bodyHandler(buffer -> {
                double v = entry.getValue() * buffer.toJsonObject().getDouble("bid");
                future.complete(v);
              });
            } else {
              future.complete(0.0);
            }
          });
        }

        if (futures.isEmpty()) {
          resultHandler.handle(Future.succeededFuture(0.0));
        } else {
          CompositeFuture.all(futures).setHandler(
              ar -> {
                double sum = futures.stream().mapToDouble(fut -> (double) fut.result()).sum();
                System.out.println("Computed evaluation : " + sum + " / " + portfolio.getShares());
                resultHandler.handle(Future.succeededFuture(sum));
              });
        }
      }
    });

    // ---
  }


  @Override
  public void buy(int amount, JsonObject quote, Handler<AsyncResult<Portfolio>> resultHandler) {
    if (amount <= 0) {
      resultHandler.handle(Future.failedFuture("Cannot buy " + quote.getString("name") + " - the amount must be " +
          "greater than 0"));
    }

    if (quote.getInteger("shares") < amount) {
      resultHandler.handle(Future.failedFuture("Cannot buy " + amount + " - not enough " +
          "stocks on the market (" + quote.getInteger("shares") + ")"));
    }

    double price = amount * quote.getDouble("ask");
    String name = quote.getString("name");
    // 1) do we have enough money
    if (portfolio.getCash() >= price) {
      // Yes, buy it
      portfolio.setCash(portfolio.getCash() - price);
      int current = portfolio.getAmount(name);
      int newAmount = current + amount;
      portfolio.getShares().put(name, newAmount);
      sendActionOnTheEventBus("BUY", amount, quote, newAmount);
      resultHandler.handle(Future.succeededFuture(portfolio));
    } else {
      resultHandler.handle(Future.failedFuture("Cannot buy " + amount + " of " + name + " - " + "not enough money, " +
          "need " + price + ", has " + portfolio.getCash()));
    }
  }


  @Override
  public void sell(int amount, JsonObject quote, Handler<AsyncResult<Portfolio>> resultHandler) {
    if (amount <= 0) {
      resultHandler.handle(Future.failedFuture("Cannot sell " + quote.getString("name") + " - the amount must be " +
          "greater than 0"));
    }

    double price = amount * quote.getDouble("bid");
    String name = quote.getString("name");
    int current = portfolio.getAmount(name);
    // 1) do we have enough stocks
    if (current >= amount) {
      // Yes, sell it
      int newAmount = current - amount;
      if (newAmount == 0) {
        portfolio.getShares().remove(name);
      } else {
        portfolio.getShares().put(name, newAmount);
      }
      portfolio.setCash(portfolio.getCash() + price);
      sendActionOnTheEventBus("SELL", amount, quote, newAmount);
      resultHandler.handle(Future.succeededFuture(portfolio));
    } else {
      resultHandler.handle(Future.failedFuture("Cannot sell " + amount + " of " + name + " - " + "not enough stocks " +
          "in portfolio"));
    }

  }

  private static String encode(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported encoding");
    }
  }


}
