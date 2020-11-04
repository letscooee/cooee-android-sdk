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
import com.letscooee.models.*;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.letscooee.utils.CooeeSDKConstants.LOG_PREFIX;

/**
 * PostLaunchActivity initialized when app is launched
 *
 * @author Abhishek Taparia
 */
public class PostLaunchActivity {

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;
    private Context context;
    private DefaultUserPropertiesCollector defaultUserPropertiesCollector;
    private ServerAPIService apiService;

    /**
     * Public Constructor
     *
     * @param context application context
     */
    public PostLaunchActivity(Context context) {
        if (context != null) {
            this.context = context;
            this.mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, Context.MODE_PRIVATE);
            this.defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
            this.apiService = APIClient.getServerAPIService();
        }
    }

    /**
     * Runs every time app is launched
     */
    public void appLaunch() {
        if (this.context == null) {
            return;
        }
        if (isAppFirstTimeLaunch()) {
            String[] appCredentials = getAppCredentials();
            AuthenticationRequestBody authenticationRequestBody = new AuthenticationRequestBody(
                    appCredentials[0],
                    appCredentials[1],
                    new DeviceData("ANDROID",
                            BuildConfig.VERSION_NAME + "",
                            defaultUserPropertiesCollector.getAppVersion(),
                            Build.VERSION.RELEASE));

            this.apiService.firstOpen(authenticationRequestBody).enqueue(new retrofit2.Callback<SDKAuthentication>() {
                @Override
                public void onResponse(@NonNull Call<SDKAuthentication> call, @NonNull Response<SDKAuthentication> response) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String sdkToken = response.body().getSdkToken();
                        Log.i(LOG_PREFIX + " bodyResponse", sdkToken);
                        appFirstOpen(sdkToken);

                    } else {
                        Log.e(LOG_PREFIX + " bodyError", String.valueOf(response.errorBody()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SDKAuthentication> call, @NonNull Throwable t) {
                    Log.e(LOG_PREFIX + " bodyError", t.toString());
                }
            });
        } else {
            this.mSharedPreferences = this.context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
            String sdk = this.mSharedPreferences.getString(CooeeSDKConstants.SDK_TOKEN, "");
            Log.i(LOG_PREFIX + " SDK return", sdk);

            successiveAppLaunch(sdk);
        }
    }

    /**
     * Check if app is launched for first time
     *
     * @return true if app is launched for first time, else false
     */
    private boolean isAppFirstTimeLaunch() {
        if (this.mSharedPreferences != null && this.mSharedPreferences.getBoolean(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, true)) {
            // App is open/launch for first time, update the preference
            this.mSharedPreferencesEditor = this.mSharedPreferences.edit();
            this.mSharedPreferencesEditor.putBoolean(CooeeSDKConstants.IS_APP_FIRST_TIME_LAUNCH, false);
            this.mSharedPreferencesEditor.apply();
            return true;
        } else {
            // App previously opened
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
     * Runs when app is opened for the first time after sdkToken is received from server asynchronously
     *
     * @param sdkToken sdkToken from server
     */
    private void appFirstOpen(String sdkToken) {
        mSharedPreferences = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE);
        mSharedPreferencesEditor = mSharedPreferences.edit();
        mSharedPreferencesEditor.putString(CooeeSDKConstants.SDK_TOKEN, sdkToken);
        mSharedPreferencesEditor.apply();

        Map<String, String> userProperties = new HashMap<>();
        userProperties.put("CE First Launch Time", new Date().toString());
        sendUserProperties(sdkToken, userProperties);

        Map<String, String> eventProperties = new HashMap<>();
        eventProperties.put("CE Source", "SYSTEM");
        eventProperties.put("CE App Version", defaultUserPropertiesCollector.getAppVersion());
        Event event = new Event("CE App Installed", eventProperties);

        sendEvent(sdkToken, event);
    }

    /**
     * Runs every time when app is opened for a new session
     *
     * @param sdkToken sdkToken stored in shared preferences
     */
    private void successiveAppLaunch(String sdkToken) {
        sendUserProperties(sdkToken, null);

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
        sendEvent(sdkToken, event);
    }

    /**
     * Send sdk events asynchronously
     *
     * @param sdkToken sdkToken from server/shared preferences
     * @param event    event name and properties
     */
    private void sendEvent(String sdkToken, Event event) {
        apiService.sendEvent(sdkToken, event).enqueue(new Callback<Campaign>() {
            @Override
            public void onResponse(@NonNull Call<Campaign> call, @NonNull Response<Campaign> response) {
                Log.i(LOG_PREFIX + " Event Sent", response.code() + "");
            }

            @Override
            public void onFailure(@NonNull Call<Campaign> call, @NonNull Throwable t) {
                Log.e(LOG_PREFIX + " bodyError", t.toString());
            }
        });
    }

    /**
     * Sends default user properties to the server
     *
     * @param sdkToken  unique token received from server
     * @param userProps additional user properties
     */
    private void sendUserProperties(String sdkToken, Map<String, String> userProps) {
        apiService = APIClient.getServerAPIService();
        defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
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
        userProperties.put("CE Installed Time", defaultUserPropertiesCollector.getInstalledTime());
        userProperties.put("CE Last Launch Time", new Date().toString());
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userProperties", userProperties);

        apiService.updateProfile(sdkToken, userMap).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.i(LOG_PREFIX + " userProperties", response.code() + "");
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(LOG_PREFIX + " bodyError", t.toString());
            }
        });
    }
}
