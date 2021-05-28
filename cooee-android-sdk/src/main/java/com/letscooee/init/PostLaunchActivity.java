package com.letscooee.init;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.google.gson.Gson;
import com.letscooee.BuildConfig;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerData;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.retrofit.UserAuthService;
import com.letscooee.trigger.EngagementTriggerActivity;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.SessionManager;
import io.sentry.Sentry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
public class PostLaunchActivity {

    private final Context context;
    private final DefaultUserPropertiesCollector defaultUserPropertiesCollector;

    public static int currentSessionNumber;
    private final UserAuthService userAuthService;
    private final SessionManager sessionManager;

    /**
     * Public Constructor
     *
     * @param context application context
     */
    public PostLaunchActivity(@NotNull Context context) {
        this.context = context;

        this.defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
        this.sessionManager = SessionManager.getInstance(context);

        sessionCreation();
        this.userAuthService = UserAuthService.getInstance(context);

        if (!userAuthService.hasToken()) {
            userAuthService.acquireSDKToken();
        }

        if (isAppFirstTimeLaunch()) {
            sendFirstLaunchEvent();
        } else {
            sendSuccessiveLaunchEvent();
        }

        APIClient.setDeviceName(getDeviceName());
        APIClient.setUserId(LocalStorageHelper.getString(context, CooeeSDKConstants.STORAGE_USER_ID, ""));
    }

    /**
     * Get device name
     *
     * @return device name
     */
    private String getDeviceName() {
        String name = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            name = Settings.Global.getString(context.getContentResolver(), "device_name");
        }

        if (name == null) {
            name = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
        }

        if (name == null) {
            name = Build.MODEL;
        }

        return name;
    }

    /**
     * Initialize onSDKStateDecided to get token and create new session
     */
    private void sessionCreation() {
        currentSessionNumber = sessionManager.getCurrentSessionNumber();
    }


    /**
     * Check if app is launched for first time
     *
     * @return true if app is launched for first time, else false
     */
    private boolean isAppFirstTimeLaunch() {
        if (LocalStorageHelper.getBoolean(context, CooeeSDKConstants.STORAGE_FIRST_TIME_LAUNCH, true)) {
            LocalStorageHelper.putBoolean(context, CooeeSDKConstants.STORAGE_FIRST_TIME_LAUNCH, false);
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
        sendUserProperties(userProperties);

        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put("CE Source", "SYSTEM");
        eventProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
        Event event = new Event("CE App Installed", eventProperties);

        HttpCallsHelper.sendEvent(context, event, data -> createTrigger(context, data));
    }

    /**
     * Runs every time when app is opened for a new session
     */
    private void sendSuccessiveLaunchEvent() {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("CE Session Count", currentSessionNumber + "");
        sendUserProperties(userProperties);

        String[] networkData = defaultUserPropertiesCollector.getNetworkData();
        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put("CE Source", "SYSTEM");
        eventProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
        eventProperties.put("CE SDK Version", BuildConfig.VERSION_NAME);
        eventProperties.put("CE OS Version", Build.VERSION.RELEASE);
        eventProperties.put("CE Network Provider", networkData[0]);
        eventProperties.put("CE Network Type", networkData[1]);
        eventProperties.put("CE Bluetooth On", defaultUserPropertiesCollector.isBluetoothOn());
        eventProperties.put("CE Wifi Connected", defaultUserPropertiesCollector.isConnectedToWifi());
        eventProperties.put("CE Device Battery", defaultUserPropertiesCollector.getBatteryLevel());

        Event event = new Event("CE App Launched", eventProperties);
        HttpCallsHelper.sendEvent(context, event, null);
    }

    /**
     * Sends default user properties to the server
     *
     * @param userProps additional user properties
     */
    private void sendUserProperties(Map<String, Object> userProps) {
        double[] location = defaultUserPropertiesCollector.getLocation();
        String[] networkData = defaultUserPropertiesCollector.getNetworkData();

        Map<String, Object> userProperties = new HashMap<>();
        if (userProps != null) {
            userProperties = new HashMap<>(userProps);
        }

        userProperties.put("CE OS", "ANDROID");
        userProperties.put("CE SDK Version", BuildConfig.VERSION_NAME);
        userProperties.put("CE SDK Version Code", BuildConfig.VERSION_CODE);
        userProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
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

        HttpCallsHelper.sendUserProfile(context, userMap, "SDK", null);
    }

    /**
     * Create inapp engagement trigger using map object
     *
     * @param context context of the application
     * @param data    map data received from backend
     */
    public static void createTrigger(Context context, Map<String, Object> data) {
        if (data == null || data.get("triggerData") == null) {
            return;
        }

        Gson gson = new Gson();
        TriggerData triggerData = gson.fromJson(String.valueOf(data.get("triggerData")), TriggerData.class);
        storeTriggerID(context, triggerData.getId(), triggerData.getDuration());
        createTrigger(context, triggerData);
    }

    /**
     * Create inapp engagement trigger
     *
     * @param context     context of the application
     * @param triggerData trigger data received from PN data payload or overloaded function
     */
    public static void createTrigger(Context context, TriggerData triggerData) {
        RuntimeData runtimeData = RuntimeData.getInstance(context);
        if (runtimeData.isInBackground()) {
            return;
        }

        try {
            Intent intent = new Intent(context, EngagementTriggerActivity.class);
            Bundle sendBundle = new Bundle();
            sendBundle.putParcelable("triggerData", triggerData);
            intent.putExtra("bundle", sendBundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ex) {
            Log.d(CooeeSDKConstants.LOG_PREFIX, "Couldn't show Engagement Trigger " + ex.toString());
            Sentry.captureException(ex);
        }
    }

    /**
     * Store trigger id and duration in local storage
     *
     * @param context
     * @param id
     * @param time
     * @return
     */
    public static ArrayList<HashMap<String, String>> storeTriggerID(Context context, String id, long time) {
        ArrayList<HashMap<String, String>> hashMaps = LocalStorageHelper.getList(context, CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("triggerID", id);
        hashMap.put("duration", String.valueOf(new Date().getTime() + time * 1000));

        hashMaps.add(hashMap);

        LocalStorageHelper.putListImmediately(context, CooeeSDKConstants.STORAGE_ACTIVE_TRIGGERS, hashMaps);

        return hashMaps;
    }
}
