package io.vertx.workshop.trader.impl

import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.types.EventBusService
import io.vertx.servicediscovery.types.MessageSource
import io.vertx.workshop.portfolio.PortfolioService

class KotlinCompulsiveTraderVerticle : io.vertx.core.AbstractVerticle() {
  override fun start() {

    val company = TraderUtils.pickACompany()
    val numberOfShares = TraderUtils.pickANumber()

    System.out.println("Groovy compulsive trader configured for company $company and shares: $numberOfShares");

    // We create the discovery service object.
    val discovery = ServiceDiscovery.create(vertx)

    val marketFuture: Future<MessageConsumer<JsonObject>> = Future.future()
    val portfolioFuture: Future<PortfolioService> = Future.future()

    MessageSource.getConsumer<JsonObject>(discovery, JsonObject().put("name", "market-data"), marketFuture)
    EventBusService.getProxy<PortfolioService>(discovery, PortfolioService::class.java, portfolioFuture)

    // When done (both services retrieved), execute the handler
    CompositeFuture.all(marketFuture, portfolioFuture).setHandler { ar ->
      if (ar.failed()) {
        System.err.println("One of the required service cannot be retrieved: ${ar.cause().message}");
      } else {
        // Our services:
        val portfolio = portfolioFuture.result();
        val marketConsumer = marketFuture.result();

        // Listen the market...
        marketConsumer.handler { message ->
          val quote = message.body();
          TraderUtils.dumbTradingLogic(company, numberOfShares, portfolio, quote)
        }
      }
    }
  }
}
