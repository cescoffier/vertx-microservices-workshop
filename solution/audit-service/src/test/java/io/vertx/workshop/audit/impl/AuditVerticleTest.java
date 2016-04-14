package io.vertx.workshop.audit.impl;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.DiscoveryOptions;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.impl.DefaultDiscoveryBackend;
import io.vertx.ext.discovery.types.MessageSource;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
      .put("backend-name", DefaultDiscoveryBackend.class.getName());
  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    Async async = tc.async();
    vertx = Vertx.vertx();
    Record record = MessageSource.createRecord("portfolio-events", "portfolio", JsonObject
        .class);
    DiscoveryService.create(vertx, new DiscoveryOptions()
        .setBackendConfiguration(new JsonObject().put("backend-name", DefaultDiscoveryBackend.class.getName())))
        .publish(record,
            r -> {
              if (r.failed()) {
                r.cause().printStackTrace();
                tc.fail(r.cause());
              }
              vertx.deployVerticle(AuditVerticle.class.getName(), new DeploymentOptions().setConfig(CONFIGURATION), s -> {
                async.complete();
              });
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

    vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
      tc.assertEquals(response.statusCode(), 200);
      response.bodyHandler(buffer -> {
        JsonArray array = buffer.toJsonArray();
        tc.assertEquals(array.size(), 1);
        async.complete();
      });
    });

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

    vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
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