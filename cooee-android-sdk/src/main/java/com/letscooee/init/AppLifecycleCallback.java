package com.letscooee.init;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.letscooee.CooeeFactory;
import com.letscooee.ar.ARHelper;
import com.letscooee.broadcast.ARActionPerformed;
import com.letscooee.models.Event;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.user.NewSessionExecutor;
import com.letscooee.user.SessionManager;
import com.letscooee.utils.Constants;
import com.letscooee.utils.RuntimeData;

import java.util.HashMap;
import java.util.Map;

class AppLifecycleCallback implements DefaultLifecycleObserver {

    private final Context context;
    private final RuntimeData runtimeData;
    private final SessionManager sessionManager;

    private Handler handler = new Handler();
    private Runnable runnable;
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
    public void onResume(@NonNull LifecycleOwner owner) {
        //Will set app is in foreground
        runtimeData.setInForeground();
        keepSessionAlive();

        if (runtimeData.isFirstForeground()) {
            return;
        }

        long backgroundDuration = runtimeData.getTimeInBackgroundInSeconds();

        if (backgroundDuration > Constants.IDLE_TIME_IN_SECONDS) {
            sessionManager.conclude();

            new NewSessionExecutor(context).execute();
            Log.d(Constants.TAG, "After 30 min of App Background " + "Session Concluded");
        } else {
            Map<String, Object> eventProps = new HashMap<>();
            eventProps.put("Background Duration", backgroundDuration);

            Event event = new Event("CE App Foreground", eventProps);
            event.setDeviceProps(sessionExecutor.getMutableDeviceProps());

            safeHTTPService.sendEvent(event);
        }

        // Sent AR CTA once App is resumed
        ARActionPerformed.processLastARResponse(context);

        // Try to launch pending AR if any
        ARHelper.launchPendingAR(context);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        runtimeData.setInBackground();

        //stop sending check message of session alive on app background
        handler.removeCallbacks(runnable);

        if (context == null) {
            return;
        }

        long duration = runtimeData.getTimeInForegroundInSeconds();

        Map<String, Object> sessionProperties = new HashMap<>();
        sessionProperties.put("Foreground Duration", duration);

        Event event = new Event("CE App Background", sessionProperties);
        event.setDeviceProps(sessionExecutor.getMutableDeviceProps());

        safeHTTPService.sendEvent(event);
    }

    /**
     * Send server check message every 5 min that session is still alive
     */
    // TODO: 03/06/21 Move to SessionManager
    private void keepSessionAlive() {
        //send server check message every 5 min that session is still alive
        //TODO: 09/06/2021 To be change with Timer class
        handler.postDelayed(runnable = () -> {
            handler.postDelayed(runnable, Constants.KEEP_ALIVE_TIME_IN_MS);
            this.sessionManager.pingServerToKeepAlive();

        }, Constants.KEEP_ALIVE_TIME_IN_MS);
    }
}
