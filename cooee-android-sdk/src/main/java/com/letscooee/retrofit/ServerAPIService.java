package com.letscooee.retrofit;

import com.letscooee.models.Campaign;
import com.letscooee.models.SDKAuthentication;
import com.letscooee.models.UserProfile;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * @author Abhishek Taparia
 * The ServerAPIService interface helps APIClient class in sending requests
 */
public interface ServerAPIService {

    @GET("first_open/")
    Call<SDKAuthentication> firstOpen();

    @GET("image_open/")
    Call<Campaign> sendEvent(@Header("sdkToken") String sdkToken, @QueryMap Map<String, Object> objectMap);

    @POST("update_profile/")
    @FormUrlEncoded
    Call<UserProfile> updateProfile(@Header("sdkToken") String sdkToken,@FieldMap Map<String,Object> objectMap);

}
