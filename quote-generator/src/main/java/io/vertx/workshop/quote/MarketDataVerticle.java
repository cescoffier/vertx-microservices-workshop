package io.vertx.workshop.quote;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.Random;

/**
 * A verticle simulating the evaluation of a company evaluation in a very unrealistic and irrational way.
 * It emits the new data on the `market` address on the event bus.
 */
public class MarketDataVerticle extends AbstractVerticle {

  String name;
  int variation;
  long period;
  String symbol;
  int stocks;
  double price;

  double bid;
  double ask;

  int share;
  private double value;

  private final Random random = new Random();

  /**
   * Method called when the verticle is deployed.
   */
  @Override
  public void start() {
    // Retrieve the configuration, and initialize the verticle.
    JsonObject config = config();
    init(config);

    // Every `period` ms, the given Handler is called.
    vertx.setPeriodic(period, l -> {
      compute();
      send();
    });
  }

  /**
   * Read the configuration and set the initial values.
   * @param config the configuration
   */
  void init(JsonObject config) {
    period = config.getLong("period", 3000L);
    variation = config.getInteger("variation", 100);
    name = config.getString("name");
    Objects.requireNonNull(name);
    symbol = config.getString("symbol", name);
    stocks = config.getInteger("volume", 10000);
    price = config.getDouble("price", 100.0);

    value = price;
    ask = price + random.nextInt(variation / 2);
    bid = price + random.nextInt(variation / 2);

    share = stocks / 2;
  }

  /**
   * Sends the market data on the event bus.
   */
  private void send() {
    vertx.eventBus().publish(GeneratorConfigVerticle.ADDRESS, toJson());
  }

  /**
   * Compute the new evaluation...
   */
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

  /**
   * @return a json representation of the market data (quote). The structure is close to
   * <a href="https://en.wikipedia.org/wiki/Market_data">https://en.wikipedia.org/wiki/Market_data</a>.
   */
  private JsonObject toJson() {
    return new JsonObject()
        .put("exchange", "vert.x stock exchange")
        .put("symbol", symbol)
        .put("name", name)
        .put("bid", bid)
        .put("ask", ask)
        .put("volume", stocks)
        .put("open", price)
        .put("shares", share);

  }
}
