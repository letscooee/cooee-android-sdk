package com.letscooee.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.letscooee.enums.LaunchType;
import com.letscooee.trigger.inapp.PreventBlurActivity;
import java.util.Date;

/**
 * A simple data holder class that contains runtime state of the application/SDK.
 *
 * @author Shashank Agrawal
 * @version 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class RuntimeData {

    private static RuntimeData instance;

    private Boolean inBackground = true;
    private Date lastEnterForeground;
    private Date lastEnterBackground;

    private LaunchType launchType;
    private String currentScreenName;
    private Activity currentActivity;
    private int currentActivityOrientation;

    /**
     * Current app configuration, {@code null} if app configuration is not changed since app launch.
     */
    private Configuration appCurrentConfiguration;

    public static RuntimeData getInstance(Context context) {
        if (instance == null) {
            synchronized (RuntimeData.class) {
                if (instance == null) {
                    instance = new RuntimeData(context);
                }
            }
        }

        return instance;
    }

    private RuntimeData(Context context) {

    }

    @NonNull
    public Boolean isInBackground() {
        return this.inBackground;
    }

    public void setInBackground() {
        Log.d(Constants.TAG, "App went to background");
        this.inBackground = true;
        this.lastEnterBackground = new Date();
        this.launchType = null;
    }

    public void setInForeground() {
        Log.d(Constants.TAG, "App went to foreground");
        this.inBackground = false;
        this.lastEnterForeground = new Date();
    }

    @SuppressWarnings("unused")
    public Boolean isInForeground() {
        return !this.inBackground;
    }

    /**
     * Returns <code>true</code> if the app is just launched and the "on foreground" event is executed.
     *
     * @return true or false
     */
    public Boolean isFirstForeground() {
        return this.lastEnterBackground == null;
    }

    public String getCurrentScreenName() {
        return this.currentScreenName;
    }

    @SuppressWarnings("unused")
    public Date getLastEnterBackground() {
        return this.lastEnterBackground;
    }

    public long getTimeInForegroundInSeconds() {
        return ((this.lastEnterBackground.getTime() - this.lastEnterForeground.getTime()) / 1000);
    }

    public long getTimeInBackgroundInSeconds() {
        if (this.lastEnterBackground == null) {
            return 0;
        }

        return ((new Date().getTime() - lastEnterBackground.getTime()) / 1000);
    }

    public void setCurrentScreenName(String name) {
        Log.d(Constants.TAG, "Updated screen: " + name);
        this.currentScreenName = name;
    }

    public LaunchType getLaunchType() {
        return launchType;
    }

    public void setLaunchType(LaunchType launchType) {
        if (this.launchType != null) {
            return;
        }
        this.launchType = launchType;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Keeps track of the currently active {@link Activity}.
     * Also updates the {@link #currentActivityOrientation} based on the orientation of the activity.
     *
     * @param activity The currently active {@link Activity}.
     */
    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;

        if (activity instanceof PreventBlurActivity) {
            return;
        }

        this.currentActivityOrientation = activity.getResources().getConfiguration().orientation;
    }

    /**
     * Returns previous activity orientation.
     *
     * @return previous activity orientation
     */
    public int getCurrentActivityOrientation() {
        return currentActivityOrientation;
    }

    /**
     * Returns the current {@link Configuration} of the application.
     *
     * @return The current {@link Configuration} of the application.
     */
    @Nullable
    public Configuration getAppCurrentConfiguration() {
        return appCurrentConfiguration;
    }

    /**
     * Stores app's configuration changes tracked while runtime.
     * This {@link Configuration} will mainly used for InApp orientation changes
     *
     * @param appCurrentConfiguration {@link Configuration} object of the app.
     */
    public void setAppCurrentConfiguration(Configuration appCurrentConfiguration) {
        this.appCurrentConfiguration = appCurrentConfiguration;
    }
}
