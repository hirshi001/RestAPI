package com.hirshi001.restapi;

import java.util.concurrent.Callable;

public interface RestFutureFactory {


    <T> RestFuture<T, T> create();

    <T> RestFuture<T, T> create(ScheduledExec executor);

    <T> RestFuture<T, T> create(RestFutureConsumer<T, T> consumer);

    <T> RestFuture<T, T> create(ScheduledExec executor, RestFutureConsumer<T, T> consumer);

    <T> RestFuture<T, T> create(ScheduledExec executor, boolean cancel, RestFutureConsumer<T, T> consumer);

    <T> RestFuture<T, T> create(Callable<T> action);

    <T> RestFuture<T, T> create(ScheduledExec executor, Callable<T> action);

     <T> RestFuture<T, T> create(ScheduledExec executor, boolean cancel, Callable<T> action);

    ScheduledExec getDefaultExecutor();


}
