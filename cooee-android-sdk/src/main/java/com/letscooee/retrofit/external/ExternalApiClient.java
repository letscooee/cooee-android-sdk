package com.letscooee.retrofit.external;

import com.letscooee.BuildConfig;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provide retrofit client to access any external API other than Cooee's APIs.
 *
 * @author Ashish Gaikwad
 * @since 1.0.0
 */
public class ExternalApiClient {

    private static Retrofit retrofit;

    private static synchronized Retrofit getClient() {
        if (retrofit != null) {
            return retrofit;
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit;
    }

    public static ExternalApiService getAPIService() {
        Retrofit retrofit = getClient();
        return retrofit.create(ExternalApiService.class);
    }
}
