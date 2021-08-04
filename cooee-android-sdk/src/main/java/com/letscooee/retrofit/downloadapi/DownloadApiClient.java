package com.letscooee.retrofit.downloadapi;

import com.letscooee.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownloadApiClient {
    private static final String BASE_URL = BuildConfig.SERVER_URL;
    private static Retrofit retrofit = null;

    private static Retrofit getClient() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static DownloadApiService getAPIService() {
        Retrofit retrofit = getClient();
        return retrofit.create(DownloadApiService.class);
    }
}
