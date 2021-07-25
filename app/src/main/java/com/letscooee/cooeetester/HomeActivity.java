package com.letscooee.cooeetester;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.letscooee.CooeeSDK;
import com.letscooee.cooeetester.databinding.ActivityHomeBinding;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.utils.InAppNotificationClickListener;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

public class HomeActivity extends AppCompatActivity implements InAppNotificationClickListener {

    private final String TAG = "HomeActivity";

    private CooeeSDK cooeeSDK;
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
        cooeeSDK.setCurrentScreen(TAG);

        Log.d(TAG, "User ID " + cooeeSDK.getUserID());

        binding.btnSendImageEvent.setOnClickListener(view -> {
            cooeeSDK.sendEvent("image", new HashMap<>());
        });

        binding.inApp1.setOnClickListener(view -> {
            this.openInApp(R.raw.sample_payload1);
        });

        binding.btnProfile.setOnClickListener(view -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        binding.tvUid.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", cooeeSDK.getUserID());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void openInApp(int rawRes) {
        // https://stackoverflow.com/a/49507293/2405040
        InputStream inputStream = context.getResources().openRawResource(rawRes);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();

        EngagementTriggerHelper.renderInAppTriggerFromJSONString(context, jsonString);
    }

    @Override
    public void onInAppButtonClick(HashMap<String, Object> hashMap) {
        for (String key : hashMap.keySet()) {
            Log.d("Type ::", key + " -> " + hashMap.get(key).getClass().getName());
        }
    }
}