package com.letscooee.async;

import android.os.AsyncTask;
import android.util.Log;

import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.SDKAuthentication;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import java.io.IOException;

import retrofit2.Response;

/**
 * This is used to create separate thread for synchronous http calls
 * @author Abhishek Taparia
 */
public class AuthSyncNetwork extends AsyncTask<AuthenticationRequestBody, Void, Response<SDKAuthentication>> {

    @Override
    protected Response<SDKAuthentication> doInBackground(AuthenticationRequestBody... authenticationRequestBodies) {
        ServerAPIService apiService = APIClient.getServerAPIService();

        Response<SDKAuthentication> sdkAuthentication = null;
        try {
            sdkAuthentication = apiService.registerUser(authenticationRequestBodies[0]).execute();
        } catch (IOException e) {
            Log.e(CooeeSDKConstants.LOG_PREFIX, "Auth Token Error Message : " + e.toString());
        }

        return sdkAuthentication;
    }
}
