package io.vertx.workshop.common;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.spi.DiscoveryBridge;
import io.vertx.ext.discovery.spi.ServiceType;
import io.vertx.ext.discovery.types.HttpEndpoint;
import io.vertx.ext.discovery.types.HttpLocation;
import sun.rmi.runtime.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A discovery bridge importing services from the environment variables exported by Docker when using links.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DockerEnvironmentBridge implements DiscoveryBridge {
  private DiscoveryService discovery;

  private List<Record> records = new ArrayList<>();

  private final static Logger LOGGER = LoggerFactory.getLogger(DockerEnvironmentBridge.class);

  @Override
  public void start(Vertx vertx, DiscoveryService discovery, JsonObject configuration, Handler<AsyncResult<Void>> completionHandler) {
    this.discovery = discovery;

    synchronized (this) {
      lookup(completionHandler);
    }
  }

  private void lookup(Handler<AsyncResult<Void>> completionHandler) {
    Map<String, String> variables = getVariables();

    // Find names
    List<String> links = variables.keySet().stream()
        .filter(key -> key.endsWith("_NAME"))
        .map(key -> extractLinkName(key, variables))
        .filter(key -> key != null)
        .collect(Collectors.toList());

    LOGGER.info("Docker links: " + links);

    for (String link : links) {
      try {
        Record record = createRecord(link,
            variables.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(link + "_"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        LOGGER.info("Record created from link " + link + " : " + record);
        discovery.publish(record, ar -> {
          if (ar.succeeded()) {
            records.add(ar.result());
            LOGGER.info("Service imported from Docker link : " + link + " with endpoint set to "
                + ar.result().getLocation().getString(Record.ENDPOINT));
          } else {
            LOGGER.error("Publication of the docker link " + link + " as a service failed", ar.cause());
          }
        });
      } catch (URISyntaxException e) {
        if (completionHandler != null) {
          completionHandler.handle(Future.failedFuture(e));
        } else {
          throw new IllegalStateException("Cannot extract service record from variables for " + link, e);
        }
      }
    }

    if (completionHandler != null) {
      completionHandler.handle(Future.succeededFuture());
    }
  }

  private String extractLinkName(String key, Map<String, String> variables) {
    // We know it ends with _NAME;
    String name = key.substring(0, key.length() - "_NAME".length());
    if (name.isEmpty()) {
      // Weird case the key is just _NAME
      return null;
    } else {
      String port = name + "_PORT";
      if (variables.containsKey(port)) {
        return name;
      } else {
        // Not a valid link
        return null;
      }
    }
  }

  private Map<String, String> getVariables() {
    LinkedHashMap<String, String> vars = new LinkedHashMap<>();
    vars.putAll(System.getenv());
    System.getProperties().entrySet().stream().forEach(entry -> {
      vars.put(entry.getKey().toString(), entry.getValue().toString());
    });
    return vars;
  }

  @Override
  public void stop(Vertx vertx, DiscoveryService discovery) {
    for (Record record : records) {
      discovery.unpublish(record.getRegistration(), v -> {
      });
    }
  }

  private Record createRecord(String name, Map<String, String> variables) throws URISyntaxException {
    Record record = new Record()
        .setName(name);

    // Add as metadata all entries
    variables.entrySet().stream().forEach(entry -> {
      if (entry.getKey().startsWith(name + "_")) {
        String label = entry.getKey().substring((name + "_").length() + 1);
        record.getMetadata().put(label, entry.getValue());
      }
    });

    String type = variables.get(name + "_ENV_SERVICE_TYPE");
    if (type == null) {
      type = ServiceType.UNKNOWN;
    } else {
      LOGGER.info("Service type for " + name + " : " + type);
    }

    URI url = new URI(variables.get(name + "_PORT"));
    switch (type) {
      case "http-endpoint":
        HttpLocation http = new HttpLocation();
        http.setHost(url.getHost());
        http.setPort(url.getPort());
        if (isTrue(variables, name + "_ENV_SSL")) {
          http.setSsl(true);
        }
        record.setType(HttpEndpoint.TYPE);
        record.setLocation(http.toJson());
        break;
      default:
        JsonObject location = new JsonObject();
        location
            .put("endpoint", url.toString())
            .put("port", url.getPort())
            .put("host", url.getHost())
            .put("proto", url.getScheme());
        record.setType(HttpEndpoint.UNKNOWN);
        record.setLocation(location);
    }

    return record;
  }

  private static boolean isTrue(Map<String, String> labels, String key) {
    return labels != null && "true".equalsIgnoreCase(labels.get(key));
  }
}
