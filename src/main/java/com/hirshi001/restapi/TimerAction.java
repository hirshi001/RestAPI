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

/**
 * This class is used to keep track of commands created by a {@link ScheduledExec} object.
 *
 * @author Hrishikesh Ingle
 */
@SuppressWarnings({"FieldMayBeFinal", "unused"})

public abstract class TimerAction {

    private final long initialDelay;
    private final long delay;
    private boolean cancelled;
    private final Runnable runnable;
    private final boolean repeating;


    public TimerAction(long initialDelay, long delay, Runnable runnable, boolean repeating) {
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.runnable = runnable;
        this.repeating = repeating;
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

    public boolean isRepeating() {
        return repeating;
    }


}
