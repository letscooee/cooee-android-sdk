package com.letscooee.android.retrofit;

import com.letscooee.android.models.Campaign;
import com.letscooee.android.models.FirstOpen;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;


public interface ServerAPIService {
    @GET("first_open/")
    Call<FirstOpen> firstOpen();

    @GET("image_open/")
    Call<Campaign> imageOpen(@Header ("sdkToken") String sdkToken,@QueryMap Map<String,Object> objectMap);

//    @GET("/users/{username}/repos")
//    void searchRepositoriesByUser(@Path("username") String githubUser);
}
