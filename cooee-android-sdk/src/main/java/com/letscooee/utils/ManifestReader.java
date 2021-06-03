package com.letscooee.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.RestrictTo;
import io.sentry.Sentry;

/**
 * Utility class which provides reading the ApplicationManifest.xml.
 *
 * @author Shashank Agrawal
 * @version 0.2.10
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ManifestReader {

    private static ManifestReader instance;

    private String appID = "";
    private String appSecret = "";

    public synchronized static ManifestReader getInstance(Context context) {
        if (instance == null) {
            instance = new ManifestReader(context);
        }
        return instance;
    }

    private ManifestReader(Context context) {
        this.init(context);
    }

    private void init(Context context) {
        ApplicationInfo appInfo;

        try {
            PackageManager pm = context.getPackageManager();
            appInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.LOG_PREFIX, "Unable to read ApplicationManifest", e);
            // Not using SentryHelper to prevent deadlock
            Sentry.captureException(e);

            return;
        }

        Bundle bundle = appInfo.metaData;
        this.appID = bundle.getString("COOEE_APP_ID");
        this.appSecret = bundle.getString("COOEE_APP_SECRET");
    }

    public String getAppID() {
        return this.appID;
    }

    public String getAppSecret() {
        return this.appSecret;
    }
}
