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

    /**
     * Schedules a one-shot action that becomes enabled after the given delay.
     *
     * @param runnable       the task to execute
     * @param durationMillis the delay time in milliseconds
     */
    public void schedule(@NonNull Runnable runnable, long durationMillis) {
        scheduledExecutor.schedule(runnable, durationMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops all scheduled tasks and interrupts all threads.
     */
    public void stop() {
        scheduledExecutor.shutdownNow();
    }

    /**
     * Returns {@code true} if this executor has been shut down.
     *
     * @return {@code true} if this executor has been shut down
     */
    public boolean isShutdown() {
        return scheduledExecutor.isShutdown();
    }
}
