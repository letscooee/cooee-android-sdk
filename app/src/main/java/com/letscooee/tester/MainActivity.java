package com.letscooee.tester;


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


import com.letscooee.android.cooeesdk.MySdk;
import com.letscooee.android.models.Campaign;
import com.letscooee.android.utils.Constants;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {

    private Button buttonVideo, buttonImage;
    private String location[];
    private MySdk mySdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySdk = new MySdk(this);
        location = mySdk.getLocation(MainActivity.this);

        buttonImage = findViewById(R.id.btnImage);
        buttonVideo = findViewById(R.id.btnVideo);

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyAsyncClass().execute(Constants.IMAGE_CAMPAIGN);
            }
        });
    }

    private class MyAsyncClass extends AsyncTask<String, Void, Campaign> {

        @Override
        protected Campaign doInBackground(String... strings) {
            return mySdk.sendEvent(Constants.IMAGE_CAMPAIGN);
        }

        @Override
        protected void onPostExecute(Campaign s) {
            super.onPostExecute(s);
            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.image_campaign, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


            ImageView imageView = popupView.findViewById(R.id.imageView);
            Picasso.with(getApplicationContext()).load(s.getMediaURL()).into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();
                }
            });
            popupWindow.showAtLocation(buttonImage, Gravity.CENTER, 0, 0);
        }
    }
}