package com.letscooee.async;

import android.os.AsyncTask;
import android.util.Log;

import com.letscooee.models.AuthenticationRequestBody;
import com.letscooee.models.SDKAuthentication;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;

import java.io.IOException;

import retrofit2.Response;

/**
 * @author Abhishek Taparia
 */
public class AuthSyncNetworkClass extends AsyncTask<AuthenticationRequestBody, Void, Response<SDKAuthentication>> {

    @Override
    protected Response<SDKAuthentication> doInBackground(AuthenticationRequestBody... authenticationRequestBodies) {
        ServerAPIService apiService = APIClient.getServerAPIService();
        Response<SDKAuthentication> sdkAuthentication = null;
        try {
            sdkAuthentication = apiService.firstOpen(authenticationRequestBodies[0]).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sdkAuthentication;
    }
}
