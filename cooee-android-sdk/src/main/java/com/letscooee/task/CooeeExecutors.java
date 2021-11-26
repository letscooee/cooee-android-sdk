package com.letscooee.task;

import androidx.annotation.RestrictTo;
import com.letscooee.retrofit.DeviceAuthService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A utility class to provide global executors for multi-threading.
 *
 * @author Shashank Agrawal
 * @version 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CooeeExecutors {

    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private static CooeeExecutors INSTANCE;

    private final ExecutorService SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();

    private final ExecutorService NETWORK_EXECUTOR = Executors.newFixedThreadPool(NUMBER_OF_CORES * 3);

    public static CooeeExecutors getInstance() {
        if (INSTANCE == null) {
            synchronized (DeviceAuthService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CooeeExecutors();
                }
            }
        }
        return INSTANCE;
    }

    public ExecutorService singleThreadExecutor() {
        return this.SINGLE_THREAD_EXECUTOR;
    }

    public ExecutorService networkExecutor() {
        return this.NETWORK_EXECUTOR;
    }
}
