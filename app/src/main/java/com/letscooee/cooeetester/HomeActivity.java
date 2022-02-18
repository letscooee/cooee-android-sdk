package com.letscooee.cooeetester;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.letscooee.CooeeSDK;
import com.letscooee.cooeetester.databinding.ActivityHomeBinding;
import com.letscooee.trigger.EngagementTriggerHelper;
import com.letscooee.utils.CooeeCTAListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

public class HomeActivity extends AppCompatActivity implements CooeeCTAListener {

    private CooeeSDK cooeeSDK;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.letscooee.cooeetester.databinding.ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        cooeeSDK = CooeeSDK.getDefaultInstance(this);
        cooeeSDK.setCTAListener(this);
        String TAG = "HomeActivity";
        cooeeSDK.setCurrentScreen(TAG);

        Log.d(TAG, "User ID " + cooeeSDK.getUserID());

        binding.btnSendImageEvent.setOnClickListener(view -> {
            cooeeSDK.sendEvent("Add To Cart", new HashMap<>());
            cooeeSDK.sendEvent("Add To Cart");
        });

        Intent intent = new Intent(this, ProfileActivity.class);
        binding.btnProfile.setOnClickListener(view -> startActivity(intent));

        binding.tvUid.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", cooeeSDK.getUserID());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
        });

        binding.ivDebugInfo.setOnClickListener(v -> cooeeSDK.showDebugInfo());

        /************** Animation Calls **********************/

        binding.btnTopLeft.setOnClickListener(view -> {
            intent.putExtra("animation", "top_left");
            startActivity(intent);
        });

        binding.btnTopRight.setOnClickListener(view -> {
            intent.putExtra("animation", "top_right");
            startActivity(intent);
        });

        binding.btnBottomLeft.setOnClickListener(view -> {
            intent.putExtra("animation", "bottom_left");
            startActivity(intent);
        });

        binding.btnBottomRight.setOnClickListener(view -> {
            intent.putExtra("animation", "bottom_right");
            startActivity(intent);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void openInApp(int rawRes) {
        // https://stackoverflow.com/a/49507293/2405040
        InputStream inputStream = context.getResources().openRawResource(rawRes);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            // To prevent adding "id" everytime in the sample JSON
            jsonObject.put("id", "TEST-1");
            jsonString = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        EngagementTriggerHelper.renderInAppTriggerFromJSONString(context, jsonString);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResponse(HashMap<String, Object> hashMap) {
        for (String key : hashMap.keySet()) {
            Log.d("Type ::", key + " -> " + hashMap.get(key).getClass().getName());
        }
    }
}