package com.letscooee.init;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.letscooee.BuildConfig;
import com.letscooee.models.*;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.Closure;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;

import io.reactivex.rxjava3.subjects.ReplaySubject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.ConnectException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PostLaunchActivity initialized when app is launched
 *
 * @author Abhishek Taparia
 */
public class PostLaunchActivity {

    private Context context;
    private DefaultUserPropertiesCollector defaultUserPropertiesCollector;
    private ServerAPIService apiService;

    public static ReplaySubject<Object> onSDKStateDecided;
    public static Date currentSessionStartTime;
    public static String currentSessionId = "";
    public static int currentSessionNumber;

    /**
     * Public Constructor
     *
     * @param context application context
     */
    public PostLaunchActivity(Context context) {
        if (context == null) {
            return;
        }

        this.context = context;

        this.defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
        this.apiService = APIClient.getServerAPIService();

        sessionCreation();
        //Block code to be transferred to sessionCreation function after merge
        {
        if (isAppFirstTimeLaunch()) {
            AuthenticationRequestBody authenticationRequestBody = getAuthenticationRequestBody();
            apiService.registerUser(authenticationRequestBody).enqueue(new Callback<SDKAuthentication>() {
                @Override
                public void onResponse(Call<SDKAuthentication> call, Response<SDKAuthentication> response) {

            if (response == null) {
                onSDKStateDecided.onError(new ConnectException());
                LocalStorageHelper.remove(context, CooeeSDKConstants.STORAGE_FIRST_TIME_LAUNCH);

            } else if (response.isSuccessful()) {
                assert response.body() != null;
                String sdkToken = response.body().getSdkToken();
                Log.i(CooeeSDKConstants.LOG_PREFIX, "Token : " + sdkToken);
                currentSessionId = response.body().getSessionID();

                LocalStorageHelper.putString(context, CooeeSDKConstants.STORAGE_SDK_TOKEN, sdkToken);

                APIClient.setAPIToken(sdkToken);
                notifySDKStateDecided();
                appFirstOpen();
            }
                }

                @Override
                public void onFailure(Call<SDKAuthentication> call, Throwable t) {
            onSDKStateDecided.onError(t);
                    LocalStorageHelper.remove(context, CooeeSDKConstants.STORAGE_FIRST_TIME_LAUNCH);
                }
            });
        } else {
            String apiToken = LocalStorageHelper.getString(context, CooeeSDKConstants.STORAGE_SDK_TOKEN, "");
            Log.i(CooeeSDKConstants.LOG_PREFIX, "Token : " + apiToken);

            APIClient.setAPIToken(apiToken);

            successiveAppLaunch();
        }
        }
    }

    /**
     * Initialize onSDKStateDecided to get token and create new session
     */
    private void sessionCreation() {
        onSDKStateDecided = ReplaySubject.create(1);

        currentSessionNumber = getSessionNumber();
        currentSessionStartTime = new Date();
    }

    /**
     * Notify SDK token state for ReplaySubject
     */
    private void notifySDKStateDecided() {
        onSDKStateDecided.onNext("");  // cannot send null here
        onSDKStateDecided.onComplete();
    }

    /**
     * Check if app is launched for first time
     *
     * @return true if app is launched for first time, else false
     */
    private boolean isAppFirstTimeLaunch() {
        if (LocalStorageHelper.getBoolean(context, CooeeSDKConstants.STORAGE_FIRST_TIME_LAUNCH, false)) {
            LocalStorageHelper.putBoolean(context, CooeeSDKConstants.STORAGE_FIRST_TIME_LAUNCH, false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get app credentials if passed as metadata from host application's manifest file
     *
     * @return String[]{appId,appSecret}
     */
    private String[] getAppCredentials() {
        ApplicationInfo app;

        try {
            app = this.context.getPackageManager().getApplicationInfo(this.context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new String[]{null, null};
        }

        Bundle bundle = app.metaData;
        String appId = bundle.getString("COOEE_APP_ID");
        String appSecret = bundle.getString("COOEE_APP_SECRET");
        return new String[]{appId, appSecret};
    }

    /**
     * returns  AuthenticationRequestBody to be used in observer
     *
     * @return AuthenticationRequestBody
     */
    private AuthenticationRequestBody getAuthenticationRequestBody() {
        String[] appCredentials = getAppCredentials();
        return new AuthenticationRequestBody(
                appCredentials[0],
                appCredentials[1],
                new DeviceData("ANDROID",
                        BuildConfig.VERSION_NAME + "",
                        defaultUserPropertiesCollector.getAppVersion(),
                        Build.VERSION.RELEASE));
    }

    /**
     * Runs when app is opened for the first time after sdkToken is received from server asynchronously
     */
    private void appFirstOpen() {
        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("CE First Launch Time", new Date().toString());
        userProperties.put("CE Installed Time", defaultUserPropertiesCollector.getInstalledTime());
        sendUserProperties(userProperties);

        Map<String, String> eventProperties = new HashMap<>();
        eventProperties.put("CE Source", "SYSTEM");
        eventProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
        Event event = new Event("CE App Installed", eventProperties);

        HttpCallsHelper.sendEvent(event,"App Installed");
    }

    /**
     * Runs every time when app is opened for a new session
     */
    private void successiveAppLaunch() {
        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("CE Session Count", currentSessionNumber + "");
        sendUserProperties(userProperties);

        String[] networkData = defaultUserPropertiesCollector.getNetworkData();
        Map<String, String> eventProperties = new HashMap<>();
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
        HttpCallsHelper.sendEventWithoutSDKState(event, "App Launched", new Closure() {
            @Override
            public void call(Map<String, Object> data) {
                if (data != null && data.get("sessionID") != null) {
                    currentSessionId = String.valueOf(data.get("sessionID"));
                }
                notifySDKStateDecided();
                Log.d("test","data"+ data.toString());
            }
        });
    }

    /**
     * Sends default user properties to the server
     *
     * @param userProps additional user properties
     */
    private void sendUserProperties(Map<String, String> userProps) {
            //Fix indentation after merge
            String[] location = defaultUserPropertiesCollector.getLocation();
            String[] networkData = defaultUserPropertiesCollector.getNetworkData();

            Map<String, String> userProperties = new HashMap<>();
            if (userProps != null) {
                userProperties = new HashMap<>(userProps);
            }

            userProperties.put("CE OS", "ANDROID");
            userProperties.put("CE SDK Version", BuildConfig.VERSION_NAME);
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
            userProperties.put("CE Last Launch Time", new Date().toString());
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userProperties", userProperties);
            userMap.put("sessionID", currentSessionId);
            userMap.put("userData", new HashMap<>());

            HttpCallsHelper.sendUserProfile(userMap,"SDK");
    }

    /**
     * Create or get next session number from shared preference
     *
     * @return next session number
     */
    private int getSessionNumber() {
        int sessionNumber = LocalStorageHelper.getInt(context, CooeeSDKConstants.STORAGE_SESSION_NUMBER,0);

        if (sessionNumber >= 0) {
            sessionNumber += 1;
        }

        LocalStorageHelper.putInt(context, CooeeSDKConstants.STORAGE_SESSION_NUMBER, sessionNumber);

        return sessionNumber;
    }
}
