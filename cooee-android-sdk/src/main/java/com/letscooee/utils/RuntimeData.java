package com.letscooee.utils;

import android.content.Context;
import android.util.Log;
import androidx.annotation.RestrictTo;

import org.jetbrains.annotations.NotNull;

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

    private String currentScreenName;

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

    @NotNull
    public Boolean isInBackground() {
        return this.inBackground;
    }

    public void setInBackground() {
        Log.d(Constants.TAG, "App went to background");
        this.inBackground = true;
        this.lastEnterBackground = new Date();
    }

    public void setInForeground() {
        Log.d(Constants.TAG, "App went to foreground");
        this.inBackground = false;
        this.lastEnterForeground = new Date();
    }

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
}
