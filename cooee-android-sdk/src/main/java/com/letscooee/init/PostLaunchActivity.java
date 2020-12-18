package com.letscooee.init;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.letscooee.BuildConfig;
import com.letscooee.async.AuthSyncNetwork;
import com.letscooee.models.*;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
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
import java.util.UUID;
import java.util.concurrent.ExecutionException;


/**
 * PostLaunchActivity initialized when app is launched
 *
 * @author Abhishek Taparia
 */
public class PostLaunchActivity {

    private Context context;
    private DefaultUserPropertiesCollector defaultUserPropertiesCollector;
    private ServerAPIService apiService;

    public static ReplaySubject<Object> onSDKStateDecided = ReplaySubject.create(1);
    public static String currentSessionStartTime = "";
    public static String currentSessionId = "";
    public static String currentSessionNumber = "";

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

        if (isAppFirstTimeLaunch()) {
            AuthenticationRequestBody authenticationRequestBody = getAuthenticationRequestBody();

            Response<SDKAuthentication> response = null;

            try {
                response = new AuthSyncNetwork().execute(authenticationRequestBody).get();
            } catch (ExecutionException | InterruptedException e) {
                onSDKStateDecided.onError(e);
                LocalStorageHelper.remove(context, CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH);
            }

            if (response == null) {
                onSDKStateDecided.onError(new ConnectException());
                LocalStorageHelper.remove(context, CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH);

            } else if (response.isSuccessful()) {
                assert response.body() != null;
                String sdkToken = response.body().getSdkToken();
                Log.i(CooeeSDKConstants.LOG_PREFIX, "Token : " + sdkToken);

                LocalStorageHelper.putString(context, CooeeSDKConstants.SDK_TOKEN, sdkToken);
                appFirstOpen();

                APIClient.setAPIToken(sdkToken);
                onSDKStateDecided.onNext(""); // cannot send null here
                onSDKStateDecided.onComplete();
            }
        } else {
            String apiToken = LocalStorageHelper.getString(context, CooeeSDKConstants.SDK_TOKEN, "");
            Log.i(CooeeSDKConstants.LOG_PREFIX, "Token : " + apiToken);

            APIClient.setAPIToken(apiToken);
            onSDKStateDecided.onNext("");  // cannot send null here
            onSDKStateDecided.onComplete();
            successiveAppLaunch();
        }
    }

    /**
     * Check if app is launched for first time
     *
     * @return true if app is launched for first time, else false
     */
    private boolean isAppFirstTimeLaunch() {
        if (LocalStorageHelper.getBoolean(context, CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, true)) {
            LocalStorageHelper.putBoolean(context, CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, false);
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
        createSession();

        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("CE First Launch Time", new Date().toString());
        userProperties.put("CE Installed Time", defaultUserPropertiesCollector.getInstalledTime());
        sendUserProperties(userProperties);

        Map<String, String> eventProperties = new HashMap<>();
        eventProperties.put("CE Source", "SYSTEM");
        eventProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
        eventProperties.put("CE Session ID", currentSessionId);
        eventProperties.put("CE Session Number", currentSessionNumber);
        eventProperties.put("CE Screen Name", AppController.currentScreen);
        Event event = new Event("CE App Installed", eventProperties);

        sendEvent(event);
    }

    /**
     * Runs every time when app is opened for a new session
     */
    private void successiveAppLaunch() {
        createSession();

        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("CE Session Count", currentSessionNumber);
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
        eventProperties.put("CE Session ID", currentSessionId);
        eventProperties.put("CE Session Number", currentSessionNumber);
        eventProperties.put("CE Screen Name", AppController.currentScreen);

        Event event = new Event("CE App Launched", eventProperties);
        sendEvent(event);
    }

    /**
     * Send sdk events asynchronously
     *
     * @param event event name and properties
     */
    private void sendEvent(Event event) {
        onSDKStateDecided.subscribe((Object ignored) -> {
            apiService.sendEvent(event).enqueue(new Callback<Campaign>() {
                @Override
                public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, " Event Sent Response Code : " + response.code());
                }

                @Override
                public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
                    // TODO Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "Event Sent Error Message : " + t.toString());
                }
            });
        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
        });
    }

    /**
     * Sends default user properties to the server
     *
     * @param userProps additional user properties
     */
    private void sendUserProperties(Map<String, String> userProps) {
        onSDKStateDecided.subscribe((Object ignored) -> {
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

            apiService.updateProfile(userMap).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "User Properties Response Code : " + response.code());
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    // TODO Saving the request locally so that it can be sent later
                    Log.e(CooeeSDKConstants.LOG_PREFIX, "User Properties Error Message : " + t.toString());
                }
            });
        }, (Throwable error) -> {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Observable Error : " + error.toString());
        });
    }

    /**
     * Create new session on every launch
     */
    void createSession() {
        currentSessionStartTime = new Date().toString();
        currentSessionId = createSessionId();
        currentSessionNumber = getSessionNumber();
    }

    /**
     * Create session id
     *
     * @return session id
     */
    private String createSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Create or get next session number from shared preference
     *
     * @return next session number
     */
    private String getSessionNumber() {
        String sessionNumber = LocalStorageHelper.getString(context, "session_number", "");

        if (sessionNumber.isEmpty()) {
            sessionNumber = "1";
        } else {
            int number = Integer.parseInt(sessionNumber);
            number += 1;
            sessionNumber = String.valueOf(number);
        }

        LocalStorageHelper.putString(context, "session_number", sessionNumber);

        return sessionNumber;
    }
}
