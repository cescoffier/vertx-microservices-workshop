package io.vertx.workshop.common;

import io.vertx.core.Future;

import java.util.function.Function;

/**
 * Utility class to chain {@link Future}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Chain {

  /**
   * Chains the two given operations.
   *
   * @param input      the initial input
   * @param operation1 the first operation, it's a {@link Function} returning a {@link Future}, the input is {@code
   *                   input}.
   * @param operation2 the second operation, it's a {@link Function} returning a {@link Future}, the input is the
   *                   result of the first operation
   * @param <R>        the type of the final {@link Future}, so the type returned by the second operation.
   * @param <A>        the input type
   * @param <B>        the intermediate type (output of the first operation, input of the second one)
   * @return a future denoting the completion of the chain. If one of the operation fails, the returned
   * future is marked as failed.
   */
  public static <R, A, B> Future<R> chain(A input, Function<A, Future<B>> operation1,
                                          Function<B, Future<R>> operation2) {
    Future<R> future = Future.future();
    operation1.apply(input).setHandler(ar -> {
      if (ar.failed()) {
        future.fail(ar.cause());
      } else {
        operation2.apply(ar.result()).setHandler(future);
      }
    });
    return future;
  }

  /**
   * Chains the three given operations.
   *
   * @param input      the initial input
   * @param operation1 the first operation, it's a {@link Function} returning a {@link Future}, the input is {@code
   *                   input}.
   * @param operation2 the second operation, it's a {@link Function} returning a {@link Future}, the input is the
   *                   result of the first operation
   * @param operation3 the second operation, it's a {@link Function} returning a {@link Future}, the input is the
   *                   result of the second operation
   * @param <R>        the type of the final {@link Future}, so the type returned by the second operation.
   * @param <A>        the input type
   * @param <B>        the intermediate type (output of the first operation, input of the second one)
   * @param <C>        the second intermediate type (output of the second operation, input of the third one)
   * @return a future denoting the completion of the chain. If one of the operation fails, the returned
   * future is marked as failed.
   */
  public static <R, A, B, C> Future<R> chain(A input,
                                             Function<A, Future<B>> operation1,
                                             Function<B, Future<C>> operation2,
                                             Function<C, Future<R>> operation3) {
    Future<R> future = Future.future();

    operation1.apply(input).setHandler(ar -> {
      if (ar.failed()) {
        future.fail(ar.cause());
      } else {
        operation2.apply(ar.result()).setHandler(ar2 -> {
              if (ar2.failed()) {
                future.fail(ar2.cause());
              } else {
                operation3.apply(ar2.result()).setHandler(future);
              }
            }
        );
      }
    });
    return future;
  }

  /**
   * Chains the three given operations. The chain is executed when the input {@link Future} is completed successfully.
   * If the input {@link Future} is a failure, the chain is not executed, and the return {@link Future} is makred as
   * failed.
   *
   * @param input      the {@link Future} triggering the chain on completion
   * @param operation1 the first operation, it's a {@link Function} returning a {@link Future}, the input is {@code
   *                   input}.
   * @param operation2 the second operation, it's a {@link Function} returning a {@link Future}, the input is the
   *                   result of the first operation
   * @param operation3 the second operation, it's a {@link Function} returning a {@link Future}, the input is the
   *                   result of the second operation
   * @param <R>        the type of the final {@link Future}, so the type returned by the second operation.
   * @param <A>        the input type
   * @param <B>        the intermediate type (output of the first operation, input of the second one)
   * @param <C>        the second intermediate type (output of the second operation, input of the third one)
   * @return a future denoting the completion of the chain. If one of the operation fails, the returned
   * future is marked as failed.
   */
  public static <R, A, B, C> Future<R> chain(Future<A> input,
                                             Function<A, Future<B>> operation1,
                                             Function<B, Future<C>> operation2,
                                             Function<C, Future<R>> operation3) {
    Future<R> future = Future.future();

    input.setHandler(arg -> {
      if (arg.failed()) {
        future.fail(arg.cause());
      } else {
        Future<R> chain = chain(arg.result(), operation1, operation2, operation3);
        chain.setHandler(future);
      }
    });


    return future;
  }

  /**
   * Chains the two given operations. The chain is executed when the input {@link Future} is completed successfully.
   * If the input {@link Future} is a failure, the chain is not executed, and the return {@link Future} is makred as
   * failed.
   *
   * @param input      the {@link Future} triggering the chain on completion
   * @param operation1 the first operation, it's a {@link Function} returning a {@link Future}, the input is {@code
   *                   input}.
   * @param operation2 the second operation, it's a {@link Function} returning a {@link Future}, the input is the
   *                   result of the first operation
   * @param <R>        the type of the final {@link Future}, so the type returned by the second operation.
   * @param <A>        the input type
   * @param <B>        the intermediate type (output of the first operation, input of the second one)
   * @return a future denoting the completion of the chain. If one of the operation fails, the returned
   * future is marked as failed.
   */
  public static <R, A, B> Future<R> chain(Future<A> input,
                                          Function<A, Future<B>> operation1,
                                          Function<B, Future<R>> operation2) {
    Future<R> future = Future.future();

    input.setHandler(arg -> {
      if (arg.failed()) {
        future.fail(arg.cause());
      } else {
        Future<R> chain = chain(arg.result(), operation1, operation2);
        chain.setHandler(future);
      }
    });


    return future;
  }

}
