package com.letscooee.device;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.letscooee.R;
import com.letscooee.trigger.inapp.PreventBlurActivity;
import com.letscooee.utils.DebugInfoCollector;

import java.util.Map;
import java.util.TreeMap;

public class CooeeDebugInfoActivity extends AppCompatActivity implements PreventBlurActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooee_debug_info);
        ImageView imageViewClose = findViewById(R.id.imageViewClose);
        imageViewClose.setOnClickListener(v -> finish());
        LinearLayout linearLayoutInfo = findViewById(R.id.linearLayoutInfo);

        int headingLayout = R.layout.view_info_heading;
        int rowLayout = R.layout.view_info_row;

        DebugInfoCollector debugInfoCollector = new DebugInfoCollector(this);
        TreeMap<String, TreeMap<String, Object>> debugInfo = debugInfoCollector.getDebugInfo();

        for (Map.Entry<String, TreeMap<String, Object>> header : debugInfo.entrySet()) {
            TreeMap<String, Object> valueMap = header.getValue();
            View titleView = LayoutInflater.from(this).inflate(headingLayout, null);
            ((TextView) titleView.findViewById(R.id.tvTitle)).setText(header.getKey());
            linearLayoutInfo.addView(titleView);

            for (Map.Entry<String, Object> property : valueMap.entrySet()) {
                View view = LayoutInflater.from(this).inflate(rowLayout, null);
                ((TextView) view.findViewById(R.id.tvTitle)).setText(property.getKey());
                ((TextView) view.findViewById(R.id.tvValue)).setText(property.getValue().toString());
                linearLayoutInfo.addView(view);
            }
        }
    }
}