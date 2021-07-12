package com.letscooee.trigger.inapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.R;
import com.letscooee.models.v3.CoreTriggerData;
import com.letscooee.models.v3.block.Size;
import com.letscooee.models.v3.inapp.Container;
import com.letscooee.models.v3.inapp.InAppData;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.utils.Constants;
import com.letscooee.utils.SentryHelper;
import com.letscooee.utils.UIUtil;

import java.lang.ref.WeakReference;

public class InAppTriggerActivityNew extends AppCompatActivity implements PreventBlurActivity {

    private static Window lastActiveWindow;

    private boolean isFreshLaunch;
    private ConstraintLayout viewInAppTriggerRoot;

    private CoreTriggerData triggerData;
    private InAppData inAppData;
    private Bitmap bitmapForBlurry;
    private ViewGroup viewGroupForBlurry;

    DisplayMetrics displayMetrics;

    private final SafeHTTPService safeHTTPService;
    private final SentryHelper sentryHelper;
    private WeakReference<InAppTriggerActivity.InAppListener> inAppListenerWeakReference;

    public InAppTriggerActivityNew() {
        safeHTTPService = CooeeFactory.getSafeHTTPService();
        sentryHelper = CooeeFactory.getSentryHelper();
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.isFreshLaunch = savedInstanceState == null;
        setContentView(R.layout.activity_engagement_trigger_new);
        viewInAppTriggerRoot = findViewById(R.id.inAppTriggerRoot);
        inAppListenerWeakReference = new WeakReference<>(CooeeSDK.getDefaultInstance(this));

        displayMetrics = getResources().getDisplayMetrics();
        try {
            triggerData = getIntent()
                    .getBundleExtra("bundle")
                    .getParcelable(Constants.INTENT_TRIGGER_DATA_KEY);

            if (triggerData == null || triggerData.getIan() == null) {
                throw new Exception("Couldn't render In-App because trigger data is null");
            }

            inAppData = triggerData.getIan();

            generateContainer();
        } catch (Exception e) {
            sentryHelper.captureException(e);
            finish();
        }

    }

    /**
     * To make the Glassmorphosis effect working, we need to capture the {@link Window} from last active/visible {@link Activity}.
     *
     * @param activity The current opened/visible activity.
     */
    public static void captureWindowForBlurryEffect(@NonNull Activity activity) {
        // Exclude activities from this plugin or which includes PreventBlurActivity
        if (activity instanceof PreventBlurActivity) {
            return;
        }

        lastActiveWindow = activity.getWindow();
    }

    public void setViewGroupForBlurry(ViewGroup viewGroup) {
        this.viewGroupForBlurry = viewGroup;
    }

    private void generateContainer() {
        Container container = inAppData.getContainer();
        ViewGroup.LayoutParams layoutParams = UIUtil.generateLayoutParams(displayMetrics,
                container.getSize());

        ImageView backgroundImage = bitmapForBlurry == null ?
                UIUtil.processBackground(this, container, viewGroupForBlurry) :
                UIUtil.processBackground(this, container, bitmapForBlurry);


    }


}
