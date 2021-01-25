package com.letscooee.retrofit;

import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.Event;
import com.letscooee.models.SDKAuthentication;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.PUT;

import java.util.Map;

/**
 * The ServerAPIService interface helps APIClient class in sending requests
 *
 * @author Abhishek Taparia
 */
public interface ServerAPIService {

    @POST("/v1/user/save")
    Call<SDKAuthentication> registerUser(@Body AuthenticationRequestBody authenticationRequestBody);

    @POST("/v1/event/track")
    Call<Map<String, Object>> sendEvent(@Body Event event);

    @FormUrlEncoded
    @POST("/v1/session/conclude")
    Call<ResponseBody> concludeSession(@Field("sessionID") String sessionID, @Field("duration") int duration);

    @PUT("/v1/user/update")
    Call<Map<String, Object>> updateProfile(@Body Map<String, Object> objectMap);

    @FormUrlEncoded
    @POST("/v1/session/keepAlive")
    Call<ResponseBody> keepAlive(@Field("sessionID") String sessionID);

    @FormUrlEncoded
    @POST("/v1/user/setFirebaseToken")
    Call<ResponseBody> setFirebaseToken(@Field("sessionID") String sessionID, @Field("firebaseToken") String firebaseToken);
}
