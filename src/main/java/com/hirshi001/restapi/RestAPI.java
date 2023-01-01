package com.hirshi001.restapi;

import java.util.concurrent.Callable;

public class RestAPI {

    private static RestFutureFactory factory;

    public static void setFactory(RestFutureFactory factory){
        RestAPI.factory = factory;
    }


    public static <T> RestFuture<T, T> create() {
        return factory.create();
    }

    public static <T> RestFuture<T, T> create(ScheduledExec executor) {
        return factory.create(executor);
    }

    public static <T> RestFuture<T, T> create(RestFutureConsumer<T, T> consumer) {
        return factory.create(consumer);
    }

    public static <T> RestFuture<T, T> create(ScheduledExec executor, RestFutureConsumer<T, T> consumer) {
        return factory.create(executor, consumer);
    }

    public static <T> RestFuture<T, T> create(ScheduledExec executor, boolean cancel, RestFutureConsumer<T, T> consumer) {
        return factory.create(executor, cancel, consumer);
    }

    public static <T> RestFuture<T, T> create(Callable<T> action) {
        return factory.create(action);
    }

    public static <T> RestFuture<T, T> create(ScheduledExec executor, Callable<T> action) {
        return factory.create(executor, action);
    }

    public static <T> RestFuture<T, T> create(ScheduledExec executor, boolean cancel, Callable<T> action) {
        return factory.create(executor, cancel, action);
    }


}
