package com.letscooee.user;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.letscooee.ContextAware;
import com.letscooee.CooeeFactory;
import com.letscooee.device.DeviceInfo;
import com.letscooee.enums.WrapperType;
import com.letscooee.init.DefaultUserPropertiesCollector;
import com.letscooee.models.Event;
import com.letscooee.models.WrapperDetails;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.permission.PermissionManager;
import com.letscooee.utils.Constants;
import com.letscooee.utils.DateUtils;
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
    private final SafeHTTPService safeHTTPService;
    private final PermissionManager permissionManager;
    private final DeviceInfo deviceInfo;
    private static WrapperDetails wrapper;

    /**
     * Public Constructor
     *
     * @param context application context
     */
    public NewSessionExecutor(@NonNull Context context) {
        super(context);
        this.defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
        this.safeHTTPService = CooeeFactory.getSafeHTTPService();
        this.permissionManager = new PermissionManager(context);
        this.deviceInfo = CooeeFactory.getDeviceInfo();
    }

    public void execute() {
        if (isAppFirstTimeLaunch()) {
            sendFirstLaunchEvent();
        } else {
            sendSuccessiveLaunchEvent();
        }

        updateWrapperInformation();
    }

    /**
     * Generate instance of {@link WrapperDetails} with the provided values
     *
     * @param wrapperType   wrapper type
     * @param versionCode   wrapper version code
     * @param versionNumber wrapper version number
     */
    @SuppressWarnings("unused")
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void updateWrapperInformation(WrapperType wrapperType, int versionCode, String versionNumber) {
        if (wrapperType == null || versionCode == 0 || TextUtils.isEmpty(versionNumber)) {
            return;
        }

        wrapper = new WrapperDetails(versionCode, versionNumber, wrapperType);
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
        Event event = new Event("CE App Installed");
        event.setDeviceProps(getMutableDeviceProps());
        safeHTTPService.sendEvent(event);
    }

    /**
     * Update device props with default values
     */
    private void updateWrapperInformation() {
        if (wrapper == null) {
            return;
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("wrp", wrapper);

        safeHTTPService.updateDeviceProperty(requestData);
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

        Map<String, Object> display = new HashMap<>();
        display.put("w", deviceInfo.getDisplayWidth());
        display.put("h", deviceInfo.getDisplayHeight());
        display.put("dpi", deviceInfo.getDpi());
        deviceProperties.put("display", display);

        Map<String, Object> device = new HashMap<>();
        device.put("model", Build.MODEL);
        device.put("vendor", Build.MANUFACTURER);
        deviceProperties.put("device", device);

        deviceProperties.put("iTime", defaultUserPropertiesCollector.getInstalledTime());
        deviceProperties.put("flTime", DateUtils.getStringDateFromDate(new Date(), Constants.DATE_FORMAT_UTC, true));

        return deviceProperties;
    }

    /**
     * Creates a {@link Map} of mostly changeable device properties.
     *
     * @return {@link Map} of device properties
     */
    public Map<String, Object> getMutableDeviceProps() {
        Map<String, Object> deviceProperties = new HashMap<>();

        Map<String, Object> storage = new HashMap<>();
        storage.put("tot", defaultUserPropertiesCollector.getTotalInternalMemorySize());
        storage.put("avl", defaultUserPropertiesCollector.getAvailableInternalMemorySize());
        deviceProperties.put("storage", storage);

        Map<String, Object> memory = new HashMap<>();
        memory.put("tot", defaultUserPropertiesCollector.getTotalRAMMemorySize());
        memory.put("avl", defaultUserPropertiesCollector.getAvailableRAMMemorySize());
        deviceProperties.put("mem", memory);

        Map<String, Object> os = new HashMap<>();
        os.put("ver", Build.VERSION.RELEASE);
        os.put("name", Constants.PLATFORM);
        deviceProperties.put("os", os);

        deviceProperties.put("bat", defaultUserPropertiesCollector.getBatteryInfo());
        deviceProperties.put("locale", defaultUserPropertiesCollector.getLocale());
        deviceProperties.put("bt", defaultUserPropertiesCollector.isBluetoothOn());
        deviceProperties.put("wifi", defaultUserPropertiesCollector.isConnectedToWifi());
        deviceProperties.put("orientation", defaultUserPropertiesCollector.getDeviceOrientation());

        deviceProperties.putAll(permissionManager.getPermissionInformation());

        return deviceProperties;
    }
}
