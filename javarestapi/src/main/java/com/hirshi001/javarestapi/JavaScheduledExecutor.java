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

import com.hirshi001.restapi.ScheduledExec;
import com.hirshi001.restapi.TimerAction;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the {@link ScheduledExec} interface for the Java platform.
 *
 * @author Hrishikesh Ingle
 */
public class JavaScheduledExecutor implements ScheduledExec {

    final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(ForkJoinPool.getCommonPoolParallelism());

    @Override
    public TimerAction run(Runnable runnable, long delay, TimeUnit period) {
        return new JavaTimerAction(scheduledExecutorService.schedule(runnable, delay, period), delay, 0 ,runnable, false);
    }

    @Override
    public void runDeferred(Runnable runnable) {
        scheduledExecutorService.execute(runnable);
    }

    @Override
    public TimerAction repeat(Runnable runnable, long initialDelay, long delay, TimeUnit period) {
        return new JavaTimerAction(scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelay, delay, period), initialDelay, delay, runnable, true);
    }
}
