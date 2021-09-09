package com.letscooee.device;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.letscooee.BuildConfig;
import com.letscooee.R;
import com.letscooee.trigger.inapp.PreventBlurActivity;
import com.letscooee.utils.Constants;
import com.letscooee.utils.DebugInfoCollector;

import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

/**
 * Show debug information for inter use only
 *
 * @author Ashish Gaikwad 09/09/21
 * @since 1.0.0
 */
public class CooeeDebugInfoActivity extends AppCompatActivity implements PreventBlurActivity {

    private final String ACTIVITY_PASSWORD;
    private static boolean IS_USER_AUTHORIZED_PREVIOUSLY = false;

    public CooeeDebugInfoActivity() {
        Calendar calendar = Calendar.getInstance();
        int month = (calendar.get(Calendar.MONTH) + 1) + Constants.INCREMENT_PASSWORD;
        int minute = calendar.get(Calendar.MINUTE) + Constants.INCREMENT_PASSWORD;
        ACTIVITY_PASSWORD = minute + "" + month;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If SDK is in DEBUG mode debug information will shown directly
        if (BuildConfig.DEBUG) {
            showInfoScreen();
            return;
        }

        // Check if user is authorized in current session
        if (IS_USER_AUTHORIZED_PREVIOUSLY) {
            showInfoScreen();
        } else {
            showPasswordScreen();
        }
    }

    /**
     * If SDK is in release mode and use is not authorized in current session password screen
     * will appear.
     */
    private void showPasswordScreen() {
        setContentView(R.layout.view_password);

        Button button = findViewById(R.id.btnNext);
        button.setOnClickListener(v -> checkPasswordAndProceed());

        ImageView imageViewClose = findViewById(R.id.imageViewClose);
        imageViewClose.setOnClickListener(v -> finish());
    }

    /**
     * Access entered password and will check for valid password.
     * If password is valid then debug information screen will be loaded.
     */
    private void checkPasswordAndProceed() {
        TextInputEditText passwordEditText = findViewById(R.id.edtPassword);
        TextView passwordWarning = findViewById(R.id.tvPasswordWarning);

        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(password)) {
            passwordWarning.setVisibility(View.VISIBLE);
            passwordWarning.setText("Enter Password");
            return;
        } else if (!password.equals(ACTIVITY_PASSWORD)) {
            passwordWarning.setVisibility(View.VISIBLE);
            passwordWarning.setText("Entered Invalid Password");
            return;
        }

        passwordWarning.setVisibility(View.INVISIBLE);
        passwordEditText.setText("");
        IS_USER_AUTHORIZED_PREVIOUSLY = true;
        showInfoScreen();
    }

    /**
     * Fetch and show all debug information on the screen.
     */
    private void showInfoScreen() {
        setContentView(R.layout.activity_cooee_debug_info);

        LinearLayout linearLayoutInfo = findViewById(R.id.linearLayoutInfo);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
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

                view.setOnClickListener(v -> {
                    ClipData clip = ClipData.newPlainText(property.getKey(), property.getValue().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, property.getKey() + " Copied", Toast.LENGTH_SHORT).show();
                });
                linearLayoutInfo.addView(view);
            }
        }

        ImageView imageViewClose = findViewById(R.id.imageViewClose);
        imageViewClose.setOnClickListener(v -> finish());
    }
}