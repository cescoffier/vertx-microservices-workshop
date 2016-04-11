package io.vertx.workshop.common;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.Record;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Test the importation of services based on docker links.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DockerEnvironmentBridgeTest {

  private static Properties properties;

  @BeforeClass
  public static void load() {
    properties = new Properties();
    properties.put("DISCOVERY_REDIS_ENV_REDIS_VERSION", "3.0.7");
    properties.put("CONSOLIDATION_ENV_JAVA_HOME", "/usr/lib/jvm/java-8-openjdk-amd64");
    properties.put("DISCOVERY_REDIS_PORT_6379_TCP_PORT", "6379");
    properties.put("DISCOVERY_REDIS_ENV_REDIS_DOWNLOAD_URL", "http://download.redis.io/releases/redis-3.0.7.tar.gz");
    properties.put("DISCOVERY_REDIS_ENV_REDIS_DOWNLOAD_SHA1", "e56b4b7e033ae8dbf311f9191cf6fdf3ae974d1c");
    properties.put("CONSOLIDATION_PORT", "tcp://172.17.0.3:8080");
    properties.put("DISCOVERY_REDIS_PORT_6379_TCP_ADDR", "172.17.0.2");
    properties.put("CONSOLIDATION_PORT_8080_TCP", "tcp://172.17.0.3:8080");
    properties.put("CONSOLIDATION_NAME", "/portfolio/CONSOLIDATION");
    properties.put("CONSOLIDATION_PORT_8080_TCP_PORT", "8080");
    properties.put("DISCOVERY_REDIS_NAME", "/portfolio/DISCOVERY_REDIS");
    properties.put("CONSOLIDATION_PORT_8080_TCP_ADDR", "172.17.0.3");
    properties.put("CONSOLIDATION_ENV_JAVA_VERSION", "8u72");
    properties.put("CONSOLIDATION_ENV_LANG", "C.UTF-8");
    properties.put("CONSOLIDATION_ENV_SERVICE_TYPE", "http-endpoint");
    properties.put("DISCOVERY_REDIS_PORT", "tcp://172.17.0.2:6379");
    properties.put("DISCOVERY_REDIS_ENV_GOSU_VERSION", "1.7");
    properties.put("DISCOVERY_REDIS_PORT_6379_TCP_PROTO", "tcp");
    properties.put("CONSOLIDATION_PORT_8080_TCP_PROTO", "tcp");
    properties.put("JAVA_DEBIAN_VERSION", "8u72-b15-1~bpo8+1");
    properties.put("CONSOLIDATION_ENV_JAVA_DEBIAN_VERSION", "8u72-b15-1~bpo8+1");
    properties.put("CONSOLIDATION_ENV_CA_CERTIFICATES_JAVA_VERSION", "20140324");
    properties.put("DISCOVERY_REDIS_PORT_6379_TCP", "tcp://172.17.0.2:6379");

    Properties current = System.getProperties();
    current.putAll(properties);
    System.setProperties(current);
  }

  @AfterClass
  public static void unload() {
    for (Object key : properties.keySet()) {
      System.clearProperty((String) key);
    }
  }

  @Test
  public void test() throws InterruptedException {
    Vertx vertx = Vertx.vertx();
    DiscoveryService service = DiscoveryService.create(vertx);
    service.registerDiscoveryBridge(new DockerEnvironmentBridge(), new JsonObject());

    List<Record> records = new ArrayList<>();

    vertx.setPeriodic(100, l -> {
      if (records.size() >= 2) {
        vertx.cancelTimer(l);
      } else {
        service.getRecords(new JsonObject(), ar -> {
          records.clear();
          records.addAll(ar.result());
        });
      }
    });

    await().until(() -> records.size() == 2);

    for (Record record : records) {
      if (record.getName().equals("CONSOLIDATION")) {
        assertConsolidation(record);
      } else if (record.getName().equals("DISCOVERY_REDIS")) {
        assertRedis(record);
      } else {
        fail("Unexpected record name");
      }
    }

  }

  private void assertRedis(Record record) {
    assertThat(record.getName()).isEqualTo("DISCOVERY_REDIS");
    assertThat(record.getLocation().getString("endpoint")).isEqualToIgnoringCase("tcp://172.17.0.2:6379");
  }

  private void assertConsolidation(Record record) {
    assertThat(record.getName()).isEqualTo("CONSOLIDATION");
    assertThat(record.getLocation().getString("endpoint")).isEqualToIgnoringCase("http://172.17.0.3:8080");
  }

}