package com.letscooee.campaign;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.letscooee.R;

import java.util.Objects;

/**
 * @author Abhishek Taparia
 * ImagePopUpActivity create layout for image campaign
 */
public class ImagePopUpActivity extends Activity {

    private ImageView imageView;
    private TextView textViewTitle;
    private Button buttonClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pop_up);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setLayout((int) (dm.widthPixels * 0.7), (int) (dm.heightPixels * 0.45));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
        String title = getIntent().getStringExtra("title");
        String mediaURL = getIntent().getStringExtra("mediaURL");
        int transitionId;
        String autoClose = getIntent().getStringExtra("autoClose");
        Log.d("AutoClose in activity", autoClose + "");
        switch (Objects.requireNonNull(getIntent().getStringExtra("transitionSide"))) {
            case "right": {
                transitionId = R.anim.slide_right;
                break;
            }
            case "left": {
                transitionId = android.R.anim.slide_in_left;
                break;
            }
            case "up": {
                transitionId = R.anim.slide_up;
                break;
            }
            case "down": {
                transitionId = R.anim.slide_down;
                break;
            }
            default: {
                Log.i("default", "true");
                transitionId = R.anim.slide_up;
            }
        }

        overridePendingTransition(transitionId, R.anim.no_change);

        textViewTitle = findViewById(R.id.textViewTitle);
        imageView = findViewById(R.id.imageViewMedia);
        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(view -> finish());

        textViewTitle.setText(title);
        Glide.with(getApplicationContext()).load(mediaURL).into(imageView);

        new Handler().postDelayed(this::finish, Integer.parseInt(autoClose) * 1000);
    }
}