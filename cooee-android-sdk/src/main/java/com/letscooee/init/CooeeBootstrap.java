package com.letscooee.init;

import android.app.Application;
import android.content.Context;
import androidx.annotation.RestrictTo;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.letscooee.CooeeFactory;
import com.letscooee.brodcast.CooeeJobSchedulerBroadcast;
import com.letscooee.schedular.CooeeJobUtils;
import com.letscooee.task.CooeeExecutors;

/**
 * A one time initializer class which initialises the Cooee SDK. This is used internally by the SDK
 * and should be quick.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class CooeeBootstrap {

    private final Context context;
    private final Application application;

    CooeeBootstrap(Application application) {
        this.application = application;
        this.context = application.getApplicationContext();

        CooeeFactory.init(this.context);
    }

    void init() {
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallback(this.context));
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleCallback(this.context));
        this.initAsyncTasks();
    }

    /**
     * These tasks should not block the application launch.
     */
    private void initAsyncTasks() {
        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            initSentry();
            checkAndStartJob();
        });
    }

    private void initSentry() {
        CooeeFactory.getSentryHelper().init();
    }

    /**
     * This method will check if job is currently present or not with system
     * If job is not present it will add job in a queue
     */
    private void checkAndStartJob() {
        // TODO: 03/06/21 Do we really need to start manually
        CooeeJobUtils.schedulePendingTaskJob(context);
    }
}
