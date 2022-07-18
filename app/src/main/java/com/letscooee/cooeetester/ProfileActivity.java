package com.letscooee.cooeetester;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.letscooee.CooeeSDK;
import com.letscooee.cooeetester.databinding.ActivityProfileBinding;
import com.letscooee.utils.PropertyNameException;

import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ActivityProfileBinding binding;
    private CooeeSDK cooee;
    private String animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        animation = intent.getStringExtra("animation");
        if (TextUtils.isEmpty(animation)) {
            animation = "slide";
        }
        overrideAnimation(animation);

        cooee = CooeeSDK.getDefaultInstance(this);
        binding.ivBack.setOnClickListener(v -> finish());
        cooee.setCurrentScreen(TAG);
        binding.btnSave.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.edtPersonName.getText().toString())) {
                Toast.makeText(this, "Name can not be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(binding.edtEmail.getText().toString())) {
                Toast.makeText(this, "Email can not be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(binding.edtMobile.getText().toString())) {
                Toast.makeText(this, "Mobile can not be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", binding.edtPersonName.getText().toString());
            userData.put("mobile", binding.edtMobile.getText().toString());
            userData.put("email", binding.edtEmail.getText().toString());

            try {
                cooee.updateUserProfile(userData);
                Toast.makeText(this, "Data Sent", Toast.LENGTH_SHORT).show();
            } catch (PropertyNameException e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overrideAnimation(animation);

    }

    private void overrideAnimation(String animation) {
        switch (animation) {
            case "top_right":
                overridePendingTransition(R.anim.slide_in_top_right, R.anim.slide_out_top_right);
                break;
            case "bottom_left":
                overridePendingTransition(R.anim.slide_in_bottom_left, R.anim.slide_out_bottom_left);
                break;
            case "bottom_right":
                overridePendingTransition(R.anim.slide_in_bottom_right, R.anim.slide_out_bottom_right);
                break;
            default:
                overridePendingTransition(R.anim.slide_in_top_left, R.anim.slide_out_top_left);
        }
    }
}