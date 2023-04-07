package com.hirshi001.restapi;

public abstract class TimerAction {

    private long initialDelay;
    private long delay;
    private boolean cancelled;
    private Runnable runnable;


    public TimerAction(long initialDelay, long delay, Runnable runnable) {
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.runnable = runnable;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public long getDelay() {
        return delay;
    }

    public Runnable getAction() {
        return runnable;
    }


}
