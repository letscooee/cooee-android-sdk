package com.letscooee.task;

import androidx.annotation.RestrictTo;
import com.letscooee.retrofit.UserAuthService;

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

    private static CooeeExecutors INSTANCE;

    private final ExecutorService SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();

    public static CooeeExecutors getInstance() {
        if (INSTANCE == null) {
            synchronized (UserAuthService.class) {
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
}
