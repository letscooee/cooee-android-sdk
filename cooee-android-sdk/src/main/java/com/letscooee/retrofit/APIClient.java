package com.letscooee.retrofit;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.letscooee.BuildConfig;
import com.letscooee.CooeeFactory;
import com.letscooee.utils.GsonDateAdapter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.letscooee.utils.Constants.TAG;

/**
 * The APIClient class will help in sending request to server
 *
 * @author Abhishek Taparia
 * @version 0.0.1
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class APIClient {

    private static final String BASE_URL = BuildConfig.SERVER_URL;
    private static Retrofit retrofit = null;

    private static String apiToken;
    private static String userId = "";
    private static String appVersion = "";
    private static String wrapperName = "";
    private static Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonDateAdapter()).create();

    public static APIService getAPIService() {
        Retrofit retrofit = getClient();
        return retrofit.create(APIService.class);
    }

    private static Retrofit getClient() {
        String deviceName = CooeeFactory.getDeviceInfo().getDeviceName();
        boolean isAppDebuggable = CooeeFactory.getAppInfo().isDebuggable();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request.Builder requestBuilder = chain.request()
                            .newBuilder()
                            .addHeader("Content-Type", "application/json");

                    boolean isPublicAPI = chain.request().url().toString().endsWith("/v1/device/validate");

                    if (!isPublicAPI && apiToken != null) {
                        requestBuilder.addHeader("x-sdk-token", apiToken);
                    }

                    if (!isPublicAPI && userId != null){
                        requestBuilder.addHeader("user-id", userId);
                    }

                    if (BuildConfig.DEBUG) {
                        requestBuilder.addHeader("sdk-debug", "1");
                    }

                    if (isAppDebuggable) {
                        requestBuilder.addHeader("app-debug", "1");
                    }

                    requestBuilder.addHeader("device-name", URLEncoder.encode(deviceName, "UTF-8"));
                    requestBuilder.addHeader("sdk-version", BuildConfig.VERSION_NAME);
                    requestBuilder.addHeader("sdk-version-code", String.valueOf(BuildConfig.VERSION_CODE));
                    requestBuilder.addHeader("app-version", appVersion);

                    if (!TextUtils.isEmpty(wrapperName)) {
                        requestBuilder.addHeader("sdk-wrapper", wrapperName);
                    }

                    Log.d(TAG, "Request: " + requestBuilder.build().toString());
                    return chain.proceed(requestBuilder.build());
                })
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }

    public static void setAPIToken(String token) {
        apiToken = TextUtils.isEmpty(token) ? "" : token;
    }

    public static void setUserId(String id) {
        userId = TextUtils.isEmpty(id) ? "" : id;
    }

    public static void setAppVersion(String version) {
        appVersion = TextUtils.isEmpty(version) ? "" : version;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void setWrapperName(String name){
        wrapperName = name;
    }
}
