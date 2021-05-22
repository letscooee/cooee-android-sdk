package com.letscooee.retrofit;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RestrictTo;

import com.letscooee.BuildConfig;
import com.letscooee.init.DefaultUserPropertiesCollector;
import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.DeviceData;
import com.letscooee.models.SDKAuthentication;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.LocalStorageHelper;

import java.util.Date;

import io.sentry.Sentry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility class to register user with server and to provide related data
 *
 * @author Ashish Gaikwad on 22/05/21
 * @version 0.2.10
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class RegisterUser {
    private static RegisterUser registerUser;
    private Context context;
    private ServerAPIService apiService;
    private DefaultUserPropertiesCollector defaultUserPropertiesCollector;

    private RegisterUser(Context context) {
        this.context = context;
        defaultUserPropertiesCollector = new DefaultUserPropertiesCollector(context);
        this.apiService = APIClient.getServerAPIService();
    }

    public boolean hasToken() {
        String sdkToken = LocalStorageHelper.getString(context, CooeeSDKConstants.STORAGE_SDK_TOKEN, null);
        return TextUtils.isEmpty(sdkToken);
    }

    /**
     * Initializes the singleton instance for RegisterUser
     */
    public static RegisterUser getInstance(Context context) {
        if (registerUser == null) {
            synchronized (RegisterUser.class) {
                if (registerUser == null) {
                    registerUser = new RegisterUser(context);
                }
            }
        }
        return registerUser;
    }

    /**
     * Make user registration with server
     */
    public void registerUser() {
        LocalStorageHelper.putLong(context, CooeeSDKConstants.FIRST_LAUNCH_CALL_TIME, new Date().getTime());
        AuthenticationRequestBody authenticationRequestBody = getAuthenticationRequestBody();
        apiService.registerUser(authenticationRequestBody).enqueue(new Callback<SDKAuthentication>() {
            @Override
            public void onResponse(Call<SDKAuthentication> call, Response<SDKAuthentication> response) {

                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String sdkToken = response.body().getSdkToken();
                    Log.i(CooeeSDKConstants.LOG_PREFIX, "Token : " + sdkToken);
                    LocalStorageHelper.putString(context, CooeeSDKConstants.STORAGE_SDK_TOKEN, sdkToken);
                    LocalStorageHelper.putString(context, CooeeSDKConstants.STORAGE_USER_ID, response.body().getId());

                    APIClient.setAPIToken(sdkToken);
                }


            }

            @Override
            public void onFailure(Call<SDKAuthentication> call, Throwable t) {
                Log.d(CooeeSDKConstants.LOG_PREFIX, "onFailure: Fail to register user");
            }
        });
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
     * Get app credentials if passed as metadata from host application's manifest file
     *
     * @return String[]{appId,appSecret}
     */
    private String[] getAppCredentials() {
        ApplicationInfo app;

        try {
            app = this.context.getPackageManager().getApplicationInfo(this.context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            Sentry.captureException(e);
            return new String[]{null, null};
        }

        Bundle bundle = app.metaData;
        String appId = bundle.getString("COOEE_APP_ID");
        String appSecret = bundle.getString("COOEE_APP_SECRET");
        return new String[]{appId, appSecret};
    }

}
