package com.letscooee.utils;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.letscooee.init.DefaultUserPropertiesCollector;

import static com.letscooee.utils.CooeeSDKConstants.LOG_PREFIX;

/**
 * @author Abhishek Taparia
 * AppController Class looks upon the lifecycle of the application, check if app is in foreground or background etc.
 */
public class AppController extends Application implements LifecycleObserver {

    private static AppController mInstance;
    private ValueChangeListener visibilityChangeListener;

    public static AppController getInstance() {
        return mInstance;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d(LOG_PREFIX + "AppController", "Foreground");
        isAppInBackground(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d(LOG_PREFIX + "AppController", "Background");
        isAppInBackground(true);
    }

    public void setOnVisibilityChangeListener(ValueChangeListener listener) {
        this.visibilityChangeListener = listener;
    }

    private void isAppInBackground(Boolean isBackground) {
        if (null != visibilityChangeListener) {
            visibilityChangeListener.onChanged(isBackground);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        // addObserver
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    // Adding some callbacks for test and log
    public interface ValueChangeListener {
        void onChanged(Boolean value);
    }
}
