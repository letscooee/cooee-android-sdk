package com.letscooee.utils;

import static com.letscooee.utils.Constants.TAG;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.letscooee.BuildConfig;
import java.util.Date;
import io.sentry.Breadcrumb;
import io.sentry.SentryLevel;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Logger {

    private final SentryHelper sentryHelper;

    public Logger(SentryHelper sentryHelper) {
        this.sentryHelper = sentryHelper;
    }

    /**
     * Print a {@link Log#i(String, String)} and create, track {@link SentryLevel#INFO} level {@link Breadcrumb}
     *
     * @param message Nonnull {@link String} to be logged
     */
    public void info(@NonNull String message) {
        this.info(message, "");
    }

    /**
     * Print a {@link Log#i(String, String)} and create, track {@link SentryLevel#INFO} level {@link Breadcrumb}
     *
     * @param message Nonnull {@link String} to be logged
     * @param extra   Nonnull {@link String} to be logged but not to be tracked in {@link Breadcrumb}
     */
    public void info(@NonNull String message, @NonNull String extra) {
        Log.i(TAG, message + " " + extra);
        Breadcrumb breadcrumb = new Breadcrumb(new Date());
        breadcrumb.setLevel(SentryLevel.INFO);
        breadcrumb.setCategory("info");
        breadcrumb.setMessage(message);
        this.sentryHelper.addBreadcrumb(breadcrumb);
    }

    /**
     * Logs only message to console & sentry {@link Breadcrumb}
     *
     * @param message message to be print
     */
    public void error(@NonNull String message) {
        this.error(message, null, "");
    }

    /**
     * Logs message & throwable to console But log only message to sentry {@link Breadcrumb}
     *
     * @param message   Message to logged.
     * @param throwable {@link Throwable} to be logged
     */
    public void error(@NonNull String message, @Nullable Throwable throwable) {
        this.error(message, throwable, "");
    }

    /**
     * Logs message, throwable & extra to console But log only message to sentry {@link Breadcrumb}
     *
     * @param message   Message to logged.
     * @param throwable {@link Throwable} to be logged
     * @param extra     Extra string to be added in Message Log but not to be tracked in {@link Breadcrumb}
     */
    public void error(@NonNull String message, @Nullable Throwable throwable, @NonNull String extra) {
        Log.e(TAG, message + " " + extra, throwable);
        Breadcrumb breadcrumb = new Breadcrumb(new Date());
        breadcrumb.setLevel(SentryLevel.ERROR);
        breadcrumb.setCategory("error");
        breadcrumb.setMessage(message);

        if (throwable != null) {
            breadcrumb.setData("error", throwable);
        }

        this.sentryHelper.addBreadcrumb(breadcrumb);
    }

    /**
     * Print a {@link Log#i(String, String)} and create, track {@link SentryLevel#INFO} level {@link Breadcrumb}
     *
     * @param message Nonnull {@link String} to be logged
     */
    public void verbose(@NonNull String message) {
        this.verbose(message, "");
    }

    /**
     * Print a {@link Log#i(String, String)} and create, track {@link SentryLevel#INFO} level {@link Breadcrumb}
     *
     * @param message Nonnull {@link String} to be logged
     * @param extra   Nonnull {@link String} to be logged but not to be tracked in {@link Breadcrumb}
     */
    public void verbose(@NonNull String message, @NonNull String extra) {
        Log.i(TAG, message + " " + extra);
        Breadcrumb breadcrumb = new Breadcrumb(new Date());
        breadcrumb.setLevel(SentryLevel.INFO);
        breadcrumb.setCategory("Verbose");
        breadcrumb.setMessage(message);
        this.sentryHelper.addBreadcrumb(breadcrumb);
    }

    /**
     * Print a {@link Log#i(String, String)} and create, track {@link SentryLevel#INFO} level {@link Breadcrumb}
     *
     * @param message Nonnull {@link String} to be logged
     */
    public void debug(@NonNull String message) {
        this.debug(message, "");
    }

    /**
     * Print a {@link Log#i(String, String)} and create, track {@link SentryLevel#INFO} level {@link Breadcrumb}
     *
     * @param message Nonnull {@link String} to be logged
     * @param extra   Nonnull {@link String} to be logged but not to be tracked in {@link Breadcrumb}
     */
    public void debug(@NonNull String message, @NonNull String extra) {
        Log.i(TAG, message + " " + extra);
        Breadcrumb breadcrumb = new Breadcrumb(new Date());
        breadcrumb.setLevel(SentryLevel.DEBUG);
        breadcrumb.setCategory("Debug");
        breadcrumb.setMessage(message);
        this.sentryHelper.addBreadcrumb(breadcrumb);
    }

    public void sdkDebug(@NonNull String message) {
        sdkDebug(message, "");
    }

    public void sdkDebug(@NonNull String message, @NonNull String extra) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message + " " + extra);
        }

        Breadcrumb breadcrumb = new Breadcrumb(new Date());
        breadcrumb.setLevel(SentryLevel.DEBUG);
        breadcrumb.setCategory("SDK Debug");
        breadcrumb.setMessage(message);
        this.sentryHelper.addBreadcrumb(breadcrumb);
    }

    public void warn(@NonNull String message) {
        warn(message, null, "");
    }

    public void warn(@NonNull String message, Throwable throwable) {
        warn(message, throwable, "");
    }

    public void warn(@NonNull String message, @Nullable Throwable throwable, @NonNull String extra) {
        Log.w(TAG, message + " " + extra, throwable);
        Breadcrumb breadcrumb = new Breadcrumb(new Date());
        breadcrumb.setLevel(SentryLevel.WARNING);
        breadcrumb.setCategory("Warn");
        breadcrumb.setMessage(message);
        this.sentryHelper.addBreadcrumb(breadcrumb);
    }

}
