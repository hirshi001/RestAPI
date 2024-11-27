/*
 * Copyright 2024 Hrishikesh Ingle
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

package test;

import com.hirshi001.javarestapi.JavaScheduledExecutor;
import com.hirshi001.restapi.ScheduledExec;
import com.hirshi001.restapi.TimerAction;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;


public class JavaScheduledExecTest {

    ScheduledExec scheduledExec;

    @Before
    public void setup(){
        scheduledExec = new JavaScheduledExecutor();
    }

    @Test
    public void testRunDelay() throws InterruptedException {
        AtomicBoolean bool = new AtomicBoolean(false);
        scheduledExec.run(() -> bool.set(true), 1, java.util.concurrent.TimeUnit.SECONDS);

        Thread.sleep(950);
        assertFalse(bool.get());

        Thread.sleep(100);
        assertTrue(bool.get());
    }


    @Test
    public void testRunCancel() throws InterruptedException {
        AtomicBoolean bool = new AtomicBoolean(false);
        TimerAction timerAction = scheduledExec.run(() -> bool.set(true), 1, java.util.concurrent.TimeUnit.SECONDS);

        Thread.sleep(950);
        assertFalse(bool.get());

        timerAction.cancel();

        Thread.sleep(100);
        assertFalse(bool.get());
    }

    @Test
    public void testRunDeferred() throws InterruptedException {
        AtomicInteger integer = new AtomicInteger(0);
        scheduledExec.runDeferred(() -> integer.set(1));

        Thread.sleep(100);
        assertEquals(1, integer.get());
    }

    @Test
    public void runRepeat() throws InterruptedException {
        AtomicInteger integer = new AtomicInteger(0);
        TimerAction timerAction = scheduledExec.repeat(integer::incrementAndGet, 1, 1, java.util.concurrent.TimeUnit.SECONDS);

        Thread.sleep(950);
        assertEquals(0, integer.get());

        Thread.sleep(1000);
        assertEquals(1, integer.get());

        Thread.sleep(1000);
        assertEquals(2, integer.get());

        timerAction.cancel();

        Thread.sleep(1000);
        assertEquals(2, integer.get());
    }


}
