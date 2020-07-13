package com.letscooee.android.retrofit;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;


public interface ServerAPIService {
    @GET("first_open/")
    Call<String> firstOpen();

//    @GET("/users/{username}/repos")
//    void searchRepositoriesByUser(@Path("username") String githubUser);
}
