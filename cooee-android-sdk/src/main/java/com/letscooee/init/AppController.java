package com.letscooee.init;

import android.app.Application;

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

}
