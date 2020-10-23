package com.letscooee.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
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

import static com.letscooee.utils.CooeeSDKConstants.LOG_PREFIX;

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
        if (this.context != null) {
            ServerAPIService apiService = APIClient.getServerAPIService();
            Response<Campaign> response = null;

            try {
                String header = this.context.getSharedPreferences(CooeeSDKConstants.SDK_TOKEN, Context.MODE_PRIVATE).getString(CooeeSDKConstants.SDK_TOKEN, "");
                response = apiService.sendEvent(header, events[0]).execute();
                Log.d(CooeeSDKConstants.LOG_PREFIX, response.code() + "");
            } catch (IOException e) {
                Log.e(LOG_PREFIX + " bodyError", e.toString());
            }

            if (response == null) {
                return null;
            }
            return response.body();
        }
        return null;
    }
}
