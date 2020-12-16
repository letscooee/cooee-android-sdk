package com.letscooee.retrofit;

import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.Campaign;
import com.letscooee.models.Event;
import com.letscooee.models.SDKAuthentication;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
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

    @POST("/v1/event/save")
    Call<Campaign> sendEvent(@Body Event event);

    @PUT("/v1/user/update")
    Call<ResponseBody> updateProfile(@Body Map<String, Object> objectMap);
}
