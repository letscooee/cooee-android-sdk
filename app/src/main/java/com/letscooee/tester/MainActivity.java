package com.letscooee.tester;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.letscooee.android.retrofit.RetrofitClient;
import com.letscooee.android.retrofit.ServerAPIService;

import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String IS_APP_FIRST_TIME_LAUNCH = "is_app_first_time_launch";
    private static final String SDK_TOKEN = "com.letscooee.tester";

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(IS_APP_FIRST_TIME_LAUNCH, Context.MODE_PRIVATE);
        mSharedPreferencesEditor= mSharedPreferences.edit();

        if(isAppFirstTimeLaunch()){
            ((TextView)findViewById(R.id.textView)).setBackgroundColor(Color.RED);
            ((TextView)findViewById(R.id.textView)).setText("APP is launch for first time.");
            ServerAPIService apiService = RetrofitClient.getServerAPIService();
            apiService.firstOpen().enqueue(new retrofit2.Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
//                    mSharedPreferences = getSharedPreferences(SDK_TOKEN, Context.MODE_PRIVATE);
//                    mSharedPreferencesEditor= mSharedPreferences.edit();
                    Log.i("body",response.body());
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.i("bodyabc",t.toString());
                }
            });
        }else {
            ((TextView)findViewById(R.id.textView)).setBackgroundColor(Color.GREEN);
            ((TextView)findViewById(R.id.textView)).setText("App previously opened.");
        }

    }

    protected boolean isAppFirstTimeLaunch(){
        if(mSharedPreferences.getBoolean(IS_APP_FIRST_TIME_LAUNCH,true)){
            // App is open/launch for first time
            // Update the preference
            mSharedPreferencesEditor.putBoolean(IS_APP_FIRST_TIME_LAUNCH,false);
            mSharedPreferencesEditor.commit();
            mSharedPreferencesEditor.apply();

            return true;
        }else {
            // App previously opened
            return false;
        }
    }
}