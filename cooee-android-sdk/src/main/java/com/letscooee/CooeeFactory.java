package com.letscooee;

import android.content.Context;
import androidx.annotation.RestrictTo;
import com.letscooee.device.AppInfo;
import com.letscooee.device.DeviceInfo;
import com.letscooee.network.BaseHTTPService;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.retrofit.UserAuthService;
import com.letscooee.room.task.PendingTaskService;
import com.letscooee.user.SessionManager;
import com.letscooee.utils.ManifestReader;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.SentryHelper;
import io.sentry.ITransaction;
import io.sentry.Sentry;

/**
 * A factory pattern utility class to provide the singleton instances of various classes.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CooeeFactory {

    private static boolean initialized;
    private static AppInfo appInfo;
    private static DeviceInfo deviceInfo;
    private static RuntimeData runtimeData;
    private static SentryHelper sentryHelper;
    private static SessionManager sessionManager;
    private static ManifestReader manifestReader;
    private static BaseHTTPService baseHTTPService;
    private static SafeHTTPService safeHTTPService;
    private static UserAuthService userAuthService;
    private static PendingTaskService pendingTaskService;

    private CooeeFactory() {
    }

    public synchronized static void init(Context context) {
        if (initialized) {
            return;
        }

        appInfo = AppInfo.getInstance(context);
        deviceInfo = DeviceInfo.getInstance(context);
        manifestReader = ManifestReader.getInstance(context);
        sentryHelper = new SentryHelper(context, appInfo, manifestReader);
        sentryHelper.init();

        // Sentry should be initialized first
        ITransaction transaction = Sentry.startTransaction("CooeeFactory.init()", "task");

        baseHTTPService = new BaseHTTPService(context);

        // This is required in PendingTaskService (ultimately). TODO make this a strong dependency
        userAuthService = new UserAuthService(context, sentryHelper);
        userAuthService.populateUserDataFromStorage();

        pendingTaskService = new PendingTaskService(context, sentryHelper);
        runtimeData = RuntimeData.getInstance(context);
        sessionManager = SessionManager.getInstance(context);
        safeHTTPService = new SafeHTTPService(context, pendingTaskService, sessionManager, runtimeData);

        transaction.finish();

        initialized = true;
    }

    public static AppInfo getAppInfo() {
        return appInfo;
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

    public static ManifestReader getManifestReader() {
        return manifestReader;
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

    public static UserAuthService getUserAuthService() {
        return userAuthService;
    }

    public static PendingTaskService getPendingTaskService() {
        return pendingTaskService;
    }
}
