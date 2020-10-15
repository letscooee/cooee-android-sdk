package com.letscooee.async;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.letscooee.models.Campaign;
import com.letscooee.models.Event;
import com.letscooee.retrofit.APIClient;
import com.letscooee.retrofit.ServerAPIService;
import com.letscooee.utils.CooeeSDKConstants;

import java.io.IOException;
import java.net.ConnectException;

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
            Log.d(CooeeSDKConstants.LOG_PREFIX, response.code() + "");
        } catch (ConnectException e){
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context,"Not connected to server, check your internet",Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if( response==null){
            return null;
        }
        return response.body();
    }
}
