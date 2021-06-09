package com.letscooee.trigger.inapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;

import com.letscooee.R;
import com.letscooee.models.TriggerData;
import com.letscooee.utils.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class FeedbackActivity extends AppCompatActivity {

    private String liked = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setLayout((int) (dm.widthPixels * 0.3), (int) (dm.heightPixels * 0.3));
        } else {
            getWindow().setLayout((int) (dm.widthPixels * 0.7), (int) (dm.heightPixels * 0.15));
        }

        updateExit((TriggerData.ExitAnimation) Objects.requireNonNull(getIntent().getSerializableExtra("exit")));

        setContentView(R.layout.activity_feedback);

        Button buttonThumpUp = findViewById(R.id.buttonThumbUp);
        buttonThumpUp.setOnClickListener(view -> {
            liked = "true";
            finish();
        });

        Button buttonThumpDown = findViewById(R.id.buttonThumbDown);
        buttonThumpDown.setOnClickListener(view -> {
            liked = "false";
            finish();
        });
    }

    private void updateExit(TriggerData.ExitAnimation animation) {
        int transitionId;
        switch (animation) {
            case SLIDE_OUT_LEFT: {
                transitionId = R.anim.slide_out_left;
                break;
            }
            case SLIDE_OUT_RIGHT: {
                transitionId = R.anim.slide_out_right;
                break;
            }
            case SLIDE_OUT_TOP: {
                transitionId = R.anim.slide_out_up;
                break;
            }
            case SLIDE_OUT_DOWN: {
                transitionId = R.anim.slide_out_down;
                break;
            }
            default: {
                transitionId = R.anim.slide_out_right;
                break;
            }
        }
        overridePendingTransition(R.anim.slide_in_down, transitionId);
    }

    @Override
    public void finish() {
        super.finish();
        Bundle bundle = getIntent().getExtras().getBundle("review");
        if (bundle == null) {
            Log.d(Constants.LOG_PREFIX, "review null");
            return;
        }

        Map<String, String> eventProperties = new HashMap<>();

        if (liked.isEmpty()) {
            liked = "No Review";
        }

        eventProperties.put("CE Liked", liked);
        for (String key : bundle.keySet()) {
            eventProperties.put(key, bundle.getString(key));
            Log.d(key, bundle.getString(key));
        }
    }
}
