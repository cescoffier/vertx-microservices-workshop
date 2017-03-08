package io.vertx.workshop.portfolio.impl;

import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.workshop.common.MicroServiceVerticle;
import io.vertx.workshop.portfolio.PortfolioService;

import static io.vertx.workshop.portfolio.PortfolioService.ADDRESS;
import static io.vertx.workshop.portfolio.PortfolioService.EVENT_ADDRESS;

/**
 * A verticle publishing the portfolio service.
 */
public class PortfolioVerticle extends MicroServiceVerticle {

  @Override
  public void start() {
    super.start();

    // Create the service object
    PortfolioServiceImpl service = new PortfolioServiceImpl(vertx, discovery, config().getDouble("money", 10000.00));

    // Register the service proxy on the event bus
    ProxyHelper.registerService(PortfolioService.class, vertx, service, ADDRESS);

    // Publish it in the discovery infrastructure
    publishEventBusService("portfolio", ADDRESS, PortfolioService.class, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        System.out.println("Portfolio service published : " + ar.succeeded());
      }
    });

    //----
    // The portfolio event service
    publishMessageSource("portfolio-events", EVENT_ADDRESS, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        System.out.println("Portfolio Events service published : " + ar.succeeded());
      }
    });
    //----
  }
}
