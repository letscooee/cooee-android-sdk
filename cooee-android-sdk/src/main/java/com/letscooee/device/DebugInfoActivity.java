package com.letscooee.device;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.letscooee.BuildConfig;
import com.letscooee.R;
import com.letscooee.models.DebugInformation;
import com.letscooee.trigger.inapp.PreventBlurActivity;
import com.letscooee.utils.Constants;
import com.letscooee.utils.DebugInfoCollector;

import java.util.Calendar;
import java.util.List;

/**
 * Show debug information for inter use only
 *
 * @author Ashish Gaikwad 09/09/21
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DebugInfoActivity extends AppCompatActivity implements PreventBlurActivity {

    private static boolean isUserAuthorisedPreviously = false;

    private final String validPassword;
    private Intent shareIntent;

    public DebugInfoActivity() {
        Calendar calendar = Calendar.getInstance();
        int month = (calendar.get(Calendar.MONTH) + 1) + Constants.INCREMENT_PASSWORD;
        int minute = calendar.get(Calendar.MINUTE) + Constants.INCREMENT_PASSWORD;
        validPassword = minute + "" + month;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is authorized in current session
        if (BuildConfig.DEBUG || isUserAuthorisedPreviously) {
            showInfoScreen();
        } else {
            showPasswordScreen();
        }
    }

    /**
     * If SDK is in release mode and user is not authorized in current session password screen
     * will appear.
     */
    private void showPasswordScreen() {
        setContentView(R.layout.view_password);

        Button button = findViewById(R.id.btnNext);
        button.setOnClickListener(v -> checkPasswordAndProceed());

        ImageView imageViewClose = findViewById(R.id.closeButton);
        imageViewClose.setOnClickListener(v -> finish());
    }

    /**
     * Access entered password and will check for valid password.
     * If password is valid then debug information screen will be loaded.
     */
    private void checkPasswordAndProceed() {
        TextInputEditText passwordEditText = findViewById(R.id.edtPassword);
        TextView passwordWarning = findViewById(R.id.tvPasswordWarning);

        @SuppressWarnings("ConstantConditions")
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(password)) {
            passwordWarning.setVisibility(View.VISIBLE);
            passwordWarning.setText("Enter Password");
            return;
        } else if (!password.equals(validPassword)) {
            passwordWarning.setVisibility(View.VISIBLE);
            passwordWarning.setText("Invalid Password");
            return;
        }

        passwordWarning.setVisibility(View.INVISIBLE);
        passwordEditText.setText("");
        isUserAuthorisedPreviously = true;
        showInfoScreen();
    }

    /**
     * Fetch and show all debug information on the screen.
     */
    private void showInfoScreen() {
        setContentView(R.layout.debug_info_activity);

        LinearLayout deviceInformationLayout = findViewById(R.id.deviceInformationLayout);
        LinearLayout userInformationLayout = findViewById(R.id.userInformationLayout);

        int rowLayout = R.layout.view_info_row;

        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        DebugInfoCollector debugInfoCollector = new DebugInfoCollector(this);
        List<DebugInformation> deviceInformation = debugInfoCollector.getDeviceInformation();
        List<DebugInformation> userInformation = debugInfoCollector.getUserInformation();

        for (DebugInformation property : deviceInformation) {
            View view = LayoutInflater.from(this).inflate(rowLayout, null);
            ((TextView) view.findViewById(R.id.tvTitle)).setText(property.getKey());
            ((TextView) view.findViewById(R.id.tvValue)).setText(property.getValue());

            view.setOnClickListener(v -> {
                shareData(property);
            });


            deviceInformationLayout.addView(view);
        }

        for (DebugInformation property : userInformation) {
            View view = LayoutInflater.from(this).inflate(rowLayout, null);
            ((TextView) view.findViewById(R.id.tvTitle)).setText(property.getKey());
            ((TextView) view.findViewById(R.id.tvValue)).setText(property.getValue());

            view.setOnClickListener(v -> {
                shareData(property);
            });

            userInformationLayout.addView(view);
        }

        ImageView imageViewClose = findViewById(R.id.closeButton);
        imageViewClose.setOnClickListener(v -> finish());
    }

    /**
     * Launches share intent
     * @param property
     */
    private void shareData(DebugInformation property) {
        if (!property.isSharable()) {
            return;
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, property.getValue());
        startActivity(Intent.createChooser(shareIntent, "Share with"));
    }
}