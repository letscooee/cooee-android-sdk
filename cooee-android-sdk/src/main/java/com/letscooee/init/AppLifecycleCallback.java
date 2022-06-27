package com.letscooee.init;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.letscooee.CooeeFactory;
import com.letscooee.ar.ARHelper;
import com.letscooee.broadcast.ARActionPerformed;
import com.letscooee.enums.LaunchType;
import com.letscooee.models.Event;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.task.CooeeExecutors;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.user.NewSessionExecutor;
import com.letscooee.user.SessionManager;
import com.letscooee.utils.RuntimeData;

import java.util.HashMap;
import java.util.Map;

class AppLifecycleCallback implements DefaultLifecycleObserver {

    private final Context context;
    private final RuntimeData runtimeData;
    private final SessionManager sessionManager;

    private final SafeHTTPService safeHTTPService;
    private final NewSessionExecutor sessionExecutor;

    AppLifecycleCallback(Context context) {
        this.context = context;
        this.runtimeData = CooeeFactory.getRuntimeData();
        this.sessionManager = CooeeFactory.getSessionManager();
        this.safeHTTPService = CooeeFactory.getSafeHTTPService();
        this.sessionExecutor = new NewSessionExecutor(context);
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        sessionManager.checkSessionExpiry();
        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> new NewSessionExecutor(context).execute());
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        // Will set app is in foreground
        runtimeData.setInForeground();
        sessionManager.keepSessionAlive();
        boolean willCreateNewSession = sessionManager.checkSessionExpiry();
        boolean isNewSession = willCreateNewSession || runtimeData.isFirstForeground();

        if (isNewSession && this.runtimeData.getLaunchType() == LaunchType.ORGANIC) {
            EngagementTriggerHelper.handleOrganicLaunch();
        }

        if (runtimeData.isFirstForeground()) {
            return;
        }

        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            long backgroundDuration = runtimeData.getTimeInBackgroundInSeconds();
            Map<String, Object> eventProps = new HashMap<>();
            eventProps.put("iaDur", backgroundDuration);

            Event event = new Event("CE App Foreground", eventProps);
            event.setDeviceProps(sessionExecutor.getMutableDeviceProps());
            safeHTTPService.sendEvent(event);
        });

        // Sent AR CTA once App is resumed
        ARActionPerformed.processLastARResponse(context);

        // Try to launch pending AR if any
        ARHelper.launchPendingAR(context);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        runtimeData.setInBackground();

        //stop sending check message of session alive on app background
        sessionManager.stopSessionAlive();

        if (context == null) {
            return;
        }

        CooeeExecutors.getInstance().singleThreadExecutor().execute(() -> {
            long duration = runtimeData.getTimeInForegroundInSeconds();

            Map<String, Object> eventProperties = new HashMap<>();
            eventProperties.put("aDur", duration);

            Event event = new Event("CE App Background", eventProperties);
            event.setDeviceProps(sessionExecutor.getMutableDeviceProps());

            safeHTTPService.sendEvent(event);
        });
    }
}
