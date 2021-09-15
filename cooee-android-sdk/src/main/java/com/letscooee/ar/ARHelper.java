package com.letscooee.ar;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.AppAR;
import com.letscooee.utils.Timer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Augmented Reality
 *
 * @author Ashish Gaikwad 14/09/21
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ARHelper {

    private static ArCoreApk.Availability availability;
    private static AppAR pendingAR;
    private static TriggerData pendingTriggerData;

    /**
     * Initialize {@link ArCoreApk.Availability} to check if device supports AR or not
     * It also send data to backend
     *
     * @param context will be instance if {@link Context}
     */
    public static void checkDeviceSupport(Context context) {
        availability = ArCoreApk.getInstance().checkAvailability(context);

        if (availability.isTransient()) {
            new Timer().schedule(() -> checkDeviceSupport(context), 200);
            return;
        }

        sendUserProperty("CE AR Supported", availability.isSupported());
    }

    /**
     * Wrap user-property in a proper format and send it server
     *
     * @param key   is {@link String} used as key parameter
     * @param value is {@link Object} so we can send any type of value
     */
    private static void sendUserProperty(String key, Object value) {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put(key, value);

        CooeeFactory.getSafeHTTPService().updateDeviceProps(userProperties);
    }

    /**
     * Check if Google Play Service for AR is installed or not. If service is not installed and
     * device supports AR it will prompt to install service. Despite the status, it will launch the
     * AR so if the device is not supported, it will open the 2D view
     *
     * @param activity    instance of current running {@link Activity}
     * @param triggerData {@link TriggerData} of the active trigger
     */
    public static void checkForARAndLaunch(Activity activity, AppAR appAR, TriggerData triggerData) {
        if (availability == null || !availability.isSupported()) {
            launchARViaUnity(activity, appAR, triggerData);
            return;
        }

        boolean launchAR = false;

        try {
            ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(activity, true);

            if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                pendingAR = appAR;
                pendingTriggerData = triggerData;
            } else {
                launchAR = true;
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            // User has declined to install AR Service.
            sendUserProperty("CE AR Service Declined", true);
            launchAR = true;
        } catch (UnavailableDeviceNotCompatibleException e) {
            // Device is not supported
            launchAR = true;
            sendUserProperty("CE AR Supported", availability.isSupported());
        } finally {
            if (launchAR) launchARViaUnity(activity, appAR, triggerData);
        }
    }

    /**
     * Launch AR using {@link UnityPlayerActivity}
     *
     * @param context     instance of {@link Context}
     * @param appAR       data required to launch AR
     * @param triggerData {@link TriggerData} of the active trigger
     */
    private static void launchARViaUnity(@NonNull Context context, @NonNull AppAR appAR, TriggerData triggerData) {
        String arData = new Gson().toJson(appAR);
        Intent intent = new Intent(context, UnityPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("arguments", arData);
        intent.putExtra("app_package", CooeeFactory.getAppInfo().getPackageName());

        try {
            context.startActivity(intent);
            CooeeFactory.getSafeHTTPService().sendEvent(new Event("CE AR Displayed", triggerData));
        } catch (ActivityNotFoundException exception) {
            CooeeFactory.getSentryHelper().captureException(exception);
        }
    }

    /**
     * Check for pending AR and if present launch them
     *
     * @param context instance of {@link Context}
     */
    public static void launchPendingAR(Context context) {
        if (pendingAR == null) {
            return;
        }

        launchARViaUnity(context, pendingAR, pendingTriggerData);
        pendingAR = null;
        pendingTriggerData = null;
    }
}