package io.vertx.workshop.common;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Launcher extends io.vertx.core.Launcher {

  public static void main(String[] args) {
    // TODO this should not be required, but the beforeStartingVertx method does not yet allow setting whether we
    // start in cluster mode. This is obviously a bug.
    List<String> list = new ArrayList<>(Arrays.asList(args));

    if (list.isEmpty()) {
      list.add("-cluster");
    }

    new Launcher().dispatch(list.toArray(new String[list.size()]));
  }

  @Override
  public void beforeStartingVertx(VertxOptions options) {
    options.setClusterHost("127.0.0.1");
  }

  @Override
  public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
    super.beforeDeployingVerticle(deploymentOptions);

    if (deploymentOptions.getConfig() == null) {
      deploymentOptions.setConfig(new JsonObject());
    }

    File conf = new File("src/conf/config.json");
    deploymentOptions.getConfig().mergeIn(getConfiguration(conf));
  }

  private JsonObject getConfiguration(File config) {
    JsonObject conf = new JsonObject();
    if (config.isFile()) {
      System.out.println("Reading config file: " + config.getAbsolutePath());
      try (Scanner scanner = new Scanner(config).useDelimiter("\\A")) {
        String sconf = scanner.next();
        try {
          conf = new JsonObject(sconf);
        } catch (DecodeException e) {
          System.err.println("Configuration file " + sconf + " does not contain a valid JSON object");
        }
      } catch (FileNotFoundException e) {
        // Ignore it.
      }
    } else {
      System.out.println("Config file not found " + config.getAbsolutePath());
    }
    return conf;
  }
}
