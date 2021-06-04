package com.letscooee.device;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.annotation.RestrictTo;
import com.letscooee.ContextAware;

/**
 * A utility helper class to provide some common information about the installing/host app.
 *
 * @author Shashank Agrawal
 * @version 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AppInfo extends ContextAware {

    private static AppInfo instance;
    private final CachedInfo cachedInfo;

    private class CachedInfo {

        private boolean isDebuggable;

        CachedInfo() {
            this.isInDebugMode();
        }

        private void isInDebugMode() {
            PackageManager pm = context.getPackageManager();
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
                isDebuggable = (0 != (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
            } catch (PackageManager.NameNotFoundException e) {
                isDebuggable = false;
            }
        }
    }

    public static AppInfo getInstance(Context context) {
        if (instance == null) {
            synchronized (AppInfo.class) {
                if (instance == null) {
                    instance = new AppInfo(context);
                }
            }
        }

        return instance;
    }

    AppInfo(Context context) {
        super(context);
        this.cachedInfo = new CachedInfo();
    }

    /**
     * If the app is debuggable or in debuggable mode.
     *
     * @return device name
     */
    public boolean isDebuggable() {
        return this.cachedInfo.isDebuggable;
    }
}
