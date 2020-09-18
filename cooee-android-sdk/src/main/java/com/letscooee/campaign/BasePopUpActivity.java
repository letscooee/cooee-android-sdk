package com.letscooee.campaign;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.letscooee.R;

import java.util.Objects;

/**
 * @author Abhishek Taparia
 * BasePopUpActivity does the basic common task of popup activities
 */
public class BasePopUpActivity extends Activity {

    protected String title;
    protected String autoClose;
    protected String mediaURL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void onCreated(Bundle bundle) {
        int transitionId;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setLayout((int) (dm.widthPixels * 0.7), (int) (dm.heightPixels * 0.45));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);

        assert bundle != null;
        title = Objects.requireNonNull(bundle.get("title")).toString();
        mediaURL = Objects.requireNonNull(bundle.get("mediaURL")).toString();
        try {
            autoClose = Objects.requireNonNull(bundle.get("autoClose")).toString();
        } catch (NullPointerException ex) {
            autoClose = null;
        }
        switch (Objects.requireNonNull(bundle.get("transitionSide")).toString()) {
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
                transitionId = R.anim.slide_up;
            }
        }
        overridePendingTransition(transitionId, R.anim.no_change);
    }
}
