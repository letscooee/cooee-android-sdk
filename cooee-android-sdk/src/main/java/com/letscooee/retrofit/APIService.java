package com.letscooee.retrofit;

import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.Event;
import com.letscooee.models.DeviceAuthResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

import java.util.Map;

/**
 * The ServerAPIService interface helps APIClient class in sending requests
 *
 * @author Abhishek Taparia
 * @version 0.0.1
 */
public interface APIService {

    @POST("/v1/device/validate")
    Call<DeviceAuthResponse> registerDevice(@Body AuthenticationRequestBody authenticationRequestBody);

    @POST("/v1/event/track")
    Call<Map<String, Object>> sendEvent(@Body Event event);

    @POST("/v1/session/conclude")
    Call<ResponseBody> concludeSession(@Body Map<String, Object> sessionConcludeRequest);

    @PUT("/v1/user/update")
    Call<DeviceAuthResponse> updateProfile(@Body Map<String, Object> objectMap);

    @PUT("/v1/device/update")
    Call<Map<String, Object>> updateDeviceProperty(@Body Map<String, Object> objectMap);

    @POST("/v1/session/keepAlive")
    Call<ResponseBody> keepAlive(@Body Map<String, Object> keepAliveRequest);

    @POST("/v1/device/setPushToken")
    Call<ResponseBody> setPushToken(@Body Map<String, Object> tokenRequest);

    @GET("/v1/trigger/details/{triggerID}")
    Call<Map<String, Object>> loadTriggerDetails(@Path("triggerID") String triggerID);

    @Multipart
    @POST("/v1/app/uploadScreenshot")
    Call<Map<String, Object>> uploadScreenshot(@Part MultipartBody.Part body, @Part("screenName") RequestBody parameter);

    @GET("/v1/user/logout")
    Call<DeviceAuthResponse> logOutUser();
}
