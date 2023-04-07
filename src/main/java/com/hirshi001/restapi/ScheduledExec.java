package com.hirshi001.restapi;

import java.util.concurrent.TimeUnit;

public interface ScheduledExec {

    void run(Runnable runnable, long delay);

    void run(Runnable runnable, long delay, TimeUnit period);

    void runDeferred(Runnable runnable);

    default TimerAction repeat(Runnable runnable, long initialDelay, long delay) {
        return repeat(runnable, initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    TimerAction repeat(Runnable runnable, long initialDelay, long delay, TimeUnit period);



}
