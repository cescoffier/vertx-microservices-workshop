/*
This is a Groovy verticle implemented as a _script_. To the content of this script is the `start` method of the
 verticle.
 */

import io.vertx.groovy.core.CompositeFuture
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.eventbus.MessageConsumer
import io.vertx.groovy.servicediscovery.types.EventBusService;
import io.vertx.groovy.servicediscovery.types.MessageSource;
import io.vertx.groovy.servicediscovery.ServiceDiscovery
import io.vertx.workshop.portfolio.PortfolioService
import io.vertx.workshop.trader.impl.TraderUtils

def company = TraderUtils.pickACompany();
def numberOfShares = TraderUtils.pickANumber();

println("Groovy compulsive trader configured for company " + company + " and shares: " + numberOfShares);

// We create the discovery service object.
def discovery = ServiceDiscovery.create(vertx);

Future<MessageConsumer<Map>> marketFuture = Future.future();
Future<PortfolioService> portfolioFuture = Future.future();

MessageSource.getConsumer(discovery,
        ["name" : "market-data"], marketFuture.completer());
EventBusService.getProxy(discovery,
        "io.vertx.workshop.portfolio.PortfolioService", portfolioFuture.completer());

// When done (both services retrieved), execute the handler
CompositeFuture.all(marketFuture, portfolioFuture).setHandler( { ar ->
  if (ar.failed()) {
    System.err.println("One of the required service cannot be retrieved: " + ar.cause());
  } else {
    // Our services:
    PortfolioService portfolio = portfolioFuture.result();
    MessageConsumer<Map> marketConsumer = marketFuture.result();

    // Listen the market...
    marketConsumer.handler( { message ->
      Map quote = message.body();
      TraderUtils.dumbTradingLogic(company, numberOfShares, portfolio, quote);
    });
  }
});

