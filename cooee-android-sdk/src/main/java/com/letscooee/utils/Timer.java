package com.letscooee.utils;

import org.jetbrains.annotations.NotNull;

import java.util.TimerTask;

/**
 * Common implementation for {@link java.util.Timer}
 *
 * @author Ashish Gaikwad on 09/06/21
 */
public class Timer {

    private final java.util.Timer handler;

    public Timer() {
        handler = new java.util.Timer();
    }

    public void schedule(@NotNull TimerTask runnable, long durationMillis) {
        handler.schedule(runnable, durationMillis);
    }
}
