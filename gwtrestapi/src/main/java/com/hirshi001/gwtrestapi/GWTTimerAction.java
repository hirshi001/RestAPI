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

import com.google.gwt.user.client.Timer;
import com.hirshi001.restapi.TimerAction;

/**
 * A class that extends TimerAction for GWT.
 *
 * @author Hrishikesh Ingle
 */
public class GWTTimerAction extends TimerAction {
    private final Timer timer, inner;
    private boolean cancelled;
    long initialDelay;
    long delay;
    long initialTime;

    public GWTTimerAction(Timer timer, Timer inner, long initialDelay, long delay, Runnable runnable) {
        super(initialDelay, delay, runnable, inner != null);
        this.timer = timer;
        this.inner = inner;
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.initialTime = System.currentTimeMillis();

    }

    @Override
    public void cancel() {
        timer.cancel();
        inner.cancel();
        cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        if(inner == null) {
            return !timer.isRunning();
        }
        return !timer.isRunning() && !inner.isRunning();
    }

    @Override
    public long getRemainingDelay() {
        long elapsed = System.currentTimeMillis() - initialTime;
        return initialDelay - elapsed;

    }

}
