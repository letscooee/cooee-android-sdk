package com.letscooee.init;

import android.app.Application;
import android.content.res.Configuration;
import androidx.annotation.NonNull;
import com.letscooee.CooeeFactory;

/**
 * Main application class to initialize the Cooee SDK.
 *
 * @author Abhishek Taparia
 */
public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new CooeeBootstrap(this).init();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*
         * This method will be called when the application configuration changes.
         * This will mainly help in managing InApp Orientation changes/blocking.
         * Mainly Orientation, Screen Size changes are used.
         */
        CooeeFactory.getRuntimeData().setAppCurrentConfiguration(newConfig);
    }
}
