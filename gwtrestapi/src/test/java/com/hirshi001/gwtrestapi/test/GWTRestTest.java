package com.hirshi001.gwtrestapi.test;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.junit.client.GWTTestCase;
import com.hirshi001.gwtrestapi.GWTRestFutureFactory;
import com.hirshi001.restapi.*;

import java.util.concurrent.TimeUnit;


public class GWTRestTest extends GWTTestCase {


    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        RestAPI.setFactory(new GWTRestFutureFactory());
    }

    public void testBasicMethods() throws InterruptedException {

        boolean[] flags = new boolean[5];

        Long[] start = new Long[1];
        RestFuture<?, String> future = RestAPI.create(()->"Hi");
        RestFuture<Integer, Integer> f2 = future
                .then((s)->{
                    flags[0] = true;
                    assert s.equals("Hi");
                })
                .then(RestAPI.create(()->{
                    flags[1] = true;
                    return null;
                }))
                .map(s->{
                    flags[2] = true;
                    return s.hashCode();
                })
                .then((i)->{
                    flags[3] = true;
                    start[0] = System.currentTimeMillis();
                })
                .pauseFor(1, TimeUnit.SECONDS)
                .then((i)->{
                    long diff = System.currentTimeMillis() - start[0];
                    if(diff > 980 && diff < 1020){
                        flags[4] = true;
                    }
                }).perform();


        // run this code 1000ms later
        Scheduler.get().scheduleFixedDelay(() -> {
            assertTrue(flags[0]);
            assertTrue(flags[1]);
            assertTrue(flags[2]);
            assertTrue(flags[3]);
            assertTrue(flags[4]);

            assertTrue(f2.isDone());
            assertTrue(f2.isSuccess());
            assertNull(f2.cause());
            assertFalse(f2.isCancelled());
            assertFalse(f2.isFailure());

            return false;
        }, 1200);


    }

    public void testBothMethods() throws InterruptedException {
        boolean[] flags = new boolean[4];

        RestFuture<String, String> future = RestAPI.create();
        future
                .then(s -> flags[0] = true)
                .thenBoth(RestAPI.<String>create().then(s -> flags[1] = true),
                        RestAPI.<String>create().then(s -> flags[2] = true))
                .then(s -> flags[3] = true);

        future.perform("Test");


        Scheduler.get().scheduleFixedDelay(() -> {
            assertTrue(flags[0]);
            assertTrue(flags[1]);
            assertTrue(flags[2]);
            assertTrue(flags[3]);
            return false;
        }, 100);

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
        return "com.hirshi001.gwtrestapi.test.Test";
    }
}
