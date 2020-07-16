package com.wizpanda.cooee.retrofit;

import android.util.Log;

import com.wizpanda.cooee.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The APIClient class will help in sending request to server
 */
public class APIClient {

    private static final String BASE_URL = BuildConfig.SERVER_URL;
    private static Retrofit retrofit = null;

    public static ServerAPIService getServerAPIService() {
        Retrofit retrofit = getClient(BASE_URL);
        return retrofit.create(ServerAPIService.class);
    }

    public static Retrofit getClient(String baseUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Request request = chain.request();
//                        Log.i("request", request.toString());
//                        return chain.proceed(request);
//                    }
//                })
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
}
