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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.hirshi001.restapi.ScheduledExec;
import com.hirshi001.restapi.TimerAction;

import java.util.concurrent.TimeUnit;

/**
 * A class that implements the ScheduledExec interface for GWT.
 *
 * @author Hrishikesh Ingle
 */
public class GWTScheduledExecutor implements ScheduledExec {

    private final Scheduler scheduler = Scheduler.get();
    @Override
    public TimerAction run(Runnable runnable, long delay) {
        Timer timer = new Timer() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        timer.schedule((int)delay);
        return new GWTTimerAction(timer, null, delay, 0, runnable);
    }

    @Override
    public TimerAction run(Runnable runnable, long delay, TimeUnit period) {
        return run(runnable, period.toMillis(delay));
    }

    @Override
    public void runDeferred(Runnable runnable) {
        scheduler.scheduleDeferred(runnable::run);
    }

    @Override
    public TimerAction repeat(Runnable runnable, long initialDelay, long delay, TimeUnit period) {
        final Timer inner = new Timer() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        Timer timer = new Timer() {
            @Override
            public void run() {
                inner.scheduleRepeating((int)period.toMillis(delay));
            }
        };
        timer.schedule((int) period.toMillis(initialDelay));
        return new GWTTimerAction(timer, inner, initialDelay, delay, runnable);
    }


}
