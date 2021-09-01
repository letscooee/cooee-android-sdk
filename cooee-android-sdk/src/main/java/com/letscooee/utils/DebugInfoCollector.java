package com.letscooee.utils;

import android.content.Context;

import androidx.annotation.RestrictTo;

import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.device.AppInfo;
import com.letscooee.device.DeviceInfo;
import com.letscooee.init.DefaultUserPropertiesCollector;

import java.util.TreeMap;

/**
 * Collect all the debug information.
 *
 * @author Ashish Gaikwad 01/09/21
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DebugInfoCollector {

    private final AppInfo appInfo;
    private final Context context;
    private final DeviceInfo deviceInfo;
    private final TreeMap<String, TreeMap<String, Object>> debugInfo;
    private final DefaultUserPropertiesCollector otherInfo;

    public DebugInfoCollector(Context context) {
        this.context = context;
        debugInfo = new TreeMap<>();
        appInfo = CooeeFactory.getAppInfo();
        deviceInfo = CooeeFactory.getDeviceInfo();
        otherInfo = new DefaultUserPropertiesCollector(context);
        init();
    }

    private void init() {
        collectDeviceInfo();
        collectUserInfo();
    }

    /**
     * Collect all User information and add it to {@link #debugInfo}
     */
    private void collectUserInfo() {
        TreeMap<String, Object> userInfo = new TreeMap<>();
        userInfo.put("User ID", LocalStorageHelper.getString(context,
                Constants.STORAGE_USER_ID, ""));
        debugInfo.put("User Info", userInfo);
    }

    /**
     * Collect all Device information and add it to {@link #debugInfo}
     */
    private void collectDeviceInfo() {
        TreeMap<String, Object> deviceInformation = new TreeMap<>();
        deviceInformation.put("Device Name", deviceInfo.getDeviceName());
        deviceInformation.put("SDK Version", BuildConfig.VERSION_NAME + "+" + BuildConfig.VERSION_CODE);
        deviceInformation.put("App Version", appInfo.getVersion());
        deviceInformation.put("Bundle ID", appInfo.getPackageName());
        deviceInformation.put("Install Date", LocalStorageHelper.getString(context,
                Constants.STORAGE_FIRST_LAUNCH_DATE, ""));
        deviceInformation.put("Build Date", appInfo.getLasBuildTime());
        String firebaseToken = LocalStorageHelper.getString(context,
                Constants.STORAGE_FB_TOKEN, "");
        deviceInformation.put("FB Token", firebaseToken);
        deviceInformation.put("Device ID", LocalStorageHelper.getString(context,
                Constants.STORAGE_DEVICE_ID, ""));
        deviceInformation.put("Resolution", otherInfo.getScreenResolution());
        debugInfo.put("Device & App Info", deviceInformation);
    }

    public TreeMap<String, TreeMap<String, Object>> getDebugInfo() {
        return debugInfo;
    }
}
