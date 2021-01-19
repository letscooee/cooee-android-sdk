package com.letscooee.cooeetester;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.widget.Button;

import com.letscooee.CooeeSDK;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.PropertyNameException;

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

        try {
            mySdk.sendEvent("onCreate",new HashMap<>());
        } catch (PropertyNameException e) {
            e.printStackTrace();
        }
        buttonImage.setOnClickListener(view -> {
            // sending event to the server
            try {
                Map<String,String> eventProp = new HashMap<>();
                eventProp.put("key1", "value1");
                mySdk.sendEvent("image", eventProp);
            } catch (PropertyNameException e) {
                e.printStackTrace();
            }
        });

        buttonVideo.setOnClickListener(view -> {
            try {
                mySdk.sendEvent("video", new HashMap<>());
            } catch (PropertyNameException e) {
                e.printStackTrace();
            }
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