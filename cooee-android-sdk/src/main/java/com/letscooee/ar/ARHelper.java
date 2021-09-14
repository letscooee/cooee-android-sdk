package com.letscooee.ar;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.gson.Gson;
import com.letscooee.CooeeFactory;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.blocks.AppAR;
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

    /**
     * Initialize {@link ArCoreApk.Availability} to check if device supports AR or not
     * It also send data to backend
     *
     * @param context will be instance if {@link Context}
     */
    public static void checkDeviceSupport(Context context) {
        availability = ArCoreApk.getInstance().checkAvailability(context);

        if (availability.isTransient()) {
            new Handler().postDelayed(() -> checkDeviceSupport(context), 200);
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
        Map<String, Object> userMap = new HashMap<>();
        Map<String, Object> userProperties = new HashMap<>();

        userProperties.put(key, value);

        userMap.put("userProperties", userProperties);
        userMap.put("userData", new HashMap<>());

        CooeeFactory.getSafeHTTPService().updateUserProfile(userMap);
    }

    /**
     * Check if Google Play Service for AR is installed or not. If service is not installed and
     * device supports AR it will prompt to install service
     *
     * @param activity instance of current running {@link Activity}
     */
    public static void checkForARService(Activity activity, AppAR appAR) {
        if (!availability.isSupported()) {
            launchAR(activity, appAR);
            return;
        }

        try {
            ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(activity, true);

            if (installStatus == ArCoreApk.InstallStatus.INSTALLED) {
                launchAR(activity, appAR);
            } else if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                pendingAR = appAR;
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            // User has declined to install AR Service.
            sendUserProperty("CE AR SERVICE DECLINE", true);
            launchAR(activity, appAR);
        } catch (UnavailableDeviceNotCompatibleException e) {
            // Device is not supported
            launchAR(activity, appAR);
        }
    }

    /**
     * Launch AR using {@link UnityPlayerActivity}
     *
     * @param context instance of {@link Context}
     * @param appAR   data required to launch AR
     */
    private static void launchAR(@NonNull Context context, @NonNull AppAR appAR) {
        String arData = new Gson().toJson(appAR);
        Intent intent = new Intent(context, UnityPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("arguments", arData);
        intent.putExtra("app_package", CooeeFactory.getAppInfo().getPackageName());

        try {
            context.startActivity(intent);
            Event event = new Event("CE AR Displayed");
            CooeeFactory.getSafeHTTPService().sendEvent(event);
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

        launchAR(context, pendingAR);
        pendingAR = null;
    }
}