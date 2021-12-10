package com.letscooee.user;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
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
    public NewSessionExecutor(@NonNull Context context) {
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
        Map<String, Object> deviceProperties = new HashMap<>();
        deviceProperties.put("CE First Launch Time", new Date());
        deviceProperties.putAll(getImmutableDeviceProps());
        deviceProperties.putAll(getMutableDeviceProps());

        Event event = new Event("CE App Installed");
        event.setDeviceProps(deviceProperties);
        safeHTTPService.sendEvent(event);
    }

    /**
     * Runs every time when app is opened for a new session
     */
    private void sendSuccessiveLaunchEvent() {

        Event event = new Event("CE App Launched");
        event.setDeviceProps(getMutableDeviceProps());
        safeHTTPService.sendEvent(event);
    }

    /**
     * Generate map of Non changeable device property
     *
     * @return {@link Map} of non changeable device property
     */
    public Map<String, Object> getImmutableDeviceProps() {
        Map<String, Object> deviceProperties = new HashMap<>();

        deviceProperties.put("CE Installed Time", defaultUserPropertiesCollector.getInstalledTime());
        deviceProperties.put("CE Source", "ANDROID SDK");
        deviceProperties.put("CE OS", Constants.PLATFORM);
        deviceProperties.put("CE Device Manufacturer", Build.MANUFACTURER);
        deviceProperties.put("CE Device Model", Build.MODEL);
        deviceProperties.put("CE Total Internal Memory", defaultUserPropertiesCollector.getTotalInternalMemorySize());
        deviceProperties.put("CE Total RAM", defaultUserPropertiesCollector.getTotalRAMMemorySize());
        deviceProperties.put("CE Screen Resolution", defaultUserPropertiesCollector.getScreenResolution());
        deviceProperties.put("CE DPI", defaultUserPropertiesCollector.getDpi());

        return deviceProperties;
    }

    /**
     * Creates a {@link Map} of mostly changeable device properties.
     *
     * @return {@link Map} of device properties
     */
    public Map<String, Object> getMutableDeviceProps() {
        String sdkVersion = BuildConfig.VERSION_NAME + "+" + BuildConfig.VERSION_CODE;

        Map<String, Object> deviceProperties = new HashMap<>();
        deviceProperties.put("CE App Version", appInfo.getVersion());
        deviceProperties.put("CE SDK Version", sdkVersion);
        deviceProperties.put("CE Bluetooth On", defaultUserPropertiesCollector.isBluetoothOn());
        deviceProperties.put("CE Wifi Connected", defaultUserPropertiesCollector.isConnectedToWifi());
        deviceProperties.put("CE Device Battery", defaultUserPropertiesCollector.getBatteryLevel());
        deviceProperties.put("CE Available RAM", defaultUserPropertiesCollector.getAvailableRAMMemorySize());
        deviceProperties.put("CE Device Orientation", defaultUserPropertiesCollector.getDeviceOrientation());
        deviceProperties.put("CE Session Count", sessionManager.getCurrentSessionNumber());
        deviceProperties.put("CE OS Version", Build.VERSION.RELEASE);
        deviceProperties.put("CE Available Internal Memory", defaultUserPropertiesCollector.getAvailableInternalMemorySize());
        deviceProperties.put("CE Device Locale", defaultUserPropertiesCollector.getLocale());
        deviceProperties.put("CE Last Launch Time", new Date());
        deviceProperties.putAll(permissionManager.getPermissionInformation());

        return deviceProperties;
    }
}
