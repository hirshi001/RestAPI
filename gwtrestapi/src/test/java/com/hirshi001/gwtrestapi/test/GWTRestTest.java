package com.hirshi001.gwtrestapi.test;

import com.google.gwt.junit.client.GWTTestCase;
import com.hirshi001.gwtrestapi.GWTRestFutureFactory;
import com.hirshi001.restapi.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class GWTRestTest extends GWTTestCase {


    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        RestAPI.setFactory(new GWTRestFutureFactory());
    }


    public void testBasicMethods() throws InterruptedException {

        AtomicBoolean flag1 = new AtomicBoolean(false);
        AtomicBoolean flag2 = new AtomicBoolean(false);
        AtomicBoolean flag3 = new AtomicBoolean(false);
        AtomicBoolean flag4 = new AtomicBoolean(false);

        AtomicLong start = new AtomicLong();
        RestFuture<?, String> future = RestAPI.create(()->"Hi");
        RestFuture<Integer, Integer> f2 = future
                .then((s)->{
                    flag1.set(true);
                    assert s.equals("Hi");
                })
                .then(RestAPI.create(()->{
                    flag4.set(true);
                    return null;
                }))
                .map(s->{
                    flag2.set(true);
                    return s.hashCode();
                })
                .then((i)->{
                    start.set(System.currentTimeMillis());
                })
                .pauseFor(1, TimeUnit.SECONDS)
                .then((i)->{
                    long diff = System.currentTimeMillis() - start.get();
                    if(diff > 980 && diff < 1020){
                        flag3.set(true);
                    }
                }).perform();


        Thread.sleep(1200);

        assertTrue(flag1.get());
        assertTrue(flag2.get());
        assertTrue(flag3.get());
        assertTrue(flag4.get());

        assertTrue(f2.isDone());
        assertTrue(f2.isSuccess());
        assertNull(f2.cause());
        assertFalse(f2.isCancelled());
        assertFalse(f2.isFailure());

    }

    public void testBothMethods() throws InterruptedException {
        AtomicBoolean flag1 = new AtomicBoolean(false);
        AtomicBoolean flag2 = new AtomicBoolean(false);
        AtomicBoolean flag3 = new AtomicBoolean(false);
        AtomicBoolean flag4 = new AtomicBoolean(false);

        RestFuture<String, String> future = RestAPI.create();
        future
                .then(s -> flag1.set(true))
                .thenBoth(RestAPI.<String>create().then(s -> flag2.set(true)),
                        RestAPI.<String>create().then(s -> flag3.set(true)))
                .then(s -> flag4.set(true));

        future.perform("Test");
        Thread.sleep(100);

        assertTrue(flag1.get());
        assertTrue(flag2.get());
        assertTrue(flag3.get());
        assertTrue(flag4.get());
    }

    public void testAsyncMethods() throws InterruptedException {
        AtomicBoolean flag1 = new AtomicBoolean(false);
        AtomicBoolean flag2 = new AtomicBoolean(false);
        AtomicBoolean flag3 = new AtomicBoolean(false);
        AtomicBoolean flag4 = new AtomicBoolean(false);
        AtomicBoolean flag5 = new AtomicBoolean(false);
        AtomicBoolean flag6 = new AtomicBoolean(false);

        Thread mainThread = Thread.currentThread();

        RestFuture<String, String> future = RestAPI.create();

        future
                .then(s -> flag1.set(true))
                .thenAsync(s -> {
                    flag2.set(true);
                    assertNotSame(mainThread, Thread.currentThread());
                })
                .then(s -> flag3.set(true))
                .map(s->5)
                .thenBothAsync(
                        RestAPI.<Integer>create().then(s -> {
                            flag4.set(true);
                            assertNotSame(mainThread, Thread.currentThread());
                        }),
                        RestAPI.<Integer>create().then(s -> {
                            flag5.set(true);
                            assertNotSame(mainThread, Thread.currentThread());
                        }))
                .then(s -> flag6.set(true));

        future.perform("Test");
        Thread.sleep(100);

        assertTrue(flag1.get());
        assertTrue(flag2.get());
        assertTrue(flag3.get());
        assertTrue(flag4.get());
        assertTrue(flag5.get());
        assertTrue(flag6.get());

    }

    public void testGet() throws ExecutionException, InterruptedException {
        String message = "Test";

        AtomicBoolean flag1 = new AtomicBoolean(false);

        RestFuture<?, String> future = RestAPI.create((f, i)->
                f.getScheduledExec().run( ()-> f.taskFinished(message),
                        100,
                        TimeUnit.MILLISECONDS));

        future = future.then(s -> flag1.set(true));
        future.perform();


        assertEquals(message, future.get());
        assertTrue(flag1.get());
    }

    public void testDelaySuccess() throws ExecutionException, InterruptedException, TimeoutException {
        String message = "Test";

        AtomicBoolean flag1 = new AtomicBoolean(false);

        RestFuture<String, String> future = RestAPI.<String>create((f, i)->{
            f.getScheduledExec().run( ()-> f.taskFinished(message), 100, TimeUnit.MILLISECONDS);
        }).then(s -> flag1.set(true));
        future.performAsync();

        assertEquals(message, future.get(200, TimeUnit.MILLISECONDS));
        assertTrue(flag1.get());
    }

    public void testDelayFail() {
        String message = "Test";

        AtomicBoolean flag1 = new AtomicBoolean(false);

        RestFuture<String, String> future = RestAPI.<String>create((f, i)->{
            f.getScheduledExec().run( ()-> f.taskFinished(message), 100, TimeUnit.MILLISECONDS);
        }).then(s -> flag1.set(true)).performAsync();

        assertThrows(TimeoutException.class, () -> future.get(50, TimeUnit.MILLISECONDS));
        assertFalse(flag1.get());
    }


    public void testListenerError(){
        RestFuture<?, String> future = RestAPI.create();
        RestFutureListener<?, String> listener = new RestFutureListener<Object, String>() {
            @Override
            public void onComplete(RestFuture<Object, String> future) {

            }
        };

        assertThrows(NullPointerException.class, ()->future.addListener(null));
        assertDoesNotThrow(()->future.addListener(listener));

        assertThrows(NullPointerException.class, ()->future.addListener(null, listener));
        assertThrows(NullPointerException.class, ()->future.addListener(future.getScheduledExec(), null));
        assertDoesNotThrow(()->future.addListener(future.getScheduledExec(), listener));
    }

    private void assertDoesNotThrow(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            throw new AssertionError("Expected no exception, but got: ", t);
        }
    }


    private <T extends Throwable> void assertThrows(Class<T> timeoutExceptionClass, ThrowableRunnable o) {
        try {
            o.run();
            throw new AssertionError("Expected exception of type: " + timeoutExceptionClass.getName());
        } catch (Throwable t) {
            
            throw new AssertionError("Expected exception of type: " + timeoutExceptionClass.getName() + " but got: ", t);
        }
    }

    @FunctionalInterface
    interface ThrowableRunnable  {
        void run() throws Throwable;
    }


    @Override
    public String getModuleName() {
        return "com.hirshi001.gwtrestapi.GWTRestAPI";
    }
}
