package io.vertx.workshop.audit.impl;

import io.vertx.core.Future;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.ext.jdbc.JDBCClient;
import io.vertx.rxjava.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.servicediscovery.types.MessageSource;
import io.vertx.workshop.common.RxMicroServiceVerticle;
import rx.Single;
import rx.functions.Func1;

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
    Single<MessageConsumer<JsonObject>> messageListenerReady = retrieveThePortfolioMessageSource();
    Single<Void> httpEndpointReady = configureTheHTTPServer().flatMap(server -> rxPublishHttpEndpoint("audit", "localhost", server.actualPort()));

    Single.zip(httpEndpointReady, databaseReady, messageListenerReady, (a,b,c) -> c)
        .subscribe(ok -> {
            // Register the handle called on messages
            ok.handler(message -> storeInDatabase(message.body()));
            // Notify the completion
            future.complete();
        }, future::fail);
    // ----
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

    return vertx.createHttpServer()
        .requestHandler(router::accept)
        .rxListen(config().getInteger("http.port", 0));
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

    Future<SQLConnection> connectionRetrieved = Future.future();
    Future<UpdateResult> insertionDone = Future.future();

    // Step 1 get the connection
    jdbc.getConnection(connectionRetrieved);

    // Step 2, when the connection is retrieved (this may have failed), do the insertion (upon success)
    connectionRetrieved.setHandler(
        ar -> {
          if (ar.failed()) {
            System.err.println("Failed to insert operation in database: " + ar.cause());
          } else {
            SQLConnection connection = ar.result();
            connection.updateWithParams(INSERT_STATEMENT,
                new JsonArray().add(operation.encode()),
                insertionDone.completer());
          }
        }
    );

    // Step 3, when the insertion is done, close the connection.
    insertionDone.setHandler(
        ar -> connectionRetrieved.result().close()
    );
  }

  private Single<Void> initializeDatabase(boolean drop) {

    // The database initialization is a multi-step process:
    // 1. Retrieve the connection
    // 2. Drop the table is exist
    // 3. Create the table
    // 4. Close the connection (in any case)
    // To handle such a process, we are going to create a set of Future we are going to compose as a chain:
    // retrieve the connection -> drop table -> create table -> close the connection
    // For this we use `Function<X, Future<R>>`that takes a parameter `X` and return a `Future<R>` object.

    // This is the returned future to notify of the completion of the whole process
    // Single<Void> databaseReady = Future.future();

    // This future will be assigned when the connection with the database is established.
    // We are going to use this future as a reference on the connection to close it.
    // Future<SQLConnection> connectionRetrieved = Future.future();
    // Retrieve a connection with the database, report on the databaseReady if failed, or assign the connectionRetrieved
    // future.
    Single<SQLConnection> connectionRetrieved = jdbc.rxGetConnection();

    // When the connection is retrieved, we want to drop the table (if drop is set to true)
    Func1<SQLConnection, Single<SQLConnection>> dropTable = connection -> {
      if (!drop) {
        return Single.just(connection); // Immediate completion.
      } else {
        return connection.rxExecute(DROP_STATEMENT).map(v -> connection);
      }
    };

    // When the table is dropped, we recreate it
    Func1<SQLConnection, Single<Void>> createTable = connection -> connection.rxExecute(CREATE_TABLE_STATEMENT);

    // Ok, now it's time to chain all these actions:
    return connectionRetrieved
        .flatMap(conn ->
            Single.just(conn)
                .flatMap(dropTable)
                .flatMap(createTable)
                // Whatever the result, if the connection has been retrieved, close it
                .doAfterTerminate(conn::close)
        );
  }
}
