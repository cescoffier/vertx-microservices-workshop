package io.vertx.workshop.quote;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This verticle exposes a HTTP endpoint to retrieve the current / last values of the maker data (quotes).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RestQuoteAPIVerticle extends AbstractVerticle {

  private Map<String, JsonObject> quotes = new HashMap<>();

  @Override
  public void start() throws Exception {
    vertx.eventBus().<JsonObject>consumer(GeneratorConfigVerticle.ADDRESS, message -> {
      // TODO Populate the `quotes` map with the received quote
      // Quotes are json objects you can retrieve from the message body
      // The map is structured as follows: name -> quote
      // ----

      // ----
    });



    vertx.createHttpServer()
        .requestHandler(request -> {
          HttpServerResponse response = request.response()
              .putHeader("content-type", "application/json");

          // TODO
          // The request handler returns a specific quote if the `symbol` parameter is set, or the whole map if none.
          // To write the response use: `request.response().end(content)`
          // Responses are returned as JSON, so don't forget the "content-type": "application/json" header.
          // If the symbol is set but not found, you should return 404.
          // Once the request handler is set,

          response
              .end(Json.encodePrettily(quotes));

          // ----

          // ----
        })
        .listen(8080, ar -> {
          if (ar.succeeded()) {
            System.out.println("Server started");
          } else {
            System.out.println("Cannot start the server: " + ar.cause());
          }
        });
  }
}
