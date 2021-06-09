package com.letscooee.user;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RestrictTo;

import com.letscooee.BuildConfig;
import com.letscooee.ContextAware;
import com.letscooee.CooeeFactory;
import com.letscooee.device.AppInfo;
import com.letscooee.init.DefaultUserPropertiesCollector;
import com.letscooee.models.Event;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PostLaunchActivity initialized when app is launched
 *
 * @author Abhishek Taparia
 * @version 0.0.2
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class NewSessionExecutor extends ContextAware {

    private final DefaultUserPropertiesCollector defaultUserPropertiesCollector;
    private final AppInfo appInfo;
    private final SessionManager sessionManager;

    /**
     * Public Constructor
     *
     * @param context application context
     */
    public NewSessionExecutor(@NotNull Context context) {
        super(context);
        this.defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
        this.sessionManager = SessionManager.getInstance(context);
        this.appInfo = CooeeFactory.getAppInfo();
    }

    public void execute() {
        if (isAppFirstTimeLaunch()) {
            sendFirstLaunchEvent();
        } else {
            sendSuccessiveLaunchEvent();
        }
    }

    /**
     * Check if app is launched for first time
     *
     * @return true if app is launched for first time, else false
     */
    private boolean isAppFirstTimeLaunch() {
        if (LocalStorageHelper.getBoolean(context, Constants.STORAGE_FIRST_TIME_LAUNCH, true)) {
            LocalStorageHelper.putBoolean(context, Constants.STORAGE_FIRST_TIME_LAUNCH, false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Runs when app is opened for the first time after sdkToken is received from server asynchronously
     */
    private void sendFirstLaunchEvent() {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("CE First Launch Time", new Date());
        userProperties.put("CE Installed Time", defaultUserPropertiesCollector.getInstalledTime());
        sendDefaultUserProperties(userProperties);

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put("CE Source", "SYSTEM");
        eventProperties.put("CE App Version", appInfo.getVersion());
        Event event = new Event("CE App Installed", eventProperties);

        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }

    /**
     * Runs every time when app is opened for a new session
     */
    private void sendSuccessiveLaunchEvent() {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("CE Session Count", sessionManager.getCurrentSessionNumber());
        sendDefaultUserProperties(userProperties);

        String[] networkData = defaultUserPropertiesCollector.getNetworkData();
        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put("CE Source", "SYSTEM");
        eventProperties.put("CE App Version", appInfo.getVersion());
        eventProperties.put("CE SDK Version", BuildConfig.VERSION_NAME);
        eventProperties.put("CE OS Version", Build.VERSION.RELEASE);
        eventProperties.put("CE Network Provider", networkData[0]);
        eventProperties.put("CE Network Type", networkData[1]);
        eventProperties.put("CE Bluetooth On", defaultUserPropertiesCollector.isBluetoothOn());
        eventProperties.put("CE Wifi Connected", defaultUserPropertiesCollector.isConnectedToWifi());
        eventProperties.put("CE Device Battery", defaultUserPropertiesCollector.getBatteryLevel());

        Event event = new Event("CE App Launched", eventProperties);
        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }

    /**
     * Sends default user properties to the server
     *
     * @param userProps additional user properties
     */
    private void sendDefaultUserProperties(Map<String, Object> userProps) {
        double[] location = defaultUserPropertiesCollector.getLocation();
        String[] networkData = defaultUserPropertiesCollector.getNetworkData();

        Map<String, Object> userProperties;
        if (userProps != null) {
            userProperties = new HashMap<>(userProps);
        } else {
            userProperties = new HashMap<>();
        }

        userProperties.put("CE OS", "ANDROID");
        userProperties.put("CE SDK Version", BuildConfig.VERSION_NAME);
        userProperties.put("CE SDK Version Code", BuildConfig.VERSION_CODE);
        userProperties.put("CE App Version", appInfo.getVersion());
        userProperties.put("CE OS Version", Build.VERSION.RELEASE);
        userProperties.put("CE Device Manufacturer", Build.MANUFACTURER);
        userProperties.put("CE Device Model", Build.MODEL);
        userProperties.put("CE Latitude", location[0]);
        userProperties.put("CE Longitude", location[1]);
        userProperties.put("CE Network Operator", networkData[0]);
        userProperties.put("CE Network Type", networkData[1]);
        userProperties.put("CE Bluetooth On", defaultUserPropertiesCollector.isBluetoothOn());
        userProperties.put("CE Wifi Connected", defaultUserPropertiesCollector.isConnectedToWifi());
        userProperties.put("CE Available Internal Memory", defaultUserPropertiesCollector.getAvailableInternalMemorySize());
        userProperties.put("CE Total Internal Memory", defaultUserPropertiesCollector.getTotalInternalMemorySize());
        userProperties.put("CE Available RAM", defaultUserPropertiesCollector.getAvailableRAMMemorySize());
        userProperties.put("CE Total RAM", defaultUserPropertiesCollector.getTotalRAMMemorySize());
        userProperties.put("CE Device Orientation", defaultUserPropertiesCollector.getDeviceOrientation());
        userProperties.put("CE Device Battery", defaultUserPropertiesCollector.getBatteryLevel());
        userProperties.put("CE Screen Resolution", defaultUserPropertiesCollector.getScreenResolution());
        userProperties.put("CE DPI", defaultUserPropertiesCollector.getDpi());
        userProperties.put("CE Device Locale", defaultUserPropertiesCollector.getLocale());
        userProperties.put("CE Last Launch Time", new Date());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userProperties", userProperties);
        userMap.put("userData", new HashMap<>());

        CooeeFactory.getSafeHTTPService().updateUserProfile(userMap);
    }
}
