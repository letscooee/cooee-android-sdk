package com.wizpanda.cooeetester;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import com.wizpanda.cooee.cooeesdk.MySdk;
import com.wizpanda.cooee.utils.Constants;


public class MainActivity extends AppCompatActivity {

    private Button buttonVideo, buttonImage;
    private String location[];
    private MySdk mySdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySdk = MySdk.getDefaultInstance(this);
        location = mySdk.getLocation();

        buttonImage = findViewById(R.id.btnImage);
        buttonVideo = findViewById(R.id.btnVideo);

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sending event to the server
                mySdk.sendEvent(Constants.IMAGE_CAMPAIGN);
            }
        });
    }
}