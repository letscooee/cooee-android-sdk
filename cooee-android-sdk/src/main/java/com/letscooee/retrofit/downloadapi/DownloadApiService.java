package com.letscooee.retrofit.downloadapi;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface DownloadApiService {
    @GET("/v1/app/config/{appId}")
    Call<Map<String, Object>> getAppConfig(@Path("appId") String appId);

    @Streaming
    @GET
    Call<ResponseBody> downloadFile(@Url String url);
}
