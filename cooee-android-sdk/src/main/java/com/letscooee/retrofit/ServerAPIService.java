package com.letscooee.retrofit;

import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.Campaign;
import com.letscooee.models.Event;
import com.letscooee.models.SDKAuthentication;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * @author Abhishek Taparia
 * The ServerAPIService interface helps APIClient class in sending requests
 */
public interface ServerAPIService {

    @POST("v1/user/save/")
    Call<SDKAuthentication> firstOpen(@Body AuthenticationRequestBody authenticationRequestBody);

    @POST("v1/event/save/")
    Call<Campaign> sendEvent(@Header("x-sdk-token") String sdkToken, @Body Event event);

    @PUT("v1/user/update/")
    Call<ResponseBody> updateProfile(@Header("x-sdk-token") String sdkToken, @Body Map<String, Object> objectMap);
}
