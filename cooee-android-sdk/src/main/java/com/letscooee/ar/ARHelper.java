package com.letscooee.ar;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.AppAR;
import com.letscooee.utils.Constants;
import com.letscooee.utils.Timer;

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

        sendDeviceProperty("ar", availability.isSupported());
    }

    /**
     * Wrap user-property in a proper format and send it server
     *
     * @param key   is {@link String} used as key parameter
     * @param value is {@link Object} so we can send any type of value
     */
    private static void sendDeviceProperty(String key, Object value) {
        Map<String, Object> deviceProperties = new HashMap<>();
        Map<String, Object> props = new HashMap<>();

        props.put(key, value);
        deviceProperties.put("props", props);

        CooeeFactory.getSafeHTTPService().updateDeviceProperty(deviceProperties);
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
            sendDeviceProperty("arServiceDeclined", true);
            launchAR = true;
        } catch (UnavailableDeviceNotCompatibleException e) {
            // Device is not supported
            launchAR = true;
            sendDeviceProperty("ar", false);
        } finally {
            if (launchAR) launchARViaUnity(activity, appAR, triggerData);
        }
    }

    /**
     * Sends broadcast to AR SDK to launch AR
     *
     * @param context     instance of {@link Context}
     * @param appAR       data required to launch AR
     * @param triggerData {@link TriggerData} of the active trigger
     */
    private static void launchARViaUnity(@NonNull Context context, @NonNull AppAR appAR, TriggerData triggerData) {
        Bundle bundle = new Bundle();
        String arData = new Gson().toJson(appAR);
        String appPackageName = CooeeFactory.getAppInfo().getPackageName();
        bundle.putString(Constants.AR_INTENT_TYPE, Constants.AR_LAUNCH_INTENT);
        bundle.putString(Constants.AR_DATA, arData);
        bundle.putString(Constants.AR_PACKAGE_NAME, appPackageName);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        intent.setAction(Constants.AR_INTENT);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName(appPackageName, Constants.AR_BROADCAST_CLASS));
        context.sendBroadcast(intent);
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
