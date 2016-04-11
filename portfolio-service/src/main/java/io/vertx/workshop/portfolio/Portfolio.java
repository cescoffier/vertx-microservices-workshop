package io.vertx.workshop.portfolio;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject(generateConverter = true)
public class Portfolio {

  private Map<String, Integer> stocks = new TreeMap<>();

  private double cash;

  public Portfolio() {
    // Empty constructor
  }

  public Portfolio(Portfolio other) {
    this.stocks = new TreeMap<>(other.stocks);
    this.cash = other.cash;
  }

  public Portfolio(JsonObject json) {
    PortfolioConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PortfolioConverter.toJson(this, json);
    return json;
  }

  public Map<String, Integer> getStocks() {
    return stocks;
  }

  public Portfolio setStocks(Map<String, Integer> stocks) {
    this.stocks = stocks;
    return this;
  }

  public double getCash() {
    return cash;
  }

  public Portfolio setCash(double cash) {
    this.cash = cash;
    return this;
  }

  // -- Additional method

  public int getAmount(String name) {
    Integer current = stocks.get(name);
    if (current == null) {
      return 0;
    }
    return current;
  }
}
