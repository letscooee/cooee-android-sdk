package com.letscooee.retrofit.internal;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.Map;

public interface PublicApiService {

    @GET("/v1/app/config/{appId}")
    Call<Map<String, Object>> getAppConfig(@Path("appId") String appId);
}
