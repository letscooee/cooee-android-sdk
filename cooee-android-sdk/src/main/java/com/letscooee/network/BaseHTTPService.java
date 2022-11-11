package com.letscooee.network;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.letscooee.ContextAware;
import com.letscooee.CooeeFactory;
import com.letscooee.exceptions.HttpRequestFailedException;
import com.letscooee.models.DeviceAuthResponse;
import com.letscooee.models.Event;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.APIService;
import com.letscooee.retrofit.external.ExternalApiClient;
import com.letscooee.retrofit.external.ExternalApiService;
import com.letscooee.retrofit.internal.PublicApiClient;
import com.letscooee.retrofit.internal.PublicApiService;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.utils.Constants;
import java.io.IOException;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A base or lower level HTTP service which simply hits the backend for given request. It does not perform
 * any retries or it does not cache the request for future reattempts. This server should not contain any business logic.
 * <p>
 * Make sure these methods are not called in the main-thread.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class BaseHTTPService extends ContextAware {

    private final APIService apiService = APIClient.getAPIService();
    private final ExternalApiService externalApiService = ExternalApiClient.getAPIService();
    private final PublicApiService publicApiService = PublicApiClient.getAPIService();
    private final Gson gson = new Gson();

    public BaseHTTPService(Context context) {
        super(context);
    }

    public Map<String, Object> sendEvent(Event event) throws HttpRequestFailedException {
        Call<Map<String, Object>> call = apiService.sendEvent(event);
        Response<?> response = this.executeHTTPCall(call, "Send " + event);

        this.updateUserAuthDetails(response);

        //noinspection unchecked
        Map<String, Object> responseData = (Map<String, Object>) response.body();

        new EngagementTriggerHelper(context).renderInAppTriggerFromResponse(responseData);

        return responseData;
    }

    public Map<String, Object> getLazyData(String triggerId) throws HttpRequestFailedException {
        Call<Map<String, Object>> call = apiService.loadTriggerDetails(triggerId);
        Response<?> response = this.executeHTTPCall(call, "Get trigger In-App data");

        //noinspection unchecked
        return (Map<String, Object>) response.body();
    }

    @SuppressWarnings("UnusedReturnValue")
    public DeviceAuthResponse updateUserProfile(Map<String, Object> data) throws HttpRequestFailedException {
        Call<DeviceAuthResponse> call = apiService.updateProfile(data);
        Response<?> response = this.executeHTTPCall(call, "Update user profile");

        return this.updateUserAuthDetails(response);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Map<String, Object> updateDeviceProperty(Map<String, Object> data) throws HttpRequestFailedException {
        Call<Map<String, Object>> call = apiService.updateDeviceProperty(data);
        Response<?> response = this.executeHTTPCall(call, "Update device property");

        //noinspection unchecked
        return (Map<String, Object>) response.body();
    }

    public void updatePushToken(Map<String, Object> data) throws HttpRequestFailedException {
        Call<ResponseBody> call = apiService.setPushToken(data);
        Response<?> response = this.executeHTTPCall(call, "Update user profile");

        /*
         * Close the response body if it is not null.
         * Ref: https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/#the-response-body-can-be-consumed-only-once
         */
        assert response.body() != null;
        ((ResponseBody) response.body()).close();
    }

    public void sendSessionConcludedEvent(Map<String, Object> data) throws HttpRequestFailedException {
        Call<ResponseBody> call = apiService.concludeSession(data);
        Response<?> response = this.executeHTTPCall(call, "Conclude Session");

        /*
         * Close the response body if it is not null.
         * Ref: https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response-body/#the-response-body-can-be-consumed-only-once
         */
        assert response.body() != null;
        ((ResponseBody) response.body()).close();
    }

    public void keepAliveSession(Map<String, Object> data) {
        apiService.keepAlive(data).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.i(Constants.TAG, "Session Alive Response Code: " + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(Constants.TAG, "Session Alive Response Error Message" + t);
            }
        });
    }

    public Map<String, Object> getAppConfig(String appId) throws HttpRequestFailedException {
        Call<Map<String, Object>> call = publicApiService.getAppConfig(appId);
        Response<?> response = this.executeHTTPCall(call, "Font Request");
        //noinspection unchecked
        return (Map<String, Object>) response.body();
    }

    public ResponseBody downloadFont(String url) throws HttpRequestFailedException {
        Call<ResponseBody> call = externalApiService.downloadFile(url);
        Response<?> response = this.executeHTTPCall(call, "Font Download");
        return (ResponseBody) response.body();
    }

    public Map<String, Object> uploadScreenshot(MultipartBody.Part body, RequestBody parameter)
            throws HttpRequestFailedException {
        Call<Map<String, Object>> call = apiService.uploadScreenshot(body, parameter);
        Response<?> response = this.executeHTTPCall(call, "Upload Request");
        //noinspection unchecked
        return (Map<String, Object>) response.body();
    }

    @SuppressWarnings("UnusedReturnValue")
    public DeviceAuthResponse logOutUser() throws HttpRequestFailedException {
        Call<DeviceAuthResponse> call = apiService.logOutUser();
        Response<?> response = this.executeHTTPCall(call, "Logout User");

        return this.updateUserAuthDetails(response);
    }

    /**
     * Deserialize response body to the {@link DeviceAuthResponse} to let update device auth details
     * like userID, sdkToken and deviceID
     *
     * @param response {@link Response} received via retrofit which may contain {@link DeviceAuthResponse}
     * @return {@link DeviceAuthResponse} retrieved from {@code response}
     */
    private DeviceAuthResponse updateUserAuthDetails(Response<?> response) {
        DeviceAuthResponse deviceAuthResponse = gson.fromJson(gson.toJson(response.body()), DeviceAuthResponse.class);
        CooeeFactory.getDeviceAuthService().checkAndUpdate(deviceAuthResponse);
        return deviceAuthResponse;
    }

    private Response<?> executeHTTPCall(Call<?> call, String message) throws HttpRequestFailedException {
        try {
            Response<?> response = call.execute();

            if (response.isSuccessful()) {
                return response;
            }

            Log.d(Constants.TAG, "Server failure for " + message + ", resp code: " + response.code()
                    + ", resp: " + response.body());

            throw new HttpRequestFailedException("Error on " + message, response.code(), response.body());

        } catch (IOException e) {
            Log.e(Constants.TAG, "Exception in HTTP: " + message, e);
            throw new HttpRequestFailedException("Exception in HTTP: " + message, e);
        }
    }

}
