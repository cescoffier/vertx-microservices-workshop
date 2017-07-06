package io.vertx.workshop.dashboard;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.servicediscovery.rest.ServiceDiscoveryRestEndpoint;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.workshop.common.MicroServiceVerticle;

/**
 * The dashboard of the micro-trader application.
 */
public class DashboardVerticle extends MicroServiceVerticle {

  private CircuitBreaker circuit;
  private WebClient client;

  @Override
  public void start(Future<Void> future) {
    super.start();
    Router router = Router.router(vertx);

    // Event bus bridge
    SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
    BridgeOptions options = new BridgeOptions();
    options
        .addOutboundPermitted(new PermittedOptions().setAddress("market"))
        .addOutboundPermitted(new PermittedOptions().setAddress("portfolio"))
        .addOutboundPermitted(new PermittedOptions().setAddress("service.portfolio"))
        .addInboundPermitted(new PermittedOptions().setAddress("service.portfolio"))
        .addOutboundPermitted(new PermittedOptions().setAddress("vertx.circuit-breaker"));

    sockJSHandler.bridge(options);
    router.route("/eventbus/*").handler(sockJSHandler);

    // Discovery endpoint
    ServiceDiscoveryRestEndpoint.create(router, discovery);

    // Last operations
    router.get("/operations").handler(this::callAuditServiceWithExceptionHandlerWithCircuitBreaker);

    // Static content
    router.route("/*").handler(StaticHandler.create());

    // Create a circuit breaker.
    circuit = CircuitBreaker.create("http-audit-service", vertx,
        new CircuitBreakerOptions()
            .setMaxFailures(2)
            .setFallbackOnFailure(true)
            .setResetTimeout(2000)
            .setTimeout(1000))
        .openHandler(v -> retrieveAuditService());

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(8080, ar -> {
          if (ar.failed()) {
            future.fail(ar.cause());
          } else {
            retrieveAuditService();
            future.complete();
          }
        });
  }

  @Override
  public void stop() throws Exception {
    if (client != null) {
      client.close();
    }
    circuit.close();
  }

  private Future<Void> retrieveAuditService() {
    return Future.future(future -> {
      HttpEndpoint.getWebClient(discovery, new JsonObject().put("name", "audit"), client -> {
        this.client = client.result();
        future.<Void>handle(client.map((Void)null));
      });
    });
  }


  private void callAuditService(RoutingContext context) {
    if (client == null) {
      context.response()
          .putHeader("content-type", "application/json")
          .setStatusCode(200)
          .end(new JsonObject().put("message", "No audit service").encode());
    } else {
      client.get("/").send(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          context.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(200)
              .end(response.body());
        }
      });
    }
  }

  private void callAuditServiceTimeout(RoutingContext context) {
    if (client == null) {
      context.response()
          .putHeader("content-type", "application/json")
          .setStatusCode(200)
          .end(new JsonObject().put("message", "No audit service").encode());
    } else {
      client.get("/")
          .timeout(5000)
          .send(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          context.response()
              .putHeader("content-type", "application/json")
              .setStatusCode(200)
              .end(response.body());
        } else {
          context.fail(ar.cause());
        }
      });
    }
  }

  private void callAuditServiceTimeoutWithCircuitBreaker(RoutingContext context) {
    HttpServerResponse resp = context.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200);

    circuit.executeWithFallback(
        future ->
            client.get("/").send(ar -> future.handle(ar.map(HttpResponse::body))),
        t -> Buffer.buffer("{\"message\":\"No audit service, or unable to call it\"}")
    )
        .setHandler(ar -> resp.end(ar.result()));
  }
}
