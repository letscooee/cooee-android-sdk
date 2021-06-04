package com.letscooee;

import android.content.Context;
import androidx.annotation.RestrictTo;
import com.letscooee.device.DeviceInfo;
import com.letscooee.network.BaseHTTPService;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.user.SessionManager;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.SentryHelper;

/**
 * A factory pattern utility class to provide the singleton instances of various classes.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CooeeFactory {

    private static DeviceInfo deviceInfo;
    private static RuntimeData runtimeData;
    private static SentryHelper sentryHelper;
    private static SessionManager sessionManager;
    private static BaseHTTPService baseHTTPService;
    private static SafeHTTPService safeHTTPService;

    private CooeeFactory() {
    }

    public static void init(Context context) {
        deviceInfo = DeviceInfo.getInstance(context);
        runtimeData = RuntimeData.getInstance(context);
        sentryHelper = SentryHelper.getInstance(context);
        sessionManager = SessionManager.getInstance(context);
        baseHTTPService = new BaseHTTPService(context);
        safeHTTPService = new SafeHTTPService(context);
    }

    public static DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public static RuntimeData getRuntimeData() {
        return runtimeData;
    }

    public static SentryHelper getSentryHelper() {
        return sentryHelper;
    }

    public static SessionManager getSessionManager() {
        return sessionManager;
    }

    public static BaseHTTPService getBaseHTTPService() {
        return baseHTTPService;
    }

    public static SafeHTTPService getSafeHTTPService() {
        return safeHTTPService;
    }
}
