package com.letscooee.init;

import android.app.Application;

/**
 * AppController Class looks upon the lifecycle of the application, check if app is in foreground or background etc.
 *
 * @author Abhishek Taparia
 */
public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new ActivityLifecycleCallback().register(this);
    }

}
