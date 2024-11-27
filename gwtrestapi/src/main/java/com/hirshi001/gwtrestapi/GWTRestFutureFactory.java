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

import com.google.gwt.core.client.GWT;
import com.hirshi001.restapi.RestFuture;
import com.hirshi001.restapi.RestFutureConsumer;
import com.hirshi001.restapi.RestFutureFactory;
import com.hirshi001.restapi.ScheduledExec;

import java.util.concurrent.Callable;

/**
 * A factory class that creates instances of {@link GWTRestFuture}.
 *
 * @author Hrishikesh Ingle
 */
public class GWTRestFutureFactory implements RestFutureFactory {

    private static ScheduledExec DEFAULT_EXECUTOR;

    public static ScheduledExec getDefaultScheduledExec() {
        if (DEFAULT_EXECUTOR == null) {
            if(GWT.isClient())
                DEFAULT_EXECUTOR = new GWTScheduledExecutor();
            else
                throw new IllegalStateException("Default executor not set");
        }
        return DEFAULT_EXECUTOR;
    }
    @Override
    public <T> RestFuture<T, T> create() {
        return create(getDefaultScheduledExec(), true, (RestFutureConsumer<T, T>) null);
    }

    @Override
    public <T> RestFuture<T, T> create(ScheduledExec executor) {
        return create(executor, true, (RestFutureConsumer<T, T>) null);
    }

    @Override
    public <T> RestFuture<T, T> create(RestFutureConsumer<T, T> consumer) {
        return create(getDefaultScheduledExec(), consumer);
    }

    @Override
    public <T> RestFuture<T, T> create(ScheduledExec executor, RestFutureConsumer<T, T> consumer) {
        return create(executor, true, consumer);
    }

    @Override
    public <T> RestFuture<T, T> create(ScheduledExec executor, boolean cancel, RestFutureConsumer<T, T> consumer) {
        return new GWTRestFuture<>(executor, cancel, consumer, null);
    }

    @Override
    public <T> RestFuture<T, T> create(Callable<T> action) {
        return create(getDefaultScheduledExec(), true, action);
    }

    @Override
    public <T> RestFuture<T, T> create(ScheduledExec executor, Callable<T> action) {
        return create(executor, true, action);
    }

    @Override
    public <T> RestFuture<T, T> create(ScheduledExec executor, boolean cancel, Callable<T> action) {
        return new GWTRestFuture<>(executor, cancel, (future, input) -> {
            try {
                T result = action.call();
                future.taskFinished(result);
            } catch (Exception e) {
                future.setCause(e);
            }
        }, null);
    }

    @Override
    public ScheduledExec getDefaultExecutor() {
        return getDefaultScheduledExec();
    }
}
