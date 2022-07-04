package com.hirshi001.restapi;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultRestFuture<T, U> implements RestFuture<T, U>{

    public static final ScheduledExecutorService DEFAULT_EXECUTOR = Executors.newScheduledThreadPool(ForkJoinPool.getCommonPoolParallelism());

    private ScheduledExecutorService executor;
    private boolean isCancelled;
    private boolean isDone;
    private boolean isSuccess;
    private final boolean cancellable;
    private U result;
    private Throwable cause;
    private final CountDownLatch latch;

    private RestFuture<?, T> parent;
    private RestFutureConsumer<T, U> task;
    private Queue<DefaultRestFuture<U, ?>> nextFutures;
    private Queue<ListenerExecutor<T, U>> listenerExecutors;
    private Queue<RestFutureListener<T, U>> listeners;
    private Consumer<Throwable> onFailure;

    public DefaultRestFuture(ScheduledExecutorService executor, boolean cancellable, RestFutureConsumer<T, U> task, RestFuture<?, T> parent) {
        this.executor = executor;
        this.cancellable = cancellable;
        this.task = task;
        this.parent = parent;

        nextFutures = new ConcurrentLinkedQueue<>();
        listenerExecutors = new ConcurrentLinkedQueue<>();
        listeners = new ConcurrentLinkedQueue<>();

        latch = new CountDownLatch(1);
    }


    @Override
    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    @Override
    public void setExecutor(ScheduledExecutorService executor) {
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
        onFailure.accept(cause);
        latch.countDown();
        forEachListener(l->{
            l.onComplete(this);
            l.failure(DefaultRestFuture.this);
        });
        forEachNextFuture(f->f.setCause(cause));
    }

    @Override
    public RestFuture<T, U> perform() {
        if(parent==null) perform(null);
        else{
            parent.perform();
        }
        return this;
    }

    @Override
    public RestFuture<T, U> performAsync() {
        executor.submit((Runnable) this::perform);
        return this;
    }

    @Override
    public RestFuture<T, U> perform(Object input) {
        if(task!=null){
            try {
                task.accept(this, (T)input);
            }catch (Exception e){
                setCause(e);
            }
        }else{
            taskFinished((U)input);
        }
        return this;
    }

    @Override
    public RestFuture<T, U> performAsync(T input) {
        executor.submit((Runnable) ()->perform(input));
        return this;
    }

    @Override
    public RestFuture<U, U> then(Consumer<U> consumer) {
        return add((r, i)->{
            consumer.accept(i);
            r.taskFinished(i);
        });
    }

    @Override
    public RestFuture<U, U> then(RestFuture<U, ?> future) {
        return add((r, i)-> {
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
        return add((r, i)-> {
            future.performAsync(i);
            r.taskFinished(i);
        });
    }

    @Override
    public RestFuture<U, U> thenAsync(Consumer<U> consumer, Executor executor) {
        return add((r, i)->{
            executor.execute(()->{
                consumer.accept(i);
                r.taskFinished(i);
            });
        });
    }

    @Override
    public RestFuture<U, U> thenBoth(RestFuture<U, ?> first, RestFuture<U, ?> second) {
        return add((r, i)->{
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
    public RestFuture<U, U> thenBothAsync(RestFuture<U, ?> first, RestFuture<U, ?> second, Executor executor) {
        return add((r, i)->{
            executor.execute(()->first.perform(i));
            executor.execute(()->second.perform(i));
            r.taskFinished(i);
        });
    }

    @Override
    public <B> RestFuture<U, B> map(Function<U, B> function) {
        return add((r, i)->{
            r.taskFinished(function.apply(i));
        });
    }

    @Override
    public <B> RestFuture<U, B> mapAsync(Function<U, B> function) {
        return mapAsync(function, executor);
    }

    @Override
    public <B> RestFuture<U, B> mapAsync(Function<U, B> function, Executor executor) {
        return add((r, i)->{
            executor.execute(()->r.taskFinished(function.apply(i)));
        });
    }

    @Override
    public RestFuture<U, U> pauseFor(long timeout) {
        return pauseFor(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public RestFuture<U, U> pauseFor(long timeout, TimeUnit unit) {
        return add((r, i)->{
            executor.schedule(()->r.taskFinished(i), timeout, unit);
        });
    }

    @Override
    public U get() throws InterruptedException {
        while(!isDone()) {
            latch.await();
        }
        return result;
    }

    @Override
    public U get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        boolean success = latch.await(timeout, unit);
        if(!success || !isSuccess()) {
            throw new TimeoutException();
        }
        return result;
    }

    @Override
    public RestFuture<T, U> addListener(RestFutureListener listener) {
        if(listener==null) throw new NullPointerException("listener cannot be null");
        listeners.add(listener);
        return this;
    }

    @Override
    public RestFuture<T, U> addListener(Executor executor, RestFutureListener listener) {
        if(executor==null) throw new NullPointerException("executor cannot be null");
        if(listener==null) throw new NullPointerException("listener cannot be null");
        listenerExecutors.add(new ListenerExecutor<>(executor, listener));
        return this;
    }

    @Override
    public boolean removeListener(RestFutureListener listener) {
        boolean success = false;
        if(listeners.removeIf(l -> l == listener)) success = true; //we need to remove possible duplicate listeners from both lists
        if(listenerExecutors.removeIf(l->l.listener == listener)) success = true;
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
        if(cancellable) {
            isCancelled = true;
            latch.countDown();
            forEachListener(l->{
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
        return cause()!=null;
    }

    protected  <B> RestFuture<U, B> add(RestFutureConsumer<U, B> function) {
        DefaultRestFuture<U, B> future = new DefaultRestFuture<>(executor, false, function, this);
        nextFutures.add(future);
        return future;
    }

    @Override
    public void taskFinished(U result) {
        //TODO: we should check for possible recursive calls causing deadlocks
        this.result = result;
        isDone = true;
        this.isSuccess = true;
        latch.countDown();
        forEachListener(l->l.success(DefaultRestFuture.this));
        forEachNextFuture(f->f.perform(result));
    }

    @Override
    public RestFuture<T, U> onFailure(Consumer<Throwable> consumer) {
        this.onFailure = consumer;
        return this;
    }


    protected void forEachListener(Consumer<RestFutureListener<T, U>> consumer) {
        listenerExecutors.forEach(listenerExecutor -> {
            try {
                listenerExecutor.executor.execute(()->consumer.accept(listenerExecutor.listener));
            }catch (Exception ignored) {}
        });

        listeners.forEach(listener -> {
            try {
                consumer.accept(listener);
            }catch (Exception ignored) {}
        });
    }

    protected void forEachNextFuture(Consumer<RestFuture<U, ?>> consumer) {
        nextFutures.forEach(nextFuture -> {
            try {
                consumer.accept(nextFuture);
            }catch (Exception ignored) {}
        });
    }


}

class ListenerExecutor<T, U>{
    final Executor executor;
    final RestFutureListener<T, U> listener;


    ListenerExecutor(Executor executor, RestFutureListener<T, U> listener) {
        this.executor = executor;
        this.listener = listener;
    }
}
