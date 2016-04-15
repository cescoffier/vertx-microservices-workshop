package io.vertx.workshop.common;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Test the future chaining composition.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ChainTest {

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown() {
    vertx.close();
  }


  @Test
  public void testChainingTwoOperations(TestContext context) {
    Async async = context.async();
    Future<Integer> chain = Chain.chain(1, this::opReturningFuture, this::opReturningFuture);
    chain.setHandler(result -> {
      if (result.failed()) {
        context.fail(result.cause());
      } else {
        context.assertEquals(3, result.result());
        async.complete();
      }
    });
  }

  @Test
  public void testChainingThreeOperations(TestContext context) {
    Async async = context.async();
    Future<Integer> chain = Chain.chain(1, this::opReturningFuture, this::opReturningFuture, this::opReturningFuture);
    chain.setHandler(result -> {
      if (result.failed()) {
        context.fail(result.cause());
      } else {
        context.assertEquals(4, result.result());
        async.complete();
      }
    });
  }

  @Test
  public void testChainingThreeOperationsWithFutureAsInput(TestContext context) {
    Async async = context.async();
    Future<Integer> future = Future.future();
    Future<Integer> chain = Chain.chain(future,
        this::opReturningFuture, this::opReturningFuture, this::opReturningFuture);
    chain.setHandler(result -> {
      if (result.failed()) {
        context.fail(result.cause());
      } else {
        context.assertEquals(4, result.result());
        async.complete();
      }
    });

    future.complete(1);
  }

  @Test
  public void testChainingThreeOperationsWithFailedFutureAsInput(TestContext context) {
    Async async = context.async();
    Future<Integer> future = Future.future();
    Future<Integer> chain = Chain.chain(future,
        this::opNotExecuted, this::opReturningFuture, this::opReturningFuture);
    chain.setHandler(result -> {
      if (result.failed()) {
        context.assertFalse(spyNotExecuted.get());
        context.assertTrue(result.cause().getMessage().contains("D'oh"));
        async.complete();
      } else {
        context.fail("Failure expected");
      }
    });

    future.fail("D'oh !");
  }

  @Test
  public void testChainingThreeOperationsReturningFuturesWithFailure(TestContext context) {
    Async async = context.async();
    Future<Integer> chain = Chain.chain(1, this::opReturningFuture, this::opReturningAFailedFuture, this::opReturningFuture);
    chain.setHandler(result -> {
      if (result.failed()) {
        context.assertTrue(result.cause().getMessage().contains("D'oh"));
        async.complete();
      } else {
        context.fail("Failure expected");

      }
    });
  }

  @Test
  public void testChainingThreeOperationsReturningFuturesWithFailureInterruption(TestContext context) {
    Async async = context.async();
    Future<Integer> chain = Chain.chain(1, this::opReturningFuture, this::opReturningAFailedFuture, this::opNotExecuted);
    chain.setHandler(result -> {
      if (result.failed()) {
        context.assertFalse(spyNotExecuted.get());
        context.assertTrue(result.cause().getMessage().contains("D'oh"));
        async.complete();
      } else {
        context.fail("Failure expected");

      }
    });
  }

  private Future<Integer> opReturningFuture(int input) {
    Future<Integer> future = Future.future();
    vertx.runOnContext(v -> future.complete(input + 1));
    return future;
  }

  private AtomicBoolean spyNotExecuted = new AtomicBoolean(false);

  private Future<Integer> opNotExecuted(int input) {
    spyNotExecuted.set(true);
    Future<Integer> future = Future.future();
    vertx.runOnContext(v -> future.complete(input + 1));
    return future;
  }

  private Future<Integer> opReturningAFailedFuture(int input) {
    Future<Integer> future = Future.future();
    vertx.runOnContext(v -> future.fail("D'oh !"));
    return future;
  }

  @Test
  public void chainingWithDifferentTypes(TestContext context) {
    Async async = context.async();
    Future<Buffer> chain = Chain.chain(1,
        input -> {
          Future<String> future = Future.future();
          vertx.runOnContext(v -> future.complete(Integer.toString(input)));
          return future;
        },
        string -> {
          Future<String> future = Future.future();
          vertx.runOnContext(v -> future.complete("hello " + string));
          return future;
        },
        string -> {
          Future<Buffer> future = Future.future();
          vertx.runOnContext(v -> future.complete(Buffer.buffer(string)));
          return future;
        }
    );

    chain.setHandler(ar -> {
      if (ar.failed()) {
        context.fail(ar.cause());
      } else {
        String r = ar.result().toString();
        context.assertEquals(r, "hello 1");
        async.complete();
      }
    });
  }

}