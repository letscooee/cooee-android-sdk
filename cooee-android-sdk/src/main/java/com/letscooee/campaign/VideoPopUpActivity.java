package com.letscooee.campaign;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.letscooee.R;

import java.util.Objects;

public class VideoPopUpActivity extends Activity {

    private VideoView videoView;
    private TextView textViewTitle;
    private Button buttonClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_pop_up);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setLayout((int)(dm.widthPixels*0.7),(int)(dm.heightPixels*0.45));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x=0;
        params.y=-20;

        getWindow().setAttributes(params);
        String title = getIntent().getStringExtra("title");
        String mediaURL = getIntent().getStringExtra("mediaURL");
        int transitionId;
        String autoClose = getIntent().getStringExtra("autoClose");
        Log.d("AutoClose in activity",autoClose+"");
        switch (Objects.requireNonNull(getIntent().getStringExtra("transitionSide"))){
            case "right":{
                transitionId= R.anim.slide_right;
                break;
            }
            case "left":{
                transitionId=android.R.anim.slide_in_left;
                break;
            }
            case "up":{
                transitionId=R.anim.slide_up;
                break;
            }
            case "down":{
                transitionId=R.anim.slide_down;
                break;
            }
            default:{
                Log.i("default","true");
                transitionId=R.anim.slide_up;
            }
        }

        overridePendingTransition(transitionId,R.anim.no_change);

        textViewTitle = findViewById(R.id.textViewTitle);
        videoView = findViewById(R.id.videoViewMedia);
        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(view -> finish());

        textViewTitle.setText(title);
        videoView.setVideoPath(mediaURL);
        videoView.start();

        Runnable runnable = this::finish;
        if (autoClose==null){
            Log.d("inside","autoClose");
        }
        else{
            Log.d("not inside","autoClose");
            new Handler().postDelayed(runnable,Integer.parseInt(autoClose)*1000);
        }
    }
}