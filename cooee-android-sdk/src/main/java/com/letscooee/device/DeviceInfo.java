package com.letscooee.device;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.letscooee.ContextAware;

/**
 * A utility helper class to provide some information of the device.
 *
 * @author Shashank Agrawal
 * @version 0.3.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DeviceInfo extends ContextAware {

    private static DeviceInfo instance;
    private final CachedInfo cachedInfo;

    private class CachedInfo {

        private String name;
        private int width;
        private int height;
        private float scaledDensity;

        CachedInfo() {
            this.cacheName();

            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            width = displayMetrics.widthPixels;
            height = displayMetrics.heightPixels;
            scaledDensity = displayMetrics.scaledDensity;
        }

        private void cacheName() {
            String deviceName = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");

            if (TextUtils.isEmpty(deviceName) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                deviceName = Settings.Global.getString(context.getContentResolver(), "device_name");
            }

            if (deviceName == null) {
                deviceName = Build.MODEL;
            }

            this.name = deviceName;
        }
    }

    public static DeviceInfo getInstance(Context context) {
        if (instance == null) {
            synchronized (DeviceInfo.class) {
                if (instance == null) {
                    instance = new DeviceInfo(context);
                }
            }
        }

        return instance;
    }

    DeviceInfo(Context context) {
        super(context);
        this.cachedInfo = new CachedInfo();
    }

    /**
     * Get the name of the device.
     *
     * @return device name
     */
    @NonNull
    public String getDeviceName() {
        return this.cachedInfo.name;
    }

    public int getDisplayWidth() {
        return this.cachedInfo.width;
    }

    public int getDisplayHeight() {
        return this.cachedInfo.height;
    }

    public float getScaledDensity() {
        return this.cachedInfo.scaledDensity;
    }
}
