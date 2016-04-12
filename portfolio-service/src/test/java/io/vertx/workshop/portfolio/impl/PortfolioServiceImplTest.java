package io.vertx.workshop.portfolio.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.workshop.portfolio.Portfolio;
import io.vertx.workshop.portfolio.PortfolioService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class PortfolioServiceImplTest {

  private Vertx vertx;
  private PortfolioService service;
  private Portfolio original;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();

    Async async = tc.async();
    vertx.deployVerticle(PortfolioVerticle.class.getName(), id -> {
      service = ProxyHelper.createProxy(PortfolioService.class, vertx, PortfolioService.ADDRESS);
      service.getPortfolio(ar -> {
        if (!ar.succeeded()) {
          System.out.println(ar.cause());
        }
        tc.assertTrue(ar.succeeded());
        original = ar.result();
        async.complete();
      });
    });
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  @Test
  public void testBuyStocks(TestContext tc) {
    Async async = tc.async();
    service.buy(10, quote("A", 10, 20, 100), ar -> {
      tc.assertTrue(ar.succeeded());
      Portfolio portfolio = ar.result();
      tc.assertEquals(portfolio.getAmount("A"), 10);
      tc.assertEquals(portfolio.getAmount("B"), 0);
      tc.assertEquals(portfolio.getCash(), original.getCash() - 10 * 10);
      async.complete();
    });
  }

  @Test
  public void testBuyAndSell(TestContext tc) {
    Async async = tc.async();
    service.buy(10, quote("A", 10, 20, 100), ar -> {
      tc.assertTrue(ar.succeeded());
      Portfolio portfolio = ar.result();
      tc.assertEquals(portfolio.getAmount("A"), 10);
      tc.assertEquals(portfolio.getAmount("B"), 0);
      tc.assertEquals(portfolio.getCash(), original.getCash() - 10 * 10);

      // Sell the bought stocks immediately
      service.sell(5, quote("A", 10, 20, 100), ar2 -> {
        tc.assertTrue(ar2.succeeded());
        Portfolio portfolio2 = ar2.result();
        tc.assertEquals(portfolio2.getAmount("A"), 5);
        tc.assertEquals(portfolio2.getAmount("B"), 0);
        tc.assertEquals(portfolio2.getCash(), portfolio.getCash() + 5 * 20);
        async.complete();
      });
    });
  }

  @Test
  public void testThatYouCannotBuyIfYouRunOutOfMoney(TestContext tc) {
    Async async = tc.async();
    service.buy(10000, quote("A", 10, 20, 100000), ar -> {
      tc.assertTrue(ar.failed());
      tc.assertTrue(ar.cause().getMessage().contains("not enough money"));
      async.complete();
    });
  }

  @Test
  public void testThatYouCannotBuyIfThereIsNotEnoughShare(TestContext tc) {
    Async async = tc.async();
    service.buy(100, quote("A", 10, 20, 10), ar -> {
      tc.assertTrue(ar.failed());
      tc.assertTrue(ar.cause().getMessage().contains("not enough stocks"));
      async.complete();
    });
  }

  @Test
  public void testThatYouCannotSellMoreThanWhatYouOwn(TestContext tc) {
    Async async = tc.async();
    service.buy(100, quote("A", 10, 20, 100), ar -> {
      service.sell(100, quote("A", 10, 20, 0), ar2 -> {
        tc.assertTrue(ar2.succeeded());
        service.sell(1, quote("A", 10, 20, 0), ar3 -> {
          tc.assertTrue(ar3.failed());
          tc.assertTrue(ar3.cause().getMessage().contains("not enough stocks"));
          async.complete();
        });
      });

    });
  }

  @Test
  public void testYouCannotBuyANegativeAmount(TestContext tc) {
    Async async = tc.async();
    service.buy(-1, quote("A", 10, 20, 100), ar -> {
      tc.assertTrue(ar.failed());
      async.complete();
    });
  }

  @Test
  public void testYouCannotSellANegativeAmount(TestContext tc) {
    Async async = tc.async();
    service.sell(-1, quote("A", 10, 20, 100), ar -> {
      tc.assertTrue(ar.failed());
      async.complete();
    });
  }

  private JsonObject quote(String name, double ask, double bid, int available) {
    return new JsonObject()
        .put("name", name)
        .put("ask", ask)
        .put("bid", bid)
        .put("shares", available);
  }

}