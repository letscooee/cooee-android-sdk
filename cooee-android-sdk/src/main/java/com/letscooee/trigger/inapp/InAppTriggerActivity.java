package com.letscooee.trigger.inapp;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.init.DefaultUserPropertiesCollector;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.Animation;
import com.letscooee.models.trigger.inapp.Container;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.trigger.inapp.renderer.ContainerRenderer;
import com.letscooee.utils.Constants;
import com.letscooee.utils.PermissionType;
import com.letscooee.utils.SentryHelper;

import jp.wasabeef.blurry.Blurry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class InAppTriggerActivity extends AppCompatActivity implements PreventBlurActivity {

    private static Window lastActiveWindow;

    private TriggerData triggerData;
    private InAppTrigger inAppData;

    private final SentryHelper sentryHelper;
    private final TriggerContext triggerContext = new TriggerContext();

    private Date startTime;
    private boolean isFreshLaunch;
    private boolean isSuccessfullyStarted;

    public InAppTriggerActivity() {
        sentryHelper = CooeeFactory.getSentryHelper();
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

            if (triggerData == null || triggerData.getInAppTrigger() == null) {
                throw new Exception("Couldn't render In-App because trigger data is null");
            }

            inAppData = triggerData.getInAppTrigger();
            this.triggerContext.setViewGroupForBlurry((ViewGroup) lastActiveWindow.getDecorView());
            this.triggerContext.onExit(data -> this.finish());
            this.triggerContext.setTriggerData(triggerData);

            setAnimations();
            renderContainerAndLayers();
            sendTriggerDisplayedEvent();
        } catch (Exception e) {
            sentryHelper.captureException(e);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTime = new Date();
        isSuccessfullyStarted = true;
    }

    private void sendTriggerDisplayedEvent() {
        if (!this.isFreshLaunch) {
            return;
        }

        Event event = new Event("CE Trigger Displayed", triggerData);
        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }

    private void setAnimations() {
        Animation animation = inAppData.getContainer().getAnimation();
        int enterAnimation = InAppAnimationProvider.getEnterAnimation(animation);
        int exitAnimation = InAppAnimationProvider.getExitAnimation(animation);

        overridePendingTransition(enterAnimation, exitAnimation);
    }

    private void renderContainerAndLayers() {
        Container containerData = inAppData.getContainer();
        RelativeLayout rootViewElement = findViewById(R.id.inAppTriggerRoot);
        triggerContext.setTriggerParentLayout(rootViewElement);
        new ContainerRenderer(this, rootViewElement, containerData, inAppData.getLayers(), triggerContext).render();
    }

    /**
     * To make the Glassmorphosis effect working, we need to capture the {@link Window} from last active/visible {@link Activity}.
     *
     * @param activity The current opened/visible activity.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void captureWindowForBlurryEffect(@NonNull Activity activity) {
        // Exclude activities from this plugin or which includes PreventBlurActivity
        if (activity instanceof PreventBlurActivity) {
            return;
        }

        lastActiveWindow = activity.getWindow();
    }

    @Override
    public void onBackPressed() {
        this.triggerContext.closeInApp("Back Press");
    }

    /**
     * +
     * Send trigger KPIs to the next activity(FeedbackActivity) to be sent back to the server
     */
    @Override
    public void finish() {
        super.finish();
        if (!isSuccessfullyStarted) {
            return;
        }

        Map<String, Object> closedEventProps = triggerContext.getClosedEventProps();

        int duration = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
        closedEventProps.put("Duration", duration);

        Event event = new Event("CE Trigger Closed", closedEventProps);
        event.withTrigger(triggerData);
        CooeeFactory.getSafeHTTPService().sendEvent(event);
    }

    /**
     * Set Bitmap which can be used by {@link Blurry}. Mostly used by Flutter plugin.
     *
     * @param bitmap
     */
    public void setBitmapForBlurry(Bitmap bitmap) {
        this.triggerContext.setBitmapForBlurry(bitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        DefaultUserPropertiesCollector devicePropsCollector = new DefaultUserPropertiesCollector(this);
        Map<String, String> permissionMap = new HashMap<>();
        Map<String, Object> deviceProperties = new HashMap<>();

        for (String permission : permissions) {

            PermissionType permissionType = PermissionType.getByValue(permission);
            boolean permissionStatus = ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED;

            permissionMap.put(permissionType.name(), permissionStatus ? "GRANTED" : "DENIED");

            if (permissionType == PermissionType.LOCATION && permissionStatus) {
                double[] location = devicePropsCollector.getLocation();
                deviceProperties.put("coordinates", location);

            } else if (permissionType == PermissionType.PHONE_DETAILS && permissionStatus) {
                String[] networkData = devicePropsCollector.getNetworkData();
                deviceProperties.put("CE Network Operator", networkData[0]);
                deviceProperties.put("CE Network Type", networkData[1]);
            }
        }
        deviceProperties.put("perm", permissionMap);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userProperties", deviceProperties);
        userMap.put("userData", new HashMap<>());

        CooeeFactory.getSafeHTTPService().updateUserProfile(userMap);
        triggerContext.closeInApp("CTA");
    }
}
