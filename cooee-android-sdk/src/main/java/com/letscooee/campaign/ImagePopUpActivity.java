package com.letscooee.campaign;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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
public class ImagePopUpActivity extends BasePopUpActivity {

    private ImageView imageView;
    private TextView textViewTitle;
    private Button buttonClose;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreated(Objects.requireNonNull(getIntent().getExtras()));
        setContentView(R.layout.activity_image_pop_up);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText(title);

        imageView = findViewById(R.id.imageViewMedia);
        Glide.with(getApplicationContext()).load(mediaURL).into(imageView);

        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(view -> finish());
        if (autoClose == null || autoClose.equals("")) {
            buttonClose.setVisibility(View.VISIBLE);
        } else {
            buttonClose.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(this::finish, Integer.parseInt(autoClose) * 1000);
        }
    }
}