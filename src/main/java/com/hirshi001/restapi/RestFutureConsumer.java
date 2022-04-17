package com.hirshi001.restapi;

@FunctionalInterface
public interface RestFutureConsumer<T, U> {

    public void accept(RestFuture<T, U> future, T input);

}
