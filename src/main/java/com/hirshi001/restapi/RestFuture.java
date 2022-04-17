package com.hirshi001.restapi;


import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

public interface RestFuture<T, U> extends Future<U>, Consumer<T>{

    static <T> RestFuture<T, T> create(ScheduledExecutorService executor) {
        return new DefaultRestFuture<>(executor);
    }

    static <T> RestFuture<T, T> create() {
        return new DefaultRestFuture<>();
    }

    public ScheduledExecutorService getExecutor();

    public void setExecutor(ScheduledExecutorService executor);

    public Throwable cause();

    public void setCause(Throwable cause);

    @Override
    boolean cancel(boolean mayInterruptIfRunning);

    @Override
    boolean isCancelled();

    @Override
    boolean isDone();

    @Override
    void accept(T t);

    RestFuture<U, U> then(Consumer<U> consumer);

    RestFuture<U, U> thenAsync(Consumer<U> consumer);

    RestFuture<U, U> thenAsync(Consumer<U> consumer, Executor executor);

    RestFuture<U, U> thenBoth(RestFuture<U, ?> first, RestFuture<U, ?> second);

    RestFuture<U, U> thenBothAsync(RestFuture<U, ?> first, RestFuture<U, ?> second);

    RestFuture<U, U> thenBothAsync(RestFuture<U, ?> first, RestFuture<U, ?> second, Executor executor);

    <B> RestFuture<U, B> map(Function<U, B> function);

    <B> RestFuture<U, B> mapAsync(Function<U, B> function);

    <B> RestFuture<U, B> mapAsync(Function<U, B> function, Executor executor);

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
    RestFuture<T, U> addListener(RestFutureListener<T, U> listener);

    /**
     *
     * @param executor the executor to call any methods in the executor
     * @param listener the listener to be notified when the task is finished, cancelled or failed
     * @return this for chaining
     * @throws NullPointerException if executor or listener is null
     */
    RestFuture<T, U> addListener(Executor executor, RestFutureListener<T, U> listener);

    /**
     *
     * @param listener the listener to remove
     * @return true if the listener was removed, false if the listener was not registered
     */
    boolean removeListener(RestFutureListener<T, U> listener);

    /**
     * Removes all listeners from this portion of the RestFuture
     * @return this for chaining
     */
    RestFuture<T, U> removeAllListeners();

    void taskFinished(U result);




}
