package com.letscooee.cooeetester;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;

import com.letscooee.CooeeSDK;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.letscooee.utils.CooeeSDKConstants;
import com.letscooee.utils.InAppNotificationClickListener;
import com.letscooee.utils.PropertyNameException;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements InAppNotificationClickListener {

    private Button buttonVideo, buttonImage;
    private String location[];
    private CooeeSDK mySdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPermission();

        mySdk = CooeeSDK.getDefaultInstance(getApplicationContext());

        mySdk.setInAppNotificationButtonListener(this);

        buttonImage = findViewById(R.id.btnImage);
        buttonVideo = findViewById(R.id.btnVideo);

        Map<String, String> userData = new HashMap<>();
        userData.put("fullName", "Abhishek Taparia");
        userData.put("address", "Main Market");
        userData.put("mobileNumber", "9876543210");
        userData.put("cemobileNumber", "9876543210");

        try {
            mySdk.sendEvent("onCreate", new HashMap<>());
        } catch (PropertyNameException e) {
            e.printStackTrace();
        }
        buttonImage.setOnClickListener(view -> {
            // sending event to the server
            try {
                Map<String, String> eventProp = new HashMap<>();
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
        Glide.with(this)
                .asBitmap()
                .load("https://images.unsplash.com/photo-1606845023594-4395299f0b23?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=675&q=80")
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        drawable.setCornerRadius(20);
                        findViewById(R.id.linearLayout2).setBackground(drawable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

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

    @Override
    public void onInAppButtonClick(HashMap<String, String> payload) {
        if (payload != null){
            Log.d("Data from inApp", payload.toString());
//            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        }
    }
}