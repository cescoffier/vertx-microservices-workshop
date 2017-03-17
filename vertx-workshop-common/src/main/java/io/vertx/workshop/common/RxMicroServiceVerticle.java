package io.vertx.workshop.common;

import io.vertx.core.Verticle;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava.servicediscovery.types.HttpEndpoint;
import io.vertx.rxjava.servicediscovery.types.MessageSource;
import io.vertx.servicediscovery.Record;
import rx.Single;

import java.util.Set;

/**
 * An implementation of {@link Verticle} taking care of the discovery and publication of services.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RxMicroServiceVerticle extends MicroServiceVerticle {

  protected Vertx vertx;
  protected ServiceDiscovery discovery;
  protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

  @Override
  public void start() {
    super.start();
    vertx = Vertx.newInstance(super.vertx);
    discovery = ServiceDiscovery.newInstance(super.discovery);
  }

  public Single<Void> rxPublishHttpEndpoint(String name, String host, int port) {
    Record record = HttpEndpoint.createRecord(name, host, port, "/");
    return rxPublish(record);
  }

  public Single<Void> rxPublishMessageSource(String name, String address) {
    Record record = MessageSource.createRecord(name, address);
    return rxPublish(record);
  }

  private Single<Void> rxPublish(Record record) {
    ObservableFuture<Void> adapter = RxHelper.observableFuture();
    publish(record, adapter.toHandler());
    return adapter.toSingle();
  }
}
