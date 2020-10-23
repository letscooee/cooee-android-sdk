package com.letscooee.cooeetester;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.widget.Button;

import com.letscooee.cooeesdk.CooeeSDK;
import com.letscooee.utils.CooeeSDKConstants;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private Button buttonVideo, buttonImage;
    private String location[];
    private CooeeSDK mySdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPermission();
        mySdk = CooeeSDK.getDefaultInstance(getApplicationContext());

        buttonImage = findViewById(R.id.btnImage);
        buttonVideo = findViewById(R.id.btnVideo);

        Map<String, String> userData = new HashMap<>();
        userData.put("fullName", "Abhishek Taparia");
        userData.put("address", "Main Market");
        userData.put("mobileNumber", "9876543210");
        userData.put("cemobileNumber", "9876543210");

//        try {
//            mySdk.updateUserProperties(userData);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        buttonImage.setOnClickListener(view -> {
            // sending event to the server
            mySdk.sendEvent(CooeeSDKConstants.IMAGE_CAMPAIGN, new HashMap<>());
        });

        buttonVideo.setOnClickListener(view -> {
            mySdk.sendEvent(CooeeSDKConstants.VIDEO_CAMPAIGN, new HashMap<>());
        });

        findViewById(R.id.textViewToken).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void setPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CooeeSDKConstants.REQUEST_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, CooeeSDKConstants.REQUEST_LOCATION);
        }
    }
}