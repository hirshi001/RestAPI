/*
 * Copyright 2023 Hrishikesh Ingle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hirshi001.gwtrestapi;

import com.hirshi001.restapi.RestFuture;
import com.hirshi001.restapi.RestFutureConsumer;
import com.hirshi001.restapi.RestFutureListener;
import com.hirshi001.restapi.ScheduledExec;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A GWT compatible implementation of {@link RestFuture}
 * @param <T> The type of the input
 * @param <U> The type of the output
 *
 * @author Hrishikesh Ingle
 */
@SuppressWarnings({"FieldMayBeFinal", "unchecked"})
public class GWTRestFuture<T, U> implements RestFuture <T, U>{

    private ScheduledExec executor;
    private boolean isCancelled;
    private boolean isDone;
    private boolean isSuccess;
    private final boolean cancellable;
    private U result;
    private Throwable cause;
    private RestFuture<?, T> parent;
    private RestFutureConsumer<T, U> task;
    private Queue<GWTRestFuture<U, ?>> nextFutures;
    private Queue<ListenerExecutor<T, U>> listenerExecutors;
    private Queue<RestFutureListener<T, U>> listeners;
    private Consumer<Throwable> onFailure;


    public GWTRestFuture(ScheduledExec executor, boolean cancellable, RestFutureConsumer<T, U> task, RestFuture<?, T> parent) {
        this.executor = executor;
        this.cancellable = cancellable;
        this.task = task;
        this.parent = parent;
        nextFutures = new LinkedList<>();
        listenerExecutors = new LinkedList<>();
        listeners = new LinkedList<>();
    }


    @Override
    public ScheduledExec getScheduledExec() {
        return executor;
    }

