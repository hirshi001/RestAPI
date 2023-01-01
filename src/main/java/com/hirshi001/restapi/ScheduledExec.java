package com.hirshi001.restapi;

import java.util.concurrent.TimeUnit;

public interface ScheduledExec {

    void run(Runnable runnable, long delay);

    void run(Runnable runnable, long delay, TimeUnit period);

    void runDeferred(Runnable runnable);



}
