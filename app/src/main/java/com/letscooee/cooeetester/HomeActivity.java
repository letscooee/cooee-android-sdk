package com.letscooee.cooeetester;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.letscooee.CooeeSDK;
import com.letscooee.cooeetester.databinding.ActivityHomeBinding;
import com.letscooee.utils.InAppNotificationClickListener;
import com.letscooee.utils.PropertyNameException;

import java.util.HashMap;

public class HomeActivity extends AppCompatActivity implements InAppNotificationClickListener {
    private CooeeSDK cooeeSDK;
    private final String TAG = "HomeActivity";
    private ActivityHomeBinding binding;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        cooeeSDK = CooeeSDK.getDefaultInstance(this);
        cooeeSDK.setInAppNotificationButtonListener(this);

        new CountDownTimer(1000, 3000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {

                binding.ivSplash.setVisibility(View.GONE);
            }
        }.start();

        try {
            cooeeSDK.setCurrentScreen(TAG);

        } catch (Exception ignored) {
        }

        try {
            cooeeSDK.sendEvent("onCreate", new HashMap<>());
        } catch (Exception ignored) {
        }

        try {
            Log.d(TAG, "************************************UUID : " + cooeeSDK.getUUID());
        } catch (Exception ignored) {
        }

        binding.btnSendImageEvent.setOnClickListener(view -> {
            try {
                cooeeSDK.sendEvent("image", new HashMap<>());
                Log.d(TAG, "****************************Image Event Sent");
            } catch (PropertyNameException e) {
                Log.e(TAG, "******************************Failed Image Event", e);
                e.printStackTrace();
            }
        });

        binding.btnSendVideoEvent.setOnClickListener(view -> {
            try {
                cooeeSDK.sendEvent("video", new HashMap<>());
                Log.d(TAG, "****************************Video Event Sent");
            } catch (PropertyNameException e) {
                Log.e(TAG, "******************************Failed Video Event", e);
                e.printStackTrace();
            }
        });
        binding.btnProfile.setOnClickListener(view -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        try {
            cooeeSDK.sendEvent("onDestroy", new HashMap<>());
        } catch (Exception ignored) {
        }

        super.onDestroy();
    }

    @Override
    public void onInAppButtonClick(HashMap<String, String> hashMap) {
        Log.d(TAG, "onInAppButtonClick: ************************************" + hashMap.toString());
    }
}