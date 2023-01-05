package com.hirshi001.restapi;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public interface RestFuture<T, U> extends Future<U> {

    public ScheduledExec getScheduledExec();

    public void setExecutor(ScheduledExec executor);

    public Throwable cause();

    public void setCause(Throwable cause);

    @Override
    boolean cancel(boolean mayInterruptIfRunning);

    @Override
    boolean isCancelled();

    @Override
    boolean isDone();

    boolean isSuccess();

    boolean isFailure();

    /**
     *
     * @return this for chaining
     */
    RestFuture<T, U> perform();

    /**
     *
     * @return this for chaining
     */
    RestFuture<T, U> performAsync();

    /**
     *
     * @return this for chaining
     */
    RestFuture<T, U> perform(T input);

    /**
     *
     * @return this for chaining
     */
    RestFuture<T, U> performAsync(T input);

    RestFuture<U, U> then(Consumer<U> consumer);

    RestFuture<U, U> then(RestFuture<U, ?> future);

    RestFuture<U, U> thenAsync(Consumer<U> consumer);

    RestFuture<U, U> thenAsync(RestFuture<U, ?> future);

    RestFuture<U, U> thenAsync(Consumer<U> consumer, ScheduledExec executor);

    RestFuture<U, U> thenBoth(RestFuture<U, ?> first, RestFuture<U, ?> second);

    RestFuture<U, U> thenBothAsync(RestFuture<U, ?> first, RestFuture<U, ?> second);

    RestFuture<U, U> thenBothAsync(RestFuture<U, ?> first, RestFuture<U, ?> second, ScheduledExec executor);

    <B> RestFuture<U, B> map(Function<U, B> function);

    <B> RestFuture<U, B> mapAsync(Function<U, B> function);

    <B> RestFuture<U, B> mapAsync(Function<U, B> function, ScheduledExec executor);

    RestFuture<U, U> pauseFor(long timeout);

    RestFuture<U, U> pauseFor(long timeout, TimeUnit unit);

    @Override
    U get() throws InterruptedException, ExecutionException;

    @Override
    U get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     *
     * @param listener the listener to be notified when the task is finished, cancelled or failed
     * @return this for chaining
     * @throws NullPointerException if listener is null
     */
    RestFuture<T, U> addListener(RestFutureListener listener);

    /**
     *
     * @param executor the executor to call any methods in the executor
     * @param listener the listener to be notified when the task is finished, cancelled or failed
     * @return this for chaining
     * @throws NullPointerException if executor or listener is null
     */
    RestFuture<T, U> addListener(ScheduledExec executor, RestFutureListener listener);

    /**
     *
     * @param listener the listener to remove
     * @return true if the listener was removed, false if the listener was not registered
     */
    boolean removeListener(RestFutureListener listener);

    /**
     * Removes all listeners from this portion of the RestFuture
     * @return this for chaining
     */
    RestFuture<T, U> removeAllListeners();

    void taskFinished(U result);

    RestFuture<T, U> onFailure(Consumer<Throwable> consumer);

}
