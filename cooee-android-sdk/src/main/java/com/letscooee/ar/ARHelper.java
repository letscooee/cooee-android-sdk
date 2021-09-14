package com.letscooee.ar;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.RestrictTo;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.letscooee.CooeeFactory;

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

        Map<String, Object> userMap = new HashMap<>();
        Map<String, Object> userProperties = new HashMap<>();

        userProperties.put("CE AR Supported", availability.isSupported());

        userMap.put("userProperties", userProperties);
        userMap.put("userData", new HashMap<>());

        CooeeFactory.getSafeHTTPService().updateUserProfile(userMap);
    }

    /**
     * Check if Google Pay Service for AR is installed or not. If service is not installed and
     * device supports AR it will prompt to install service
     *
     * @param activity instance of current running {@link Activity}
     */
    public static void checkForARService(Activity activity) {
        try {
            if (availability.isSupported()) {
                //noinspection SwitchStatementWithoutDefaultBranch
                switch (ArCoreApk.getInstance().requestInstall(activity, true)) {
                    case INSTALLED:
                        // Success: Google Play Services for AR is installed.
                        break;
                    case INSTALL_REQUESTED:
                         /* When this method returns `INSTALL_REQUESTED`:
                         1. ARCore pauses this activity.
                         2. ARCore prompts the user to install or update Google Play
                            Services for AR (market://details?id=com.google.ar.core).
                         3. ARCore downloads the latest device profile data.
                         4. ARCore resumes this activity. The next invocation of
                            requestInstall() will either return `INSTALLED` or throw an
                            exception if the installation or update did not succeed.*/
                        break;
                }
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            // User has declined to install AR Service.
            CooeeFactory.getSentryHelper().captureException(e);
        } catch (UnavailableDeviceNotCompatibleException e) {
            // Device is not supported
        }
    }
}