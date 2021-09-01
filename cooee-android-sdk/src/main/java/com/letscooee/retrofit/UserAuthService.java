package com.letscooee.retrofit;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.DeviceData;
import com.letscooee.models.UserAuthResponse;
import com.letscooee.schedular.CooeeJobUtils;
import com.letscooee.utils.Constants;
import com.letscooee.utils.LocalStorageHelper;
import com.letscooee.utils.ManifestReader;
import com.letscooee.utils.SentryHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class to register user with server and to provide related data
 *
 * @author Ashish Gaikwad on 22/05/21
 * @version 0.2.10
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class UserAuthService {

    private final Context context;
    private final SentryHelper sentryHelper;
    private final APIService apiService;

    private String sdkToken;
    private String userID;
    private String deviceID;

    public UserAuthService(Context context, SentryHelper sentryHelper) {
        this.context = context.getApplicationContext();
        this.apiService = APIClient.getAPIService();
        this.sentryHelper = sentryHelper;
    }

    public boolean hasToken() {
        String sdkToken = LocalStorageHelper.getString(context, Constants.STORAGE_SDK_TOKEN, null);
        return !TextUtils.isEmpty(sdkToken);
    }

    public String getUserID() {
        return this.userID;
    }

    /**
     * This method will pull user data (like SDK token & user ID) from the local storage (shared preference)
     * and populates it for further use.
     */
    public void populateUserDataFromStorage() {
        sdkToken = LocalStorageHelper.getString(context, Constants.STORAGE_SDK_TOKEN, null);
        if (TextUtils.isEmpty(sdkToken)) {
            Log.d(Constants.TAG, "No SDK token found in preference");
        }

        userID = LocalStorageHelper.getString(context, Constants.STORAGE_USER_ID, null);
        if (TextUtils.isEmpty(userID)) {
            Log.d(Constants.TAG, "No user ID found in preference");
        }

        this.updateAPIClient();
    }

    /**
     * Method will ensure that the SDK has acquired the token. If on the first time, token can't be pulled
     * from the server, calling this method will reattempt the same maximum within 1 minute.
     */
    public synchronized void acquireSDKToken() {
        if (this.hasToken()) {
            return;
        }

        Log.d(Constants.TAG, "Attempt to acquire SDK token");
        long lastCheckTime = LocalStorageHelper.getLong(context, Constants.STORAGE_LAST_TOKEN_ATTEMPT, 0);

        // We are attempting first time
        if (lastCheckTime == 0) {
            this.getSDKTokenFromServer();
        } else {
            Calendar calender = Calendar.getInstance();
            calender.setTimeInMillis(lastCheckTime);
            calender.add(Calendar.MINUTE, 1);

            // If the last attempt was
            if (new Date().after(calender.getTime())) {
                this.getSDKTokenFromServer();
            }
        }
    }

    /**
     * Make user registration with server (if not already) and acquire a SDK token which will be later used to authenticate
     * other endpoints.
     */
    private void getSDKTokenFromServer() {
        AuthenticationRequestBody requestBody = getAuthenticationRequestBody();
        apiService.registerUser(requestBody).enqueue(new Callback<UserAuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserAuthResponse> call, @NonNull Response<UserAuthResponse> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    UserAuthService.this.saveUserDataInStorage(response.body());

                    // Start the job immediately to make sure the pending tasks can be sent
                    CooeeJobUtils.triggerPendingTaskJobImmediately(UserAuthService.this.context);
                } else {
                    UserAuthService.this.sentryHelper.captureMessage("Unable to acquire token- " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserAuthResponse> call, @NonNull Throwable t) {
                Log.e(Constants.TAG, "Unable to acquire token", t);
            }
        });

        LocalStorageHelper.putLong(context, Constants.STORAGE_LAST_TOKEN_ATTEMPT, new Date().getTime());
    }

    private void saveUserDataInStorage(UserAuthResponse userAuthResponse) {
        this.sdkToken = userAuthResponse.getSdkToken();
        this.userID = userAuthResponse.getId();
        this.deviceID= userAuthResponse.getDeviceID();
        this.updateAPIClient();

        LocalStorageHelper.putString(context, Constants.STORAGE_SDK_TOKEN, sdkToken);
        LocalStorageHelper.putString(context, Constants.STORAGE_USER_ID, userID);
        LocalStorageHelper.putString(context, Constants.STORAGE_DEVICE_ID, deviceID);
    }

    private void updateAPIClient() {
        if (BuildConfig.DEBUG) {
            Log.i(Constants.TAG, "SDK Token: " + sdkToken);
            Log.i(Constants.TAG, "User ID: " + userID);
        }

        APIClient.setAPIToken(sdkToken);
        APIClient.setUserId(userID);
        this.sentryHelper.setUserId(userID);
    }

    /**
     * returns  AuthenticationRequestBody to be used in observer
     *
     * @return AuthenticationRequestBody
     */
    private AuthenticationRequestBody getAuthenticationRequestBody() {
        ManifestReader manifestReader = ManifestReader.getInstance(context);

        return new AuthenticationRequestBody(
                manifestReader.getAppID(),
                manifestReader.getAppSecret(),
                new DeviceData("ANDROID",
                        BuildConfig.VERSION_NAME,
                        CooeeFactory.getAppInfo().getVersion(),
                        Build.VERSION.RELEASE));
    }
}
