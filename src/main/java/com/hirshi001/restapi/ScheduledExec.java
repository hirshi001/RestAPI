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

package com.hirshi001.restapi;

import java.util.concurrent.TimeUnit;

/**
 * This interface is used to schedule tasks.
 *
 * @author Hrishikesh Ingle
 */
@SuppressWarnings("unused")
public interface ScheduledExec {

    default TimerAction run(Runnable runnable, long delay) {
        return run(runnable, delay, TimeUnit.MILLISECONDS);
    }

    TimerAction run(Runnable runnable, long delay, TimeUnit period);

    void runDeferred(Runnable runnable);

    default TimerAction repeat(Runnable runnable, long initialDelay, long delay) {
        return repeat(runnable, initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    TimerAction repeat(Runnable runnable, long initialDelay, long delay, TimeUnit period);

}
