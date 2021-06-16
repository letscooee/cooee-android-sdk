package com.letscooee.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RestrictTo;

import com.letscooee.BuildConfig;
import com.letscooee.ContextAware;
import com.letscooee.device.AppInfo;
import com.letscooee.trigger.inapp.InAppTriggerActivity;

import io.sentry.CustomSamplingContext;
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
public class SentryHelper extends ContextAware {

    private static final String COOEE_DSN = "https://83cd199eb9134e40803220b7cca979db@o559187.ingest.sentry.io/5693686";

    private final AppInfo appInfo;
    private final ManifestReader manifestReader;
    private final User sentryUser = new User();

    private Boolean enabled;

    public SentryHelper(Context context, AppInfo appInfo, ManifestReader manifestReader) {
        super(context);
        this.appInfo = appInfo;
        this.manifestReader = manifestReader;
        this.enabled = !BuildConfig.DEBUG;
    }

    public void init() {
        Log.d(Constants.TAG, "Initializing Sentry: " + enabled.toString());
        if (!enabled) {
            return;
        }

        Sentry.setUser(sentryUser);

        SentryAndroid.init(context, options -> {
            options.setDsn(COOEE_DSN);
            options.setRelease("com.letscooee@" + BuildConfig.VERSION_NAME + "+" + BuildConfig.VERSION_CODE);
            options.setEnvironment(BuildConfig.DEBUG ? "development" : "production");

            options.setTracesSampler(context -> {
                CustomSamplingContext ctx = context.getCustomSamplingContext();
                if (ctx != null) {
                    if (InAppTriggerActivity.class.getSimpleName().equals(ctx.get("ActivityName"))) {
                        // These are important - take a big sample
                        return 0.75;
                    } else {
                        return null;
                    }
                }

                return 0.25;
            });

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
                Log.d(Constants.TAG, "Skipping Sentry event with message: " + event.getMessage());
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
        Sentry.setTag("client.appPackage", this.appInfo.getPackageName());
        Sentry.setTag("client.appVersion", this.appInfo.getVersion());
        Sentry.setTag("client.appName", this.appInfo.getName());
        Sentry.setTag("client.appId", this.manifestReader.getAppID());
        Sentry.setTag("appBuildType", this.appInfo.isDebuggable() ? "debug" : "release");
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

        if (event.getOriginThrowable() == null) {
            return false;
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Objects.requireNonNull(event.getOriginThrowable()).printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();

        return stackTrace.toLowerCase().contains("cooee");
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
        Log.e(Constants.TAG, message);
        Sentry.captureMessage(Constants.TAG + ": " + message);
    }

    /**
     * Utility method to capture exception in Sentry.
     *
     * @param throwable Throwable to log
     */
    public void captureException(Throwable throwable) {
        this.captureException("", throwable);
    }

    public void captureException(String message, Throwable throwable) {
        Log.e(Constants.TAG, message, throwable);

        if (!enabled) {
            return;
        }

        SentryId id = Sentry.captureException(throwable);
        Log.d(Constants.TAG, "Sentry id of the exception: " + id.toString());
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
