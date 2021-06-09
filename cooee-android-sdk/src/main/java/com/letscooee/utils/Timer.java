package com.letscooee.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Common implementation for {@link ScheduledExecutorService}
 *
 * @author Ashish Gaikwad on 09/06/21
 * @version 0.3.0
 */
public class Timer {

    private final ScheduledExecutorService scheduledPool;

    public Timer() {

        scheduledPool = Executors.newScheduledThreadPool(1);
    }

    public void schedule(@NotNull Runnable runnable, long durationMillis) {
        scheduledPool.schedule(runnable, durationMillis, TimeUnit.MILLISECONDS);
    }
}
