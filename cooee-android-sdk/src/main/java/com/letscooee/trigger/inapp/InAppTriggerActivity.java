package com.letscooee.trigger.inapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AppCompatActivity;
import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.Animation;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.permission.PermissionManager;
import com.letscooee.trigger.inapp.renderer.InAppTriggerRenderer;
import com.letscooee.utils.Constants;
import com.letscooee.utils.RuntimeData;
import com.letscooee.utils.SentryHelper;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import jp.wasabeef.blurry.Blurry;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class InAppTriggerActivity extends AppCompatActivity implements PreventBlurActivity {

    private TriggerData triggerData;
    private InAppTrigger inAppData;

    private final RuntimeData runtimeData;
    private final SentryHelper sentryHelper;
    private final TriggerContext triggerContext = new TriggerContext();

    private Date startTime;
    private boolean isFreshLaunch;
    private boolean isSuccessfullyStarted;
    private boolean makeInAppFullScreen;

    public InAppTriggerActivity() {
        sentryHelper = CooeeFactory.getSentryHelper();
        runtimeData = CooeeFactory.getRuntimeData();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.in_app_trigger_activity);

        this.isFreshLaunch = savedInstanceState == null;

        try {
            triggerData = getIntent()
                    .getBundleExtra("bundle")
                    .getParcelable(Constants.INTENT_TRIGGER_DATA_KEY);
            makeInAppFullScreen = getIntent()
                    .getBundleExtra("bundle")
                    .getBoolean(Constants.IN_APP_FULLSCREEN_FLAG_KEY);

            if (makeInAppFullScreen) {
                setFullscreen();
            }

            if (triggerData == null || TextUtils.isEmpty(triggerData.getId())) {
                throw new Exception("Couldn't render In-App because trigger data is null");
            }

            inAppData = triggerData.getInAppTrigger();
            this.triggerContext.setViewGroupForBlurry(this.getDecorView());
            this.triggerContext.onExit(data -> this.finishAfterTransition());
            this.triggerContext.setTriggerData(triggerData);
            this.triggerContext.setMakeInAppFullScreen(makeInAppFullScreen);
            //setRequestedOrientation(inAppData.getInAppOrientation());

            setAnimations();
            renderInApp();
            sendTriggerDisplayedEvent();
        } catch (Exception e) {
            sentryHelper.captureException(e);
            finish();
        }
    }

    /**
     * Sets the activity to fullscreen
     */
    private void setFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            //noinspection deprecation
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTime = new Date();
        isSuccessfullyStarted = true;
    }

    public void sendTriggerDisplayedEvent() {
        if (!this.isFreshLaunch) {
            return;
        }

        Event event = new Event(Constants.EVENT_TRIGGER_DISPLAYED, triggerData);
        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }

    private void setAnimations() {
        Animation animation = inAppData.getAnimation();
        int enterAnimation = InAppAnimationProvider.getEnterAnimation(animation);
        int exitAnimation = InAppAnimationProvider.getExitAnimation(animation);

        overridePendingTransition(enterAnimation, exitAnimation);
    }

    private void renderInApp() {
        RelativeLayout rootViewElement = findViewById(R.id.inAppTriggerRoot);
        triggerContext.setTriggerParentLayout(rootViewElement);

        new InAppTriggerRenderer(this, rootViewElement, inAppData, inAppData, triggerContext).render();
    }

    /**
     * To make the Glassmorphosis effect working, we need to capture the {@link Window} from last active/visible {@link Activity}.
     * This method returns the {@link ViewGroup} from the last activity.
     */
    public ViewGroup getDecorView() {
        Activity activity = this.runtimeData.getCurrentActivity();
        // Exclude activities from this plugin or which includes PreventBlurActivity
        if (activity == null || activity instanceof PreventBlurActivity) {
            return null;
        }

        return (ViewGroup) activity.getWindow().getDecorView();
    }

    @Override
    public void onBackPressed() {
        this.triggerContext.closeInApp("Back Press");
    }

    @Override
    public void finishAfterTransition() {
        if (isSuccessfullyStarted) {
            //getWindow().setExitTransition(Transition.);
        }
        super.finishAfterTransition();
    }

    /**
     * +
     * Send trigger KPIs to the next activity(FeedbackActivity) to be sent back to the server
     */
    @Override
    public void finish() {
        Log.d(Constants.TAG, "finish 191: "+new Date().getTime());
        super.finish();
        if (!isSuccessfullyStarted) {
            return;
        }

        setAnimations();

        Map<String, Object> closedEventProps = triggerContext.getClosedEventProps();

        int duration = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
        closedEventProps.put("duration", duration);

        Event event = new Event(Constants.EVENT_TRIGGER_CLOSED, closedEventProps);
        event.withTrigger(triggerData);
        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }

    /**
     * Set Bitmap which can be used by {@link Blurry}. Mostly used by Flutter plugin.
     *
     * @param bitmap {@link Bitmap}
     */
    public void setBitmapForBlurry(Bitmap bitmap) {
        this.triggerContext.setBitmapForBlurry(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionManager permissionManager = new PermissionManager(InAppTriggerActivity.this);

        Map<String, Object> deviceProp = new HashMap<>();
        deviceProp.put("props", permissionManager.getPermissionInformation());

        CooeeFactory.getSafeHTTPService().updateDeviceProperty(deviceProp);
        triggerContext.closeInApp("CTA");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*
         * As we added orientation configuration (in AndroidManifest.xml) changes should be listened in the activity.
         * As soon as orientation changes, this method will be called. and we need to update the device resource
         */
        Log.d(Constants.TAG, "onConfigurationChanged 224: " + newConfig.orientation);
        CooeeFactory.getDeviceInfo().initializeResource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Constants.TAG, "onPause 260: ");
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(Constants.TAG, "onDestroy 266: "+new Date().getTime());
        runtimeData.getCurrentActivity().setRequestedOrientation(runtimeData.getCurrentActivityOrientation());
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
    }
}
