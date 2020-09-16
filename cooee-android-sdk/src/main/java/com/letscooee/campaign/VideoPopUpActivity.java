package com.letscooee.campaign;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.letscooee.R;

import java.util.Objects;

/**
 * @author Abhishek Taparia
 * VideoPopUpActivity create layout for video campaign
 */
public class VideoPopUpActivity extends BasePopUpActivity {

    private VideoView videoView;
    private TextView textViewTitle;
    private Button buttonClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreated(Objects.requireNonNull(getIntent().getExtras()));
        setContentView(R.layout.activity_video_pop_up);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText(title);

        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(view -> finish());
        if (autoClose == null || autoClose.equals("")) {
            buttonClose.setVisibility(View.VISIBLE);
        } else {
            buttonClose.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(this::finish, Integer.parseInt(autoClose) * 1000);
        }

        videoView = findViewById(R.id.videoViewMedia);
        videoView.setVideoPath(mediaURL);
        videoView.start();
    }
}