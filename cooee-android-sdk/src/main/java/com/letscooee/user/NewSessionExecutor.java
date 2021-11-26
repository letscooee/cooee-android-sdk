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
import com.letscooee.network.SafeHTTPService;
import com.letscooee.permission.PermissionManager;
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
    private final SafeHTTPService safeHTTPService;
    private final PermissionManager permissionManager;

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
        this.safeHTTPService = CooeeFactory.getSafeHTTPService();
        this.sessionManager.startNewSession();
        this.permissionManager = new PermissionManager(context);
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
        userProperties.putAll(getDeviceNonChangeableProps());
        sendDefaultUserProperties(userProperties);

        Event event = new Event("CE App Installed");
        event.setDeviceProps(getCommonDeviceProperties());
        safeHTTPService.sendEvent(event);
    }

    /**
     * Runs every time when app is opened for a new session
     */
    private void sendSuccessiveLaunchEvent() {
        sendDefaultUserProperties(null);

        Event event = new Event("CE App Launched");
        event.setDeviceProps(getCommonDeviceProperties());
        safeHTTPService.sendEvent(event);
    }

    /**
     * Generate map of Non changeable device property
     *
     * @return {@link Map} of non changeable device property
     */
    public Map<String, Object> getDeviceNonChangeableProps() {
        Map<String, Object> userProperties = new HashMap<>();

        userProperties.put("CE Installed Time", defaultUserPropertiesCollector.getInstalledTime());
        userProperties.put("CE Source", "ANDROID SDK");
        userProperties.put("CE OS", "ANDROID");
        userProperties.put("CE Device Manufacturer", Build.MANUFACTURER);
        userProperties.put("CE Device Model", Build.MODEL);
        userProperties.put("CE Total Internal Memory", defaultUserPropertiesCollector.getTotalInternalMemorySize());
        userProperties.put("CE Total RAM", defaultUserPropertiesCollector.getTotalRAMMemorySize());
        userProperties.put("CE Screen Resolution", defaultUserPropertiesCollector.getScreenResolution());
        userProperties.put("CE DPI", defaultUserPropertiesCollector.getDpi());

        return userProperties;
    }

    /**
     * Creates a {@link Map} of mostly changeable device properties.
     *
     * @return {@link Map} of device properties
     */
    public Map<String, Object> getCommonDeviceProperties() {
        String sdkVersion = BuildConfig.VERSION_NAME + "+" + BuildConfig.VERSION_CODE;

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put("CE App Version", appInfo.getVersion());
        eventProperties.put("CE SDK Version", sdkVersion);
        eventProperties.put("CE Bluetooth On", defaultUserPropertiesCollector.isBluetoothOn());
        eventProperties.put("CE Wifi Connected", defaultUserPropertiesCollector.isConnectedToWifi());
        eventProperties.put("CE Device Battery", defaultUserPropertiesCollector.getBatteryLevel());
        eventProperties.put("CE Available RAM", defaultUserPropertiesCollector.getAvailableRAMMemorySize());
        eventProperties.put("CE Device Orientation", defaultUserPropertiesCollector.getDeviceOrientation());
        eventProperties.putAll(permissionManager.getPermissionInformation());

        return eventProperties;
    }

    /**
     * Sends default user properties to the server
     *
     * @param userProperties additional user properties
     */
    private void sendDefaultUserProperties(Map<String, Object> userProperties) {

        if (userProperties == null) {
            userProperties = new HashMap<>();
        }

        userProperties.putAll(getCommonDeviceProperties());
        userProperties.put("CE Session Count", sessionManager.getCurrentSessionNumber());
        userProperties.put("CE OS Version", Build.VERSION.RELEASE);
        userProperties.put("CE Available Internal Memory", defaultUserPropertiesCollector.getAvailableInternalMemorySize());
        userProperties.put("CE Device Locale", defaultUserPropertiesCollector.getLocale());
        userProperties.put("CE Last Launch Time", new Date());


        Map<String, Object> userMap = new HashMap<>();
        userMap.put("props", userProperties);

        this.safeHTTPService.updateDeviceProperty(userMap);
    }
}
