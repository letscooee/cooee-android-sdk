package com.letscooee.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.letscooee.models.Campaign;
import com.letscooee.models.Event;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import java.io.IOException;

import retrofit2.Response;

/**
 * @author Abhishek Taparia
 * SendEventAsyncNetworkClass help create a async network request to send event to server
 */
public class SendEventAsyncNetworkClass extends AsyncTask<Event, Void, Campaign> {
    private Context context;

    public SendEventAsyncNetworkClass(Context context) {
        this.context = context;
    }

    @Override
    protected final Campaign doInBackground(Event... events) {
        ServerAPIService apiService = APIClient.getServerAPIService();
        Response<Campaign> response = null;
        Log.d("check", "out");
        try {
            String header = context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE).getString(CooeeSDKConstants.SDK_TOKEN, "");
            response = apiService.sendEvent(header, events[0]).execute();
            Log.d("ResponseCode", response.code() + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.body();
    }
}