    @Override
    public void setExecutor(ScheduledExec executor) {
        this.executor = executor;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public void setCause(Throwable cause) {
        this.cause = cause;
        this.isDone = true;
        if (onFailure != null) onFailure.accept(cause);
        forEachListener(l -> {
            l.onComplete(this);
            l.failure(GWTRestFuture.this);
        });
        forEachNextFuture(f -> f.setCause(cause));
    }

    @Override
    public RestFuture<T, U> perform() {
        if (parent == null) perform(null);
        else {
            parent.perform();
        }
        return this;
    }

    @Override
    public RestFuture<T, U> performAsync() {
        executor.runDeferred(this::perform);
        return this;
    }

    @Override
    public RestFuture<T, U> perform(Object input) {
        if (task != null) {
            try {
                task.accept(this, (T) input);
            } catch (Exception e) {
                setCause(e);
            }
        } else {
            taskFinished((U) input);
        }
        return this;
    }

    @Override
    public RestFuture<T, U> performAsync(T input) {
        executor.runDeferred(() -> perform(input));
        return this;
    }

    @Override
    public RestFuture<U, U> then(Consumer<U> consumer) {
        return add((r, i) -> {
            consumer.accept(i);
            r.taskFinished(i);
        });
    }

    @Override
    public RestFuture<U, U> then(RestFuture<U, ?> future) {
        return add((r, i) -> {
            future.perform(i);
            r.taskFinished(i);
        });
    }

    @Override
    public RestFuture<U, U> thenAsync(Consumer<U> consumer) {
        return thenAsync(consumer, executor);
    }

    @Override
    public RestFuture<U, U> thenAsync(RestFuture<U, ?> future) {
        return add((r, i) -> {
            future.performAsync(i);
            r.taskFinished(i);
        });
    }

    @Override
    public RestFuture<U, U> thenAsync(Consumer<U> consumer, ScheduledExec executor) {
        return add((r, i) -> {
            executor.runDeferred(() -> {
                consumer.accept(i);
                r.taskFinished(i);
            });
        });
    }

    @Override
    public RestFuture<U, U> thenBoth(RestFuture<U, ?> first, RestFuture<U, ?> second) {
        return add((r, i) -> {
            first.perform(i);
            second.perform(i);
            r.taskFinished(i);
        });
    }

    @Override
    public RestFuture<U, U> thenBothAsync(RestFuture<U, ?> first, RestFuture<U, ?> second) {
        return thenBothAsync(first, second, executor);
    }

    @Override
    public RestFuture<U, U> thenBothAsync(RestFuture<U, ?> first, RestFuture<U, ?> second, ScheduledExec executor) {
        return add((r, i) -> {
            executor.runDeferred(() -> first.perform(i));
            executor.runDeferred(() -> second.perform(i));
            r.taskFinished(i);
        });
    }

    @Override
    public <B> RestFuture<U, B> map(Function<U, B> function) {
        return add((r, i) -> {
            r.taskFinished(function.apply(i));
        });
    }

    @Override
    public <B> RestFuture<U, B> mapAsync(Function<U, B> function) {
        return mapAsync(function, executor);
    }

    @Override
    public <B> RestFuture<U, B> mapAsync(Function<U, B> function, ScheduledExec executor) {
        return add((r, i) -> {
            executor.runDeferred(() -> r.taskFinished(function.apply(i)));
        });
    }

    @Override
    public RestFuture<U, U> pauseFor(long timeout) {
        return pauseFor(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public RestFuture<U, U> pauseFor(long timeout, TimeUnit unit) {
        return add((r, i) -> {
            executor.run(() -> r.taskFinished(i), timeout, unit);
        });
    }

    @Override
    public U get() throws InterruptedException {
        return result;
    }

    @Override
    public U get(long timeout, TimeUnit unit) throws TimeoutException {
        return result;
    }

    @Override
    public RestFuture<T, U> addListener(RestFutureListener listener) {
        if (listener == null) throw new NullPointerException("listener cannot be null");
        listeners.add(listener);
        return this;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public RestFuture<T, U> addListener(ScheduledExec executor, RestFutureListener listener) {
        if (executor == null) throw new NullPointerException("executor cannot be null");
        if (listener == null) throw new NullPointerException("listener cannot be null");
        listenerExecutors.add( new ListenerExecutor<>(executor, listener));
        return this;
    }

    @Override
    public boolean removeListener(RestFutureListener listener) {
        boolean success = listeners.removeIf(l -> l == listener);
        //we need to remove possible duplicate listeners from both lists
        if (listenerExecutors.removeIf(l -> l.listener == listener)) success = true;
        return success;
    }

    @Override
    public RestFuture<T, U> removeAllListeners() {
        listeners.clear();
        listenerExecutors.clear();
        return this;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (cancellable) {
            isCancelled = true;
            forEachListener(l -> {
                l.onComplete(this);
                l.cancelled(this);
            });
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public boolean isFailure() {
        return cause() != null;
    }

    protected <B> RestFuture<U, B> add(RestFutureConsumer<U, B> function) {
        GWTRestFuture<U, B> future = new GWTRestFuture<>(executor, false, function, this);
        nextFutures.add(future);
        return future;
    }

    @Override
    public void taskFinished(U result) {
        this.result = result;
        isDone = true;
        this.isSuccess = true;
        forEachListener(l -> {
            l.onComplete(this);
            l.success(this);
        });
        forEachNextFuture(f -> f.perform(result));
    }

    @Override
    public RestFuture<T, U> onFailure(Consumer<Throwable> consumer) {
        this.onFailure = consumer;
        return this;
    }


    protected void forEachListener(Consumer<RestFutureListener<T, U>> consumer) {
        listenerExecutors.forEach(listenerExecutor -> {
            try {
                listenerExecutor.executor.runDeferred((() -> consumer.accept(listenerExecutor.listener)));
            } catch (Exception ignored) {
            }
        });

        listeners.forEach(listener -> {
            try {
                consumer.accept(listener);
            } catch (Exception ignored) {
            }
        });
    }

    protected void forEachNextFuture(Consumer<RestFuture<U, ?>> consumer) {
        nextFutures.forEach(nextFuture -> {
            try {
                consumer.accept(nextFuture);
            } catch (Exception ignored) {
            }
        });
    }


}

class ListenerExecutor<T, U> {
    final ScheduledExec executor;
    final RestFutureListener<T, U> listener;


    ListenerExecutor(ScheduledExec executor, RestFutureListener<T, U> listener) {
        this.executor = executor;
        this.listener = listener;
    }
}
