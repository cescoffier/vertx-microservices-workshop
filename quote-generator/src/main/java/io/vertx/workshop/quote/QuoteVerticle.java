package io.vertx.workshop.quote;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.Random;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class QuoteVerticle extends AbstractVerticle {

  private String name;
  private int variation;
  private long period;
  private String symbol;
  int stocks;
  private double price;

  double bid;
  double ask;

  int share;
  private double value;
  private Random random;

  @Override
  public void start() throws Exception {
    JsonObject config = config();
    init(config);

    vertx.setPeriodic(period, l -> {
      compute();
      send();
    });
  }

  void init(JsonObject config) {
    period = config.getLong("period", 3000L);
    variation = config.getInteger("variation", 100);
    name = config.getString("name");
    Objects.requireNonNull(name);
    symbol = config.getString("symbol", name);
    stocks = config.getInteger("volume", 10000);
    price = config.getDouble("price", 100.0);

    random = new Random();

    value = price;
    ask = price + random.nextInt(variation / 2);
    bid = price + random.nextInt(variation / 2);

    share = stocks / 2;
  }

  private void send() {
    vertx.eventBus().publish("stocks", toJson());
  }

  void compute() {

    if (random.nextBoolean()) {
      value = value + random.nextInt(variation);
      ask = value + random.nextInt(variation / 2);
      bid = value + random.nextInt(variation / 2);
    } else {
      value = value - random.nextInt(variation);
      ask = value - random.nextInt(variation / 2);
      bid = value - random.nextInt(variation / 2);
    }

    if (value <= 0) {
      value = 1.0;
    }
    if (ask <= 0) {
      ask = 1.0;
    }
    if (bid <= 0) {
      bid = 1.0;
    }

    if (random.nextBoolean()) {
      // Adjust share
      int shareVariation = random.nextInt(100);
      if (shareVariation > 0 && share + shareVariation < stocks) {
        share += shareVariation;
      } else if (shareVariation < 0 && share + shareVariation > 0) {
        share += shareVariation;
      }
    }
  }

  private JsonObject toJson() {
    return new JsonObject()
        .put("symbol", symbol)
        .put("name", name)
        .put("bid", bid)
        .put("ask", ask)
        .put("volume", stocks)
        .put("open", price)
        .put("shares", share);

  }
}
