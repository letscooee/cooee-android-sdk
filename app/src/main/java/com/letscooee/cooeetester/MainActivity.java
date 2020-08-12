package com.letscooee.cooeetester;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
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

        mySdk = CooeeSDK.getDefaultInstance(this);

        buttonImage = findViewById(R.id.btnImage);
        buttonVideo = findViewById(R.id.btnVideo);

        Map<String,Object> userData = new HashMap<>();
        userData.put("fullName","Abhishek Taparia");
        userData.put("address","Main Market");
        userData.put("mobileNumber","9879156641");

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sending event to the server
                Map<String, String> eventParameters = new HashMap<>();
                eventParameters.put("Image Name", "pikachu");
                mySdk.sendEvent(CooeeSDKConstants.IMAGE_CAMPAIGN, eventParameters);
            }
        });

        buttonVideo.setOnClickListener(view -> {
            mySdk.sendEvent(CooeeSDKConstants.VIDEO_CAMPAIGN, new HashMap<>());
        });

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast

                        Log.d("TAG", token);
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}