package io.vertx.workshop.audit.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.types.MessageSource;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.workshop.common.MicroServiceVerticle;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A verticle storing operations in a database (hsql) and providing access to the operations.
 */
public class AuditVerticle extends MicroServiceVerticle {

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

    // TODO
    // ----
    Future<Void> databaseReady = initializeDatabase(config().getBoolean("drop", false));
    Future<MessageConsumer<JsonObject>> messageListenerReady = retrieveThePortfolioMessageSource();
    Future<HttpServer> httpEndpointReady = configureTheHTTPServer();

    CompositeFuture.all(httpEndpointReady, databaseReady, messageListenerReady)
        .setHandler(ar -> {
          if (ar.succeeded()) {
            // Register the handle called on messages
            messageListenerReady.result().handler(message -> storeInDatabase(message.body()));
            // Notify the completion
            future.complete();
          } else {
            future.fail(ar.cause());
          }
        });

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

    //TODO
    // ----
    // 1 - we retrieve the connection
    jdbc.getConnection(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
      } else {
        SQLConnection connection = ar.result();
        // 2. we execute the query
        connection.query(SELECT_STATEMENT, result -> {
          ResultSet set = result.result();

          // 3. Build the list of operations
          List<JsonObject> operations = set.getRows().stream()
              .map(json -> new JsonObject(json.getString("OPERATION")))
              .collect(Collectors.toList());

          // 4. Close the connection
          connection.close();

          // 5. Send the list to the response
          context.response().setStatusCode(200).end(Json.encodePrettily(operations));


        });
      }
    });
    // ----
  }

  private Future<HttpServer> configureTheHTTPServer() {
    Future<HttpServer> future = Future.future();

    //TODO
    //----
    // Use a Vert.x Web router for this REST API.
    Router router = Router.router(vertx);
    router.get("/").handler(this::retrieveOperations);

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(8080, future.completer());
    //----
    return future;
  }

  private Future<MessageConsumer<JsonObject>> retrieveThePortfolioMessageSource() {
    Future<MessageConsumer<JsonObject>> future = Future.future();
    MessageSource.get(vertx, discovery,
        new JsonObject().put("name", "portfolio-events"),
        future.completer()
    );
    return future;
  }


  private void storeInDatabase(JsonObject operation) {
    // Storing in the database is also a multi step process,
    // 1. need to retrieve a connection
    // 2. execute the insertion statement
    // 3. close the connection

    Future<SQLConnection> connectionRetrieved = Future.future();
    Future<UpdateResult> insertionDone = Future.future();

    // Step 1 get the connection
    jdbc.getConnection(connectionRetrieved.completer());

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

  private Future<Void> initializeDatabase(boolean drop) {

    // The database initialization is a multi-step process:
    // 1. Retrieve the connection
    // 2. Drop the table is exist
    // 3. Create the table
    // 4. Close the connection
    // To handle such a process, we are going to create a set of (Future, Handler) that we compose.

    // This is the returned future to notify of the completion of the whole process
    Future<Void> databaseReady = Future.future();

    // This future will be assigned when the connection with the database is established.
    Future<SQLConnection> connectionRetrieved = Future.future();
    // Retrieve a connection with the database, report on the databaseReady if failed, or assign the connectionRetrieve
    // future.
    jdbc.getConnection(connectionRetrieved.completer());


    // When the connection is retrieve, we want to drop the table (if drop is set to true)
    // First, create a future notifying of the completion of the operation
    Future<SQLConnection> tableDropped = Future.future();
    // Then, define a handler doing this operation.
    Handler<SQLConnection> dropTableHandler = (connection) -> {
      if (!drop) {
        tableDropped.complete(connection); // Immediate completion.
      } else {
        connection.execute(DROP_STATEMENT, completer(tableDropped, connection));
      }
    };

    // When the table is dropped (or skipped), we need to create the table. We use the same pattern
    // First,  create a future notifying of the completion of the operation
    Future<SQLConnection> tableCreated = Future.future();
    // Then define a handler doing this operation
    Handler<SQLConnection> createTableHandler = (connection) -> {
      connection.execute(CREATE_TABLE_STATEMENT,
          completer(tableCreated, connection)
      );
    };

    // Finally, we must close the connection, here is the handler that would do it.
    Handler<SQLConnection> closeConnectionHandler = (connection) -> connection.close(databaseReady.completer());

    // Ok, now it's time to compose all these actions:
    // connectionRetrieved -> dropTable -> createTable -> closeConnection
    // The Future.compose method takes a Handler as first param and a Future as second param
    // If the future on which the composition is done is succeeded, the handler is called, otherwise the failure is
    // reported on the future object (second parameter).

    // connectionRetrieved -> dropTable
    connectionRetrieved.compose(dropTableHandler, databaseReady);
    // dropTable -> createTable
    tableDropped.compose(createTableHandler, databaseReady);
    // createTable -> closeConnection
    tableCreated.compose(closeConnectionHandler, databaseReady);

    return databaseReady;
  }

  /**
   * A utility method returning a `Handler<SQLConnection>`
   *
   * @param future     the future.
   * @param connection the connection
   * @return the handler.
   */
  private static Handler<AsyncResult<Void>> completer(Future<SQLConnection> future, SQLConnection connection) {
    return ar -> {
      if (ar.failed()) {
        future.fail(ar.cause());
      } else {
        future.complete(connection);
      }
    };
  }


}
