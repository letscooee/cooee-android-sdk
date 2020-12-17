package com.letscooee.retrofit;

import android.util.Log;

import com.letscooee.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.letscooee.utils.CooeeSDKConstants.LOG_PREFIX;

/**
 * The APIClient class will help in sending request to server
 *
 * @author Abhishek Taparia
 */
public class APIClient {

    private static final String BASE_URL = BuildConfig.SERVER_URL;
    private static Retrofit retrofit = null;

    private static String apiToken;

    public static ServerAPIService getServerAPIService() {
        Retrofit retrofit = getClient(BASE_URL);
        return retrofit.create(ServerAPIService.class);
    }

    public static Retrofit getClient(String baseUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request.Builder requestBuilder = chain.request()
                            .newBuilder()
                            .addHeader("Content-Type", "application/json");

                    boolean isPublicAPI = chain.request().url().toString().endsWith("v1/user/save");

                    if (!isPublicAPI) {
                        requestBuilder.addHeader("x-sdk-token", apiToken);
                    }

                    Log.d(LOG_PREFIX, "Request : " + requestBuilder.build().toString());
                    return chain.proceed(requestBuilder.build());
                })
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }

    public static void setAPIToken(String token) {
        apiToken = token;
    }
}
