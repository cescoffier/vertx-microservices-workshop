package io.vertx.workshop.quote;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.workshop.common.MicroServiceVerticle;

/**
 * a verticle generating "fake" quotes based on the configuration.
 */
public class GeneratorConfigVerticle extends MicroServiceVerticle {

  @Override
  public void start() throws Exception {
    super.start();

    JsonArray quotes = config().getJsonArray("quotes");
    for (Object q : quotes) {
      JsonObject quote = (JsonObject) q;
      vertx.deployVerticle(QuoteVerticle.class.getName(), new DeploymentOptions().setConfig(quote));
    }
    vertx.deployVerticle(RestQuoteAPIVerticle.class.getName());

    publishMessageSource("quotes", "stocks", rec -> {
      if (!rec.succeeded()) {
        rec.cause().printStackTrace();
      }
      System.out.println("Quotes service published : " + rec.succeeded());
    });
  }
}
