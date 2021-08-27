package com.letscooee.retrofit;

import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.Event;
import com.letscooee.models.UserAuthResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.Map;

/**
 * The ServerAPIService interface helps APIClient class in sending requests
 *
 * @author Abhishek Taparia
 * @version 0.0.1
 */
public interface APIService {

    @POST("/v1/user/save")
    Call<UserAuthResponse> registerUser(@Body AuthenticationRequestBody authenticationRequestBody);

    @POST("/v1/event/track")
    Call<Map<String, Object>> sendEvent(@Body Event event);

    @POST("/v1/session/conclude")
    Call<ResponseBody> concludeSession(@Body Map<String, Object> sessionConcludeRequest);

    @PUT("/v1/user/update")
    Call<Map<String, Object>> updateProfile(@Body Map<String, Object> objectMap);

    @POST("/v1/session/keepAlive")
    Call<ResponseBody> keepAlive(@Body Map<String, Object> keepAliveRequest);

    @POST("/v1/user/setFirebaseToken")
    Call<ResponseBody> setFirebaseToken(@Body Map<String, Object> tokenRequest);

    @GET("/v1/trigger/details/{triggerID}")
    Call<Map<String, Object>> loadTriggerDetails(@Path("triggerID") String triggerID);
}
