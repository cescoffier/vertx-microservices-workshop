package io.vertx.workshop.quote;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class QuoteVerticleTest {


  @Test
  public void testComputation() {
    JsonObject json = new JsonObject()
        .put("name", "test")
        .put("symbol", "TT");

    QuoteVerticle verticle = new QuoteVerticle();
    verticle.init(json);

    int volume = verticle.stocks;

    assertThat(verticle.ask).isGreaterThan(0.0);
    assertThat(verticle.bid).isGreaterThan(0.0);
    assertThat(verticle.share).isGreaterThanOrEqualTo(0).isLessThanOrEqualTo(volume);

    for (int i = 0; i < 1000000; i++) {
      verticle.compute();
      assertThat(verticle.ask).isGreaterThan(0.0);
      assertThat(verticle.bid).isGreaterThan(0.0);
      assertThat(verticle.share).isGreaterThanOrEqualTo(0).isLessThanOrEqualTo(volume);
    }

  }

}