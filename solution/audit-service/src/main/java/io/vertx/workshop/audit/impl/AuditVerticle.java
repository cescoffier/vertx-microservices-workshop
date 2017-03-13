package io.vertx.workshop.audit.impl;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import io.vertx.rxjava.ext.sql.SQLConnection;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.servicediscovery.types.MessageSource;
import io.vertx.workshop.common.RxMicroServiceVerticle;
import rx.Single;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A verticle storing operations in a database (hsql) and providing access to the operations.
 */
public class AuditVerticle extends RxMicroServiceVerticle {

  private static final String DROP_STATEMENT = "DROP TABLE IF EXISTS AUDIT";
  private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS AUDIT (id INTEGER IDENTITY, operation varchar(250))";
  private static final String INSERT_STATEMENT = "INSERT INTO AUDIT (operation) VALUES ?";
  private static final String SELECT_STATEMENT = "SELECT * FROM AUDIT ORDER BY ID DESC LIMIT 10";

  private JDBCClient jdbc;

  /**
   * Starts the verticle asynchronously. The the initialization is completed, it calls
   * `complete()` on the given {@link Future} object. If something wrong happens,
   * `fail` is called.
   *
   * @param future the future to indicate the completion
   */
  @Override
  public void start(Future<Void> future) {
    super.start();

    // creates the jdbc client.
    jdbc = JDBCClient.createNonShared(vertx, config());

    // ----
    Single<Void> databaseReady = initializeDatabase(config().getBoolean("drop", false));
    Single<Void> httpEndpointReady = configureTheHTTPServer()
        .flatMap(server -> rxPublishHttpEndpoint("audit", "localhost", server.actualPort()));
    Single<MessageConsumer<JsonObject>> messageConsumerReady = retrieveThePortfolioMessageSource();
    Single<MessageConsumer<JsonObject>> readySingle = Single.zip(
        databaseReady,
        httpEndpointReady,
        messageConsumerReady,
        (db, http, consumer) -> consumer);
    // ----

    readySingle.doOnSuccess(consumer -> {
      // on success we set the handler that will store message in the database
      consumer.handler(message -> storeInDatabase(message.body()));
    }).subscribe(consumer -> {
      // complete the verticle start with a success
      future.complete();
    }, error -> {
      // signal a verticle start failure
      future.fail(error);
    });
  }

  @Override
  public void stop(Future<Void> future) throws Exception {
    jdbc.close();
    super.stop(future);
  }

  private void retrieveOperations(RoutingContext context) {
    // We retrieve the operation using the following process:
    // 1. Get the connection
    // 2. When done, execute the query
    // 3. When done, iterate over the result to build a list
    // 4. close the connection
    // 5. return this list in the response

    // ----
    // 1 - we retrieve the connection
    Single<List<JsonObject>> result = jdbc.rxGetConnection().flatMap(
        conn -> conn
            // 2. we execute the query
            .rxQuery(SELECT_STATEMENT)
            // 3. Build the list of operations
            .map(set -> set.getRows()
                .stream()
                .map(json -> new JsonObject(json.getString("OPERATION")))
                .collect(Collectors.toList()))
            // 4. Close the connection
            .doAfterTerminate(conn::close));

    result.subscribe(operations -> {
      // 5. Send the list to the response
      context.response()
          .setStatusCode(200)
          .end(Json.encodePrettily(operations));
    }, context::fail);
    // ----
  }

  private Single<HttpServer> configureTheHTTPServer() {
    //----
    // Use a Vert.x Web router for this REST API.
    Router router = Router.router(vertx);
    router.get("/").handler(this::retrieveOperations);
    HttpServer server = vertx.createHttpServer().requestHandler(router::accept);
    Integer port = config().getInteger("http.port", 0);
    return server.rxListen(port);
    //----
  }

  private Single<MessageConsumer<JsonObject>> retrieveThePortfolioMessageSource() {
    return MessageSource.rxGetConsumer(discovery, new JsonObject().put("name", "portfolio-events"));
  }


  private void storeInDatabase(JsonObject operation) {
    // Storing in the database is also a multi step process,
    // 1. need to retrieve a connection
    // 2. execute the insertion statement
    // 3. close the connection

    // Step 1 get the connection
    Single<SQLConnection> connectionRetrieved = jdbc.rxGetConnection();

    // Step 2, when the connection is retrieved (this may have failed), do the insertion (upon success)
    Single<UpdateResult> update = connectionRetrieved.flatMap(connection -> connection
        .rxUpdateWithParams(INSERT_STATEMENT, new JsonArray().add(operation.encode()))

        // Step 3, when the insertion is done, close the connection.
        .doAfterTerminate(connection::close));

    update.subscribe(result -> {
      // Ok
    }, err -> {
      System.err.println("Failed to insert operation in database: " + err);
    });
  }

  private Single<Void> initializeDatabase(boolean drop) {

    // The database initialization is a multi-step process:
    // 1. Retrieve the connection
    // 2. Drop the table is exist
    // 3. Create the table
    // 4. Close the connection (in any case)
    // To handle such a process, we are going to create an RxJava Single and compose it with the RxJava flatMap operation:
    // retrieve the connection -> drop table -> create table -> close the connection
    // For this we use `Func1<X, Single<R>>`that takes a parameter `X` and return a `Single<R>` object.

    // This is the starting point of our Rx operations
    // This single will be completed when the connection with the database is established.
    // We are going to use this single as a reference on the connection to close it.
    Single<SQLConnection> connectionRetrieved = jdbc.rxGetConnection();

    // Ok, now it's time to chain all these actions:
    Single<List<Integer>> resultSingle = connectionRetrieved
        .flatMap(conn -> {
          // When the connection is retrieved

          // Prepare the batch
          List<String> batch = new ArrayList<>();
          if (drop) {
            // When the table is dropped, we recreate it
            batch.add(DROP_STATEMENT);
          }
          // Just create the table
          batch.add(CREATE_TABLE_STATEMENT);

          // We compose with a statement batch
          Single<List<Integer>> next = conn.rxBatch(batch);

          // Whatever the result, if the connection has been retrieved, close it
          return next.doAfterTerminate(conn::close);
        });

    return resultSingle.map(list -> null);
  }
}
