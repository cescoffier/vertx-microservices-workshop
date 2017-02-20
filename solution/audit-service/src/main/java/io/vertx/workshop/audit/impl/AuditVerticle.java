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
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.workshop.common.Chain;
import io.vertx.workshop.common.MicroServiceVerticle;

import java.util.List;
import java.util.function.Function;
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

    // ----
    Future<Void> databaseReady = initializeDatabase(config().getBoolean("drop", false));
    Future<MessageConsumer<JsonObject>> messageListenerReady = retrieveThePortfolioMessageSource();
    Future<Void> httpEndpointReady = configureTheHTTPServer().compose(
        server -> {
          Future<Void> regFuture = Future.future();
          publishHttpEndpoint("audit", "localhost", server.actualPort(), regFuture);
          return regFuture;
        }
    );

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

    //----
    // Use a Vert.x Web router for this REST API.
    Router router = Router.router(vertx);
    router.get("/").handler(this::retrieveOperations);

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(config().getInteger("http.port", 0), future);
    //----
    return future;
  }

  private Future<MessageConsumer<JsonObject>> retrieveThePortfolioMessageSource() {
    Future<MessageConsumer<JsonObject>> future = Future.future();
    MessageSource.getConsumer(discovery,
        new JsonObject().put("name", "portfolio-events"),
        future
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

  private Future<Void> initializeDatabase(boolean drop) {

    // The database initialization is a multi-step process:
    // 1. Retrieve the connection
    // 2. Drop the table is exist
    // 3. Create the table
    // 4. Close the connection (in any case)
    // To handle such a process, we are going to create a set of Future we are going to compose as a chain:
    // retrieve the connection -> drop table -> create table -> close the connection
    // For this we use `Function<X, Future<R>>`that takes a parameter `X` and return a `Future<R>` object.

    // This is the returned future to notify of the completion of the whole process
    Future<Void> databaseReady = Future.future();

    // This future will be assigned when the connection with the database is established.
    // We are going to use this future as a reference on the connection to close it.
    Future<SQLConnection> connectionRetrieved = Future.future();
    // Retrieve a connection with the database, report on the databaseReady if failed, or assign the connectionRetrieved
    // future.
    jdbc.getConnection(connectionRetrieved);

    // When the connection is retrieved, we want to drop the table (if drop is set to true)
    Function<SQLConnection, Future<SQLConnection>> dropTable = connection -> {
      Future<SQLConnection> future = Future.future();
      if (!drop) {
        future.complete(connection); // Immediate completion.
      } else {
        connection.execute(DROP_STATEMENT, completer(future, connection));
      }
      return future;
    };

    // When the table is dropped, we recreate it
    Function<SQLConnection, Future<Void>> createTable = connection -> {
      Future<Void> future = Future.future();
      connection.execute(CREATE_TABLE_STATEMENT, future.completer());
      return future;
    };

    // Ok, now it's time to chain all these actions:
    // connectionRetrieved -> dropTable -> createTable -> in all case close the connection

    Chain.chain(connectionRetrieved, dropTable, createTable)
        .setHandler(ar -> {
          // Whatever the result, if the connection has been retrieved, close it
          if (connectionRetrieved.result() != null) {
            connectionRetrieved.result().close();
          }

          // Complete the main future with the result.
          databaseReady.handle(ar);
        });

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
