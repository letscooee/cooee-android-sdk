package com.wizpanda.cooeetester;


import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.wizpanda.cooee.cooeesdk.MySdk;
import com.wizpanda.cooee.models.Campaign;
import com.wizpanda.cooee.utils.Constants;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {

    private Button buttonVideo, buttonImage;
    private String location[];
    private MySdk mySdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySdk = MySdk.getDefaultInstance(this);
        location = mySdk.getLocation(MainActivity.this);

        buttonImage = findViewById(R.id.btnImage);
        buttonVideo = findViewById(R.id.btnVideo);

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sending event to the server
                Campaign campaign = mySdk.sendEvent(Constants.IMAGE_CAMPAIGN);
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.image_campaign, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ImageView imageView = popupView.findViewById(R.id.imageView);
                Picasso.with(getApplicationContext()).load(campaign.getMediaURL()).into(imageView);

                TextView textView = popupView.findViewById(R.id.textView);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });
                popupWindow.showAtLocation(buttonImage, Gravity.CENTER, 0, 0);
            }
        });
    }
}