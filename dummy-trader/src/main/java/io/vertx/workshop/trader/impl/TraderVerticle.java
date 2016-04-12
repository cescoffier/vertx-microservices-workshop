package io.vertx.workshop.trader.impl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

/**
 * The main verticle creating traders.
 */
public class TraderVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.deployVerticle(CompulsiveTraderVerticle.class.getName(),
        new DeploymentOptions().setConfig(new JsonObject().put("company", "Divinator")));

    vertx.deployVerticle(CompulsiveTraderVerticle.class.getName(),
        new DeploymentOptions().setConfig(new JsonObject().put("company", "Black Coat")));
  }
}
