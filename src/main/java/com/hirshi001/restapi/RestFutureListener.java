package com.hirshi001.restapi;

@FunctionalInterface
public interface RestFutureListener<T, U> {

    void success(RestFuture<T, U> future);

    default void failure(RestFuture<T, U> future){}

    default void cancelled(RestFuture<T, U> future){}

}
