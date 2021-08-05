package com.letscooee.init;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.RestrictTo;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.messaging.FirebaseMessaging;
import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.font.FontProcessor;
import com.letscooee.pushnotification.PushProviderUtils;
import com.letscooee.schedular.CooeeJobUtils;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.utils.Constants;

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
    }

    void init() {

        // Skip initialisation of CooeeBootstrap if it's getting called via CooeeARProcess
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            if (processName.contains(Constants.AR_PROCESS))
                return;
        }

        CooeeFactory.init(this.context);
        application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallback(this.context));
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleCallback(this.context));
        this.initAsyncTasks();
    }

    /**
     * These tasks should not block the application launch.
     */
    private void initAsyncTasks() {
        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            getAndUpdateFirebaseToken();
            checkAndStartJob();
            FontProcessor.fetchFontFile(context);
        });
    }

    /**
     * This method will check if job is currently present or not with system
     * If job is not present it will add job in a queue
     */
    private void checkAndStartJob() {
        CooeeJobUtils.schedulePendingTaskJob(context);
    }

    private void getAndUpdateFirebaseToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (BuildConfig.DEBUG) {
                Log.d(Constants.TAG, "FCM token fetched- " + token);
            }

            PushProviderUtils.pushTokenRefresh(token);
        });
    }
}
