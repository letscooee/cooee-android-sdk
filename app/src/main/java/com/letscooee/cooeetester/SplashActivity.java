package com.letscooee.cooeetester;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.letscooee.CooeeSDK;
import com.letscooee.cooeetester.databinding.ActivitySplashBinding;

import java.util.HashMap;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;
    private CooeeSDK sdk;
    private final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception e) {
        }
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_splash);
        sdk = CooeeSDK.getDefaultInstance(this);
        try {
            sdk.setCurrentScreen(TAG);
            sdk.sendEvent("onCreate", new HashMap<>());
        } catch (Exception ignored) {
        }
        new CountDownTimer(1000, 3000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                Log.d(TAG, "inside finish");
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        try {
            sdk.sendEvent("onDestroy", new HashMap<>());
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }
}