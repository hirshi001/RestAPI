package com.hirshi001.restapi;


import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

public interface RestFuture<T, U> extends Future<U>{


    static <T> RestFuture<T, T> create() {
        return create(DefaultRestFuture.DEFAULT_EXECUTOR, true, (RestFutureConsumer<T, T>) null);
    }

    static <T> RestFuture<T, T> create(ScheduledExecutorService executor) {
        return create(executor, true, (RestFutureConsumer<T, T>) null);
    }

    static <T> RestFuture<T, T> create(RestFutureConsumer<T, T> consumer) {
        return create(DefaultRestFuture.DEFAULT_EXECUTOR, consumer);
    }

    static <T> RestFuture<T, T> create(ScheduledExecutorService executor, RestFutureConsumer<T, T> consumer) {
        return create(executor, true, consumer);
    }

    static <T> RestFuture<T, T> create(ScheduledExecutorService executor, boolean cancel, RestFutureConsumer<T, T> consumer) {
        return new DefaultRestFuture<>(executor, cancel, consumer, null);
    }

    static <T> RestFuture<T, T> create(Callable<T> action) {
        return create(DefaultRestFuture.DEFAULT_EXECUTOR, true, action);
    }

    static <T> RestFuture<T, T> create(ScheduledExecutorService executor, Callable<T> action) {
        return create(executor, true, action);
    }

    static <T> RestFuture<T, T> create(ScheduledExecutorService executor, boolean cancel, Callable<T> action) {
        return new DefaultRestFuture<>(executor, cancel, (future, input) -> {
            try {
                T result = action.call();
                future.taskFinished(result);
            } catch (Exception e) {
                future.setCause(e);
            }
        }, null);
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
    RestFuture<T, U> addListener(RestFutureListener listener);

    /**
     *
     * @param executor the executor to call any methods in the executor
     * @param listener the listener to be notified when the task is finished, cancelled or failed
     * @return this for chaining
     * @throws NullPointerException if executor or listener is null
     */
    RestFuture<T, U> addListener(Executor executor, RestFutureListener listener);

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
