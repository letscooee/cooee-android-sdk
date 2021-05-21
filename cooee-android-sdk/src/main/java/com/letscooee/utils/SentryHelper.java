package com.letscooee.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.RestrictTo;

import com.letscooee.BuildConfig;
import com.letscooee.init.DefaultUserPropertiesCollector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.android.core.SentryAndroid;

/**
 * Initialise the sentry
 *
 * @author Ashish Gaikwad on 21/05/21
 * @version 0.1
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SentryHelper {

    private static SentryHelper sentryHelper;
    private static Context context;
    private static DefaultUserPropertiesCollector defaultUserPropertiesCollector;

    private SentryHelper(Context context) {
        this.context = context;
        defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
    }

    /**
     * Initialize Sentry with Manual initialization
     */
    public static SentryHelper getInstance(Context context) {
        SentryHelper.context = context;
        if (sentryHelper == null) {
            sentryHelper = new SentryHelper(context);
            SentryAndroid.init(context, options -> {
                if (BuildConfig.DEBUG) {
                    options.setDsn("");
                } else {
                    options.setDsn("https://83cd199eb9134e40803220b7cca979db@o559187.ingest.sentry.io/5693686");
                }

                options.setRelease("com.letscooee@" + BuildConfig.VERSION_NAME + "+" + BuildConfig.VERSION_CODE);
                options.setEnvironment(BuildConfig.BUILD_TYPE);

                options.setBeforeSend((event, hint) -> {

                    if (isNotCooeeException(event)) {
                        return null;
                    }

                    if (BuildConfig.DEBUG) {
                        return null;
                    }

                    return event;
                });
            });

            setupSentryTags();
        }

        return sentryHelper;
    }

    /**
     * Adds Manual tags to Sentry
     */
    private static void setupSentryTags() {
        Sentry.setTag("client.appPackage", defaultUserPropertiesCollector.getAppPackage());
        Sentry.setTag("client.appVersion", defaultUserPropertiesCollector.getAppVersion());
        Sentry.setTag("client.appName", getApplicationName());
        Sentry.setTag("client.appId", Objects.requireNonNull(getAppCredentials()));
        if (isDebuggable()) {
            Sentry.setTag("buildType", "debug");
        } else {
            Sentry.setTag("buildType", "release");
        }
    }

    /**
     * Filters the exceptions before sending
     *
     * @param event will be SentryEvent
     */
    private static boolean isNotCooeeException(SentryEvent event) {
        boolean isCooeeException = false;
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Objects.requireNonNull(event.getThrowable()).printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();
        if (stackTrace.toLowerCase().contains("cooee")) {
            isCooeeException = true;
        }
        if (event.getMessage() != null) {
            if (event.getMessage().getFormatted().toLowerCase().contains("cooee")) {
                isCooeeException = true;
            }
        }
        return !isCooeeException;
    }

    /**
     * Get app name
     *
     * @return app name
     */
    public static String getApplicationName() {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }


    /**
     * Checks if app is in debug or in release mode
     *
     * @return true ot false
     */
    private static boolean isDebuggable() {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /*debuggable variable will remain false*/
        }

        return debuggable;
    }

    /**
     * Get app credentials if passed as metadata from host application's manifest file
     *
     * @return String[]{appId,appSecret}
     */
    private static String getAppCredentials() {
        ApplicationInfo app;

        try {
            app = SentryHelper.context.getPackageManager().getApplicationInfo(SentryHelper.context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            Sentry.captureException(e);
            return null;
        }

        Bundle bundle = app.metaData;

        return bundle.getString("COOEE_APP_ID");
    }
}
