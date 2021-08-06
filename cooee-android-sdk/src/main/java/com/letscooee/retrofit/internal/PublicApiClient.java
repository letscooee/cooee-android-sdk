package com.letscooee.retrofit.internal;

import com.letscooee.BuildConfig;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PublicApiClient {

    private static Retrofit retrofit = null;

    private static synchronized Retrofit getClient() {
        if (retrofit != null) {
            return retrofit;
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    public static PublicApiService getAPIService() {
        Retrofit retrofit = getClient();
        return retrofit.create(PublicApiService.class);
    }
}
