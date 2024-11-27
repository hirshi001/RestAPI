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

package com.hirshi001.javarestapi;

import com.hirshi001.restapi.TimerAction;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the {@link TimerAction} interface for the Java platform.
 *
 * @author Hrishikesh Ingle
 */
public class JavaTimerAction extends TimerAction {

    private final ScheduledFuture<?> future;

    public JavaTimerAction(ScheduledFuture<?> future, long initialDelay, long delay, Runnable runnable, boolean repeating) {
        super(initialDelay, delay, runnable, repeating);
        this.future = future;
    }

    @Override
    public void cancel() {
        future.cancel(true);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public long getRemainingDelay() {
        return future.getDelay(TimeUnit.MILLISECONDS);
    }


}
