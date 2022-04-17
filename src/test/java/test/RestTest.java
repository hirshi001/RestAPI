package test;

import com.hirshi001.restapi.DefaultRestFuture;
import com.hirshi001.restapi.RestFuture;
import com.hirshi001.restapi.RestFutureListener;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class RestTest {

    @Test
    public void BasicMethodsTest() throws InterruptedException {
        AtomicBoolean flag1 = new AtomicBoolean(false);
        AtomicBoolean flag2 = new AtomicBoolean(false);
        AtomicBoolean flag3 = new AtomicBoolean(false);

        AtomicLong start = new AtomicLong();
        RestFuture<String, String> future = new DefaultRestFuture<>(Executors.newScheduledThreadPool(1));


        future
                .then((s)->{
                    flag1.set(true);
                })
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
                });

        future.accept("test");

        Thread.sleep(1200);

        assertTrue(flag1.get());
        assertTrue(flag2.get());
        assertTrue(flag3.get());
    }

    @Test
    public void BothMethodsTest() throws InterruptedException {
        AtomicBoolean flag1 = new AtomicBoolean(false);
        AtomicBoolean flag2 = new AtomicBoolean(false);
        AtomicBoolean flag3 = new AtomicBoolean(false);
        AtomicBoolean flag4 = new AtomicBoolean(false);

        RestFuture<String, String> future = RestFuture.create();
        future
                .then(s -> flag1.set(true))
                .thenBoth(RestFuture.<String>create().then(s -> flag2.set(true)),
                        RestFuture.<String>create().then(s -> flag3.set(true)))
                .then(s -> flag4.set(true));

        future.accept("Test");
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

        RestFuture<String, String> future = RestFuture.create();
        future
                .then(s -> flag1.set(true))
                .thenAsync(s -> {
                    flag2.set(true);
                    assertNotSame(mainThread, Thread.currentThread());
                })
                .then(s -> flag3.set(true))
                .map(s->5)
                .thenBothAsync(
                        RestFuture.<Integer>create().then(s -> {
                            flag4.set(true);
                            assertNotSame(mainThread, Thread.currentThread());
                        }),
                        RestFuture.<Integer>create().then(s -> {
                            flag5.set(true);
                            assertNotSame(mainThread, Thread.currentThread());
                        }))
                .then(s -> flag6.set(true));

        future.accept("Test");
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

        RestFuture<String, String> future = RestFuture.create();
        future.then(s -> flag1.set(true));

        future.getExecutor().schedule(() -> future.accept(message), 100, TimeUnit.MILLISECONDS);

        assertEquals(message, future.get());
        assertTrue(flag1.get());
    }

    @Test
    public void GetTestDelaySuccess() throws ExecutionException, InterruptedException, TimeoutException {
        String message = "Test";

        AtomicBoolean flag1 = new AtomicBoolean(false);

        RestFuture<String, String> future = RestFuture.create();
        future.then(s -> flag1.set(true));

        future.getExecutor().schedule(() -> future.accept(message), 100, TimeUnit.MILLISECONDS);

        assertEquals(message, future.get(200, TimeUnit.MILLISECONDS));
        assertTrue(flag1.get());
    }

    @Test
    public void GetTestDelayFail() {
        String message = "Test";

        AtomicBoolean flag1 = new AtomicBoolean(false);

        RestFuture<String, String> future = RestFuture.create();
        future.then(s -> flag1.set(true));

        future.getExecutor().schedule(() -> future.accept(message), 100, TimeUnit.MILLISECONDS);

        assertThrows(TimeoutException.class, () -> future.get(50, TimeUnit.MILLISECONDS));
        assertFalse(flag1.get());
    }

    @Test
    public void ListenerErrorTest(){
        RestFuture<String, String> future = RestFuture.create();
        RestFutureListener<String, String> listener = future1 -> {};

        assertThrows(NullPointerException.class, ()->future.addListener(null));
        assertDoesNotThrow(()->future.addListener(listener));

        assertThrows(NullPointerException.class, ()->future.addListener(null, listener));
        assertThrows(NullPointerException.class, ()->future.addListener(future.getExecutor(), null));
        assertDoesNotThrow(()->future.addListener(future.getExecutor(), listener));
    }

    @Test
    public void deadlockTest(){
        RestFuture<String, String> future = RestFuture.create();
       future.then(future);


       future.getExecutor().submit(()->{
           future.accept("Test");
       });

       assertThrows(TimeoutException.class, ()->future.get(10, TimeUnit.MILLISECONDS));
    }
}
