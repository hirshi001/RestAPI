package test;

import com.hirshi001.javarestapi.JavaRestFutureFactory;
import com.hirshi001.restapi.RestAPI;
import com.hirshi001.restapi.RestFuture;
import com.hirshi001.restapi.RestFutureListener;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;


public class RestTest {


    @Before
    public void setup(){
        RestAPI.setFactory(new JavaRestFutureFactory());
    }

    @Test
    public void BasicMethodsTest() throws InterruptedException {
        AtomicBoolean flag1 = new AtomicBoolean(false);
        AtomicBoolean flag2 = new AtomicBoolean(false);
        AtomicBoolean flag3 = new AtomicBoolean(false);
        AtomicBoolean flag4 = new AtomicBoolean(false);

        AtomicLong start = new AtomicLong();
        RestFuture<?, String> future = RestAPI.create(()->"Hi");
        RestFuture f2 = future
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

    @Test
    public void BothMethodsTest() throws InterruptedException {
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

    @Test
    public void AsyncMethodsTest() throws InterruptedException {
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

    @Test
    public void GetTest() throws ExecutionException, InterruptedException {
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

    @Test
    public void GetTestDelaySuccess() throws ExecutionException, InterruptedException, TimeoutException {
        String message = "Test";

        AtomicBoolean flag1 = new AtomicBoolean(false);

        RestFuture<String, String> future = RestAPI.<String>create((f, i)->{
            f.getScheduledExec().run( ()-> f.taskFinished(message), 100, TimeUnit.MILLISECONDS);
        }).then(s -> flag1.set(true));
        future.performAsync();

        assertEquals(message, future.get(200, TimeUnit.MILLISECONDS));
        assertTrue(flag1.get());
    }

    @Test
    public void GetTestDelayFail() {
        String message = "Test";

        AtomicBoolean flag1 = new AtomicBoolean(false);

        RestFuture<String, String> future = RestAPI.<String>create((f, i)->{
            f.getScheduledExec().run( ()-> f.taskFinished(message), 100, TimeUnit.MILLISECONDS);
        }).then(s -> flag1.set(true)).performAsync();

        assertThrows(TimeoutException.class, () -> future.get(50, TimeUnit.MILLISECONDS));
        assertFalse(flag1.get());
    }

    @Test
    public void ListenerErrorTest(){
        RestFuture<?, String> future = RestAPI.create();
        RestFutureListener<?, String> listener = new RestFutureListener<Object, String>() {
            @Override
            public void onComplete(RestFuture<Object, String> future) {

            }
        };

        assertThrows(NullPointerException.class, ()->future.addListener(null));
        future.addListener(listener);

        assertThrows(NullPointerException.class, ()->future.addListener(null, listener));
        assertThrows(NullPointerException.class, ()->future.addListener(future.getScheduledExec(), null));
        future.addListener(future.getScheduledExec(), listener);
    }

}
