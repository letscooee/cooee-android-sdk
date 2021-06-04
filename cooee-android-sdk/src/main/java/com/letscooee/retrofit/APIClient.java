package com.letscooee.retrofit;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.RestrictTo;
import com.letscooee.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

import static com.letscooee.utils.Constants.LOG_PREFIX;

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
    private static String deviceName = "";
    private static String userId = "";

    public static APIService getAPIService() {
        Retrofit retrofit = getClient();
        return retrofit.create(APIService.class);
    }

    private static Retrofit getClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request.Builder requestBuilder = chain.request()
                            .newBuilder()
                            .addHeader("Content-Type", "application/json");

                    boolean isPublicAPI = chain.request().url().toString().endsWith("v1/user/save");

                    if (!isPublicAPI && apiToken != null) {
                        requestBuilder.addHeader("x-sdk-token", apiToken);
                    }

                    if (BuildConfig.DEBUG) {
                        requestBuilder.addHeader("sdk-debug", "1");
                    }

                    requestBuilder.addHeader("device-name", deviceName);
                    requestBuilder.addHeader("user-id", userId);

                    Log.d(LOG_PREFIX, "Request: " + requestBuilder.build().toString());
                    return chain.proceed(requestBuilder.build());
                })
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }

    public static void setAPIToken(String token) {
        apiToken = TextUtils.isEmpty(token) ? "" : token;
    }

    public static void setDeviceName(String name) {
        deviceName = name;
    }

    public static void setUserId(String id) {
        userId = TextUtils.isEmpty(id) ? "" : id;
    }
}
