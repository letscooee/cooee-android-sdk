package com.letscooee.device;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.RestrictTo;

import com.letscooee.ContextAware;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        private final String name;
        private final String version;
        private final String packageName;
        private final boolean isDebuggable;
        private final long lastUpdateTime;
        private final long firstInstallTime;

        CachedInfo() {
            this.name = this.getAppName();
            this.isDebuggable = this.isInDebugMode();

            PackageInfo packageInfo = getPackageInfo();
            assert packageInfo != null;

            this.version = packageInfo.versionName + "+" + packageInfo.versionCode;
            this.packageName = packageInfo.packageName;
            this.lastUpdateTime = packageInfo.lastUpdateTime;
            this.firstInstallTime = packageInfo.firstInstallTime;
        }

        private PackageInfo getPackageInfo() {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }

        private String getAppName() {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            int stringId = applicationInfo.labelRes;
            return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
        }

        private boolean isInDebugMode() {
            try {
                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
                return (0 != (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
            } catch (PackageManager.NameNotFoundException e) {
                return false;
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

    /**
     * @return Name of the app as end-user see.
     */
    public String getName() {
        return this.cachedInfo.name;
    }

    public String getPackageName() {
        return this.cachedInfo.packageName;
    }

    public String getVersion() {
        return this.cachedInfo.version;
    }

    /**
     * Provide last build date
     *
     * @return date in {@link String}
     */
    public String getLasBuildTime() {
        return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH)
                .format(new Date(this.cachedInfo.lastUpdateTime));
    }

    /**
     * Provide App install date
     *
     * @return date in {@link String}
     */
    public String getFirstInstallTime() {
        return new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH)
                .format(new Date(this.cachedInfo.firstInstallTime));
    }
}
