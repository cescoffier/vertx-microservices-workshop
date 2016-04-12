package io.vertx.workshop.portfolio.impl;

import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.workshop.portfolio.Portfolio;
import io.vertx.workshop.portfolio.PortfolioService;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PortfolioVerticleTest {

  @Test
  public void testServiceAccess() {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(PortfolioVerticle.class.getName());

    PortfolioService proxy = ProxyHelper.createProxy(PortfolioService.class, vertx, PortfolioService.ADDRESS);

    assertThat(proxy).isNotNull();
    AtomicReference<Portfolio> reference = new AtomicReference<>();
    proxy.getPortfolio(ar -> reference.set(ar.result()));

    await().untilAtomic(reference, not(nullValue()));

    vertx.close();
  }

}