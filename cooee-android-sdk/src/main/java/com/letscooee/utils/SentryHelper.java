package com.letscooee.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.letscooee.BuildConfig;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.android.core.SentryAndroid;
import io.sentry.protocol.SentryId;
import io.sentry.protocol.User;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for Sentry initialization, logging & other utility.
 *
 * @author Ashish Gaikwad on 21/05/21
 * @version 0.2.10
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SentryHelper {

    private static final String COOEE_DSN = "https://83cd199eb9134e40803220b7cca979db@o559187.ingest.sentry.io/5693686";

    @SuppressLint("StaticFieldLeak")
    private static SentryHelper INSTANCE;

    private final Context context;
    private final User sentryUser = new User();

    private Boolean enabled;

    private SentryHelper(Context context) {
        this.context = context.getApplicationContext();
        this.enabled = !BuildConfig.DEBUG;
        this.init();
    }

    public static SentryHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SentryHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SentryHelper(context);
                }
            }
        }

        return INSTANCE;
    }

    private void init() {
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Initializing Sentry: " + enabled.toString());
        if (!enabled) {
            return;
        }

        Sentry.setUser(sentryUser);

        SentryAndroid.init(context, options -> {
            options.setDsn(COOEE_DSN);
            options.setRelease("com.letscooee@" + BuildConfig.VERSION_NAME + "+" + BuildConfig.VERSION_CODE);
            options.setEnvironment(BuildConfig.DEBUG ? "development" : "production");

            this.setupFilterToExcludeNonCooeeEvents(options);
        });

        this.setupGlobalTags();
    }

    /**
     * Side effect of adding Sentry to an SDK is that the exceptions from the app is also gathered in our Sentry dashboard.
     *
     * @param options Sentry options
     * @see <a href="https://forum.sentry.io/t/restrict-sentry-events-just-from-the-android-sdk-library/13977">Forum Post</a>
     */
    private void setupFilterToExcludeNonCooeeEvents(SentryOptions options) {
        options.setBeforeSend((event, hint) -> {
            if (!containsWordCooee(event)) {
                Log.d(CooeeSDKConstants.LOG_PREFIX, "Skipping Sentry event with message: " + event.getMessage());
                return null;
            }

            // Additional check to prevent sending events in the local debug mode of SDK
            if (BuildConfig.DEBUG) {
                return null;
            }

            return event;
        });
    }

    /**
     * Adds some global tags to each event.
     */
    private void setupGlobalTags() {
        Sentry.setTag("client.appPackage", getAppPackage());
        Sentry.setTag("client.appVersion", getAppVersion());
        Sentry.setTag("client.appName", getApplicationName());
        Sentry.setTag("client.appId", Objects.requireNonNull(getCooeeAppID()));
        Sentry.setTag("appBuildType", isAppInDebugMode() ? "debug" : "release");
    }

    /**
     * Get host application package name
     *
     * @return app package name
     */
    private String getAppPackage() {
        PackageInfo packageInfo = null;

        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            this.captureException(e);
        }

        assert packageInfo != null;
        return packageInfo.packageName;
    }

    public String getAppVersion() {
        PackageInfo packageInfo = null;

        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            this.captureException(e);
        }

        assert packageInfo != null;
        return packageInfo.versionName;
    }

    /**
     * Checks if the event message or stacktrace contains the word "Cooee" (case insensitive).
     *
     * @param event will be SentryEvent
     */
    private boolean containsWordCooee(SentryEvent event) {
        if (event.getMessage() != null) {
            if (event.getMessage().getFormatted().toLowerCase().contains("cooee")) {
                return true;
            }
        }

        if (event.getThrowable() == null) {
            return false;
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Objects.requireNonNull(event.getThrowable()).printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();

        return stackTrace.toLowerCase().contains("cooee");
    }

    /**
     * Get app's name.
     *
     * @return app name
     */
    private String getApplicationName() {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    /**
     * Checks if the installing app is in debug or in release mode.
     *
     * @return true ot false
     */
    public boolean isAppInDebugMode() {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            // Debuggable variable will remain false
        }

        return debuggable;
    }

    /**
     * Get Cooee's client app ID from host application's manifest file
     *
     * @return String[]{appId,appSecret}
     */
    private String getCooeeAppID() {
        ApplicationInfo app;

        try {
            app = this.context.getPackageManager().getApplicationInfo(this.context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Sentry.captureException(e);
            return null;
        }

        Bundle bundle = app.metaData;

        return bundle.getString("COOEE_APP_ID");
    }

    /**
     * This is a utility method which can be used while building the tester app to enable the Sentry notifications even
     * on debug mode of the SDK.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public void enableSentryForDevelopment() {
        this.enabled = true;
        this.init();
    }

    /**
     * Capture any plain message to Sentry. This method prefix the message with the word "Cooee" so that
     * {@link #setupFilterToExcludeNonCooeeEvents(SentryOptions)} can pass the validation and send it to Sentry.
     *
     * @param message Any custom message to send.
     */
    public void captureMessage(String message) {
        Log.e(CooeeSDKConstants.LOG_PREFIX, message);
        Sentry.captureMessage(CooeeSDKConstants.LOG_PREFIX + ": " + message);
    }

    /**
     * Utility
     *
     * @param throwable
     */
    public void captureException(Throwable throwable) {
        throwable.printStackTrace();

        if (!enabled) {
            return;
        }

        SentryId id = Sentry.captureException(throwable);
        Log.d(CooeeSDKConstants.LOG_PREFIX, "Sentry id of the exception: " + id.toString());
    }

    /**
     * Set Cooee's User id to Sentry's {@link User} so that this information can be shown in
     * the Sentry dashboard as well.
     *
     * @param id Identify of the Cooee's User.
     */
    public void setUserId(String id) {
        sentryUser.setId(id);
    }

    /**
     * Set additional Cooee's User information to Sentry's {@link User} so that this information can be shown in
     * the Sentry dashboard as well. Sentry is already GDPR compliant.
     *
     * @param userData Additional user data which may contain <code>mobile</code>, <code>name</code> or <code>mobile</code>.
     */
    public void setUserInfo(Map<String, Object> userData) {
        if (userData == null) {
            return;
        }

        Object name = userData.get("name");
        if (name != null && !TextUtils.isEmpty(name.toString())) {
            sentryUser.setUsername(name.toString());
        }

        Object email = userData.get("email");
        if (email != null && !TextUtils.isEmpty(email.toString())) {
            sentryUser.setEmail(email.toString());
        }

        Object mobile = userData.get("mobile");
        if (mobile != null && !TextUtils.isEmpty(mobile.toString())) {
            Map<String, String> userDataExtra = new HashMap<>();
            userDataExtra.put("mobile", mobile.toString());
            sentryUser.setOthers(userDataExtra);
        }
    }
}
