package com.letscooee.cooeetester;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.letscooee.CooeeSDK;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextMobile;
    EditText editTextEmail;
    Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextEmail = findViewById(R.id.textViewEmail);
        editTextMobile = findViewById(R.id.textViewMobile);
        editTextName = findViewById(R.id.textViewName);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(view -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", editTextName.getText().toString());
            userData.put("mobile", editTextMobile.getText().toString());
            userData.put("email", editTextEmail.getText().toString());

            CooeeSDK cooeeSDK = CooeeSDK.getDefaultInstance(getApplicationContext());
            try {
                cooeeSDK.updateUserProfile(userData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}