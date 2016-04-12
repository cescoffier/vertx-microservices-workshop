package io.vertx.workshop.quote;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class GeneratorConfigVerticleTest {

  @Test
  public void test() throws IOException {
    byte[] bytes = Files.readAllBytes(new File("src/test/resources/config.json").toPath());
    JsonObject config = new JsonObject(new String(bytes, "UTF-8"));

    Vertx vertx = Vertx.vertx();

    List<JsonObject> mch = new ArrayList<>();
    List<JsonObject> dvn = new ArrayList<>();
    List<JsonObject> bct = new ArrayList<>();

    vertx.eventBus().consumer(GeneratorConfigVerticle.ADDRESS, message -> {
      JsonObject quote = (JsonObject) message.body();
      System.out.println(quote.encodePrettily());
      assertThat(quote.getDouble("bid")).isGreaterThan(0);
      assertThat(quote.getDouble("ask")).isGreaterThan(0);
      assertThat(quote.getInteger("volume")).isGreaterThan(0);
      assertThat(quote.getInteger("shares")).isGreaterThan(0);
      switch (quote.getString("symbol")) {
        case "MCH":
          mch.add(quote);
          break;
        case "DVN":
          dvn.add(quote);
          break;
        case "BCT":
          bct.add(quote);
          break;
      }
    });

    vertx.deployVerticle(GeneratorConfigVerticle.class.getName(), new DeploymentOptions().setConfig(config));

    await().until(() -> mch.size() > 10);
    await().until(() -> dvn.size() > 10);
    await().until(() -> bct.size() > 10);
  }

}