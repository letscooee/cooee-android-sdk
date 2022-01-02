package com.letscooee.utils;

import androidx.annotation.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Common implementation of {@link ScheduledExecutorService} to make it a timer or interval.
 *
 * @author Ashish Gaikwad on 09/06/21
 * @version 0.3.0
 */
public class Timer {

    private final ScheduledExecutorService scheduledExecutor;

    public Timer() {
        scheduledExecutor = Executors.newScheduledThreadPool(1);
    }

    public void schedule(@NonNull Runnable runnable, long durationMillis) {
        scheduledExecutor.schedule(runnable, durationMillis, TimeUnit.MILLISECONDS);
    }
}
