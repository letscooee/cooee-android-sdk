package com.letscooee.retrofit.internal;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PublicApiService {
    @GET("/v1/app/config/{appId}")
    Call<Map<String, Object>> getAppConfig(@Path("appId") String appId);
}
