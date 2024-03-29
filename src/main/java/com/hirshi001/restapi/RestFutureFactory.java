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

import java.util.concurrent.Callable;

/**
 * This interface is used to create {@link RestFuture} objects.
 *
 * @author Hrishikesh Ingle
 */
public interface RestFutureFactory {


    <T> RestFuture<T, T> create();

    <T> RestFuture<T, T> create(ScheduledExec executor);

    <T> RestFuture<T, T> create(RestFutureConsumer<T, T> consumer);

    <T> RestFuture<T, T> create(ScheduledExec executor, RestFutureConsumer<T, T> consumer);

    <T> RestFuture<T, T> create(ScheduledExec executor, boolean cancel, RestFutureConsumer<T, T> consumer);

    <T> RestFuture<T, T> create(Callable<T> action);

    <T> RestFuture<T, T> create(ScheduledExec executor, Callable<T> action);

     <T> RestFuture<T, T> create(ScheduledExec executor, boolean cancel, Callable<T> action);

    ScheduledExec getDefaultExecutor();


}
