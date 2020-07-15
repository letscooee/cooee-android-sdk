package com.wizpanda.cooee.retrofit;

import com.wizpanda.cooee.models.Campaign;
import com.wizpanda.cooee.models.SDKAuthentication;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * The ServerAPIService interface helps APIClient class in sending requests
 */

public interface ServerAPIService {
    @GET("first_open/")
    Call<SDKAuthentication> firstOpen();

    @GET("image_open/")
    Call<Campaign> imageOpen(@Header("sdkToken") String sdkToken, @Query("name") String name, @QueryMap Map<String, String> objectMap);

}
