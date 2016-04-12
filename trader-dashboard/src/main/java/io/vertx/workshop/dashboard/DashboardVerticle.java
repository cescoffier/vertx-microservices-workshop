package io.vertx.workshop.dashboard;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.rest.DiscoveryRestEndpoint;
import io.vertx.ext.discovery.types.HttpEndpoint;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.workshop.common.MicroServiceVerticle;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DashboardVerticle extends MicroServiceVerticle {

  @Override
  public void start() {
    super.start();
    Router router = Router.router(vertx);

    // Eventbus bridge
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions();
    options
        .addOutboundPermitted(new PermittedOptions().setAddress("market"))
        .addOutboundPermitted(new PermittedOptions().setAddress("portfolio"))
        .addOutboundPermitted(new PermittedOptions().setAddress("service.portfolio"))
        .addInboundPermitted(new PermittedOptions().setAddress("service.portfolio"));

    sockJSHandler.bridge(options);
    router.route("/eventbus/*").handler(sockJSHandler);

    // Discovery endpoint
    DiscoveryRestEndpoint.create(router, discovery);

    // Last operation
    router.get("/operations").handler(this::lastOperations);

    // Static content
    router.route("/*").handler(StaticHandler.create());


    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(8080);
  }

  private void lastOperations(RoutingContext context) {
    try {
      HttpEndpoint.get(vertx, discovery, new JsonObject().put("name", "AUDIT"), client -> {
        if (client.failed() || client.result() == null) {
          System.err.println("No audit service");
          context.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(200)
              .end(new JsonObject().put("message", "No audit service").encode());
        } else {
          client.result().getNow("/", response -> {
            response
                .bodyHandler(buffer -> context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(200)
                    .end(buffer));
          });
        }
      });
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
