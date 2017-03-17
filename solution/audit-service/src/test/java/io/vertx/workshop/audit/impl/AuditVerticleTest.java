package io.vertx.workshop.audit.impl;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.impl.DefaultServiceDiscoveryBackend;
import io.vertx.servicediscovery.types.MessageSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class AuditVerticleTest {

  private final static JsonObject CONFIGURATION = new JsonObject()
      .put("url", "jdbc:hsqldb:mem:audit?shutdown=true")
      .put("driverclass", "org.hsqldb.jdbcDriver")
      .put("drop", true)
      .put("backend-name", DefaultServiceDiscoveryBackend.class.getName())
      .put("http.port", 8081);
  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    Async async = tc.async();
    vertx = Vertx.vertx();
    Record record = MessageSource.createRecord("portfolio-events", "portfolio", JsonObject
        .class);
    ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions()
        .setBackendConfiguration(new JsonObject().put("backend-name", DefaultServiceDiscoveryBackend.class.getName())))
        .publish(record,
            r -> {
              if (r.failed()) {
                r.cause().printStackTrace();
                tc.fail(r.cause());
              }
              vertx.deployVerticle(AuditVerticle.class.getName(), new DeploymentOptions().setConfig(CONFIGURATION), tc.asyncAssertSuccess(s -> async.complete()));
            });
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void testInsertion(TestContext tc) {
    Async async = tc.async();

    vertx.eventBus().publish("portfolio", createBuyOperation());


    WebClient.create(vertx).get(8081, "localhost", "/")
        .as(BodyCodec.jsonArray())
        .send(tc.asyncAssertSuccess(response -> {
      tc.assertEquals(response.statusCode(), 200);
      tc.assertEquals(response.body().size(), 1);
      async.complete();
    }));

  }

  @Test
  public void testMultipleInsertion(TestContext tc) throws InterruptedException {
    Async async = tc.async();

    vertx.eventBus().publish("portfolio", createBuyOperation());
    vertx.eventBus().publish("portfolio", createSellOperation());
    vertx.eventBus().publish("portfolio", createBuyOperation());
    vertx.eventBus().publish("portfolio", createSellOperation());
    vertx.eventBus().publish("portfolio", createBuyOperation());
    vertx.eventBus().publish("portfolio", createSellOperation());
    vertx.eventBus().publish("portfolio", createBuyOperation());
    vertx.eventBus().publish("portfolio", createSellOperation());
    vertx.eventBus().publish("portfolio", createBuyOperation());
    vertx.eventBus().publish("portfolio", createSellOperation());
    vertx.eventBus().publish("portfolio", createBuyOperation());
    vertx.eventBus().publish("portfolio", createSellOperation());

    vertx.createHttpClient().getNow(8081, "localhost", "/", response -> {
      tc.assertEquals(response.statusCode(), 200);
      response.bodyHandler(buffer -> {
        JsonArray array = buffer.toJsonArray();
        tc.assertEquals(array.size(), 10);
        async.complete();
      });
    });

  }

  private JsonObject createBuyOperation() {
    return new JsonObject()
        .put("action", "BUY")
        .put("quote", "stuff")
        .put("data", System.currentTimeMillis())
        .put("original-amount", 10)
        .put("new-amount", 15);
  }

  private JsonObject createSellOperation() {
    return new JsonObject()
        .put("action", "SELL")
        .put("quote", "stuff")
        .put("data", System.currentTimeMillis())
        .put("original-amount", 15)
        .put("new-amount", 10);
  }

}