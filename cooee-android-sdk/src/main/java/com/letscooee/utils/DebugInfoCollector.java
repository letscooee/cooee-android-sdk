package com.letscooee.utils;

import android.content.Context;

import androidx.annotation.RestrictTo;

import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.device.AppInfo;
import com.letscooee.device.DeviceInfo;
import com.letscooee.models.DebugInformation;
import com.letscooee.pushnotification.PushProviderUtils;

import java.util.ArrayList;
import java.util.List;

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
    private final List<DebugInformation> deviceInformation;
    private final List<DebugInformation> userInformation;

    public DebugInfoCollector(Context context) {
        this.context = context;
        deviceInformation = new ArrayList<>();
        userInformation = new ArrayList<>();
        appInfo = CooeeFactory.getAppInfo();
        deviceInfo = CooeeFactory.getDeviceInfo();
        init();
    }

    /**
     * Collects device info and user info
     */
    private void init() {
        collectDeviceInfo();
        collectUserInfo();
    }

    /**
     * Collect all User information and add it to {@link #userInformation}
     */
    private void collectUserInfo() {
        DebugInformation userId = new DebugInformation("User ID",
                LocalStorageHelper.getString(context, Constants.STORAGE_USER_ID, ""),
                true);
        userInformation.add(userId);
    }

    /**
     * Collect all Device information and add it to {@link #deviceInformation}
     */
    private void collectDeviceInfo() {
        deviceInformation.add(new DebugInformation("Device Name", deviceInfo.getDeviceName()));
        deviceInformation.add(new DebugInformation("SDK Version",
                BuildConfig.VERSION_NAME + "+" + BuildConfig.VERSION_CODE));
        deviceInformation.add(new DebugInformation("App Version", appInfo.getVersion()));
        deviceInformation.add(new DebugInformation("Bundle ID", appInfo.getPackageName()));
        deviceInformation.add(new DebugInformation("Install Date", appInfo.getFirstInstallTime()));
        deviceInformation.add(new DebugInformation("Build Date", appInfo.getLasBuildTime()));
        deviceInformation.add(new DebugInformation("FB Token",
                PushProviderUtils.getLastSentToken(), true));
        deviceInformation.add(new DebugInformation("Device ID", LocalStorageHelper.getString(context,
                Constants.STORAGE_DEVICE_ID, ""), true));
        deviceInformation.add(new DebugInformation("Resolution", deviceInfo.getDisplayWidth() + "x" +
                deviceInfo.getDisplayHeight()));
    }

    public List<DebugInformation> getDeviceInformation() {
        return deviceInformation;
    }

    public List<DebugInformation> getUserInformation() {
        return userInformation;
    }
}
