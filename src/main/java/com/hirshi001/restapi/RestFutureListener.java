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
 * A functional interface that accepts a {@link RestFuture} as an input.
 * @param <T> The type of the input
 * @param <U> The type of the output
 */
@FunctionalInterface
public interface RestFutureListener<T, U> {

    void onComplete(RestFuture<T, U> future);

    default void success(RestFuture<T, U> future){}

    default void failure(RestFuture<T, U> future){}

    default void cancelled(RestFuture<T, U> future){}

}
