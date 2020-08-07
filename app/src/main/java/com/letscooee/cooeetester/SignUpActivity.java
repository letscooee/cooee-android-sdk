package com.letscooee.cooeetester;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.letscooee.cooeesdk.CooeeSDK;
import com.letscooee.cooeetester.R;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    TextView textViewName;
    TextView textViewMobile;
    TextView textViewEmail;
    Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        textViewEmail = findViewById(R.id.textViewEmail);
        textViewMobile = findViewById(R.id.textViewMobile);
        textViewName = findViewById(R.id.textViewName);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(view -> {
            Map<String, String> userProperties = new HashMap<>();
            userProperties.put("name1", "v1");
            userProperties.put("name2", "v2");
            userProperties.put("name3", "v3");
            Map<String, String> userData = new HashMap<>();
            userData.put("name", textViewName.getText().toString());
            userData.put("mobile", textViewMobile.getText().toString());
            userData.put("email", textViewEmail.getText().toString());

            CooeeSDK cooeeSDK = CooeeSDK.getDefaultInstance(this);
            cooeeSDK.updateProfile(userData);
        });

    }
}