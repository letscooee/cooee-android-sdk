package com.letscooee.trigger.inapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AppCompatActivity;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.R;
import com.letscooee.models.Event;
import com.letscooee.models.trigger.TriggerData;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.models.trigger.inapp.Container;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.models.trigger.inapp.Layer;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.trigger.inapp.renderer.ContainerRenderer;
import com.letscooee.trigger.inapp.renderer.LayerRenderer;
import com.letscooee.utils.Constants;
import com.letscooee.utils.SentryHelper;
import jp.wasabeef.blurry.Blurry;

import java.lang.ref.WeakReference;
import java.util.*;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class InAppTriggerActivity extends AppCompatActivity implements PreventBlurActivity {

    private static Window lastActiveWindow;

    private boolean isFreshLaunch;
    private RelativeLayout rootViewElement;

    private TriggerData triggerData;
    private InAppTrigger inAppData;
    private Bitmap bitmapForBlurry;
    private ViewGroup viewGroupForBlurry;

    private final SafeHTTPService safeHTTPService;
    private final SentryHelper sentryHelper;
    private final InAppGlobalData globalData = new InAppGlobalData();

    private WeakReference<InAppTriggerActivity.InAppListener> inAppListenerWeakReference;
    private String closeBehaviour;
    private Date startTime;
    private boolean isSuccessfullyStarted;
    private ViewGroup containerView;

    public InAppTriggerActivity() {
        safeHTTPService = CooeeFactory.getSafeHTTPService();
        sentryHelper = CooeeFactory.getSentryHelper();
    }

    public interface InAppListener {
        void inAppNotificationDidClick(HashMap<String, Object> payload);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.in_app_trigger_activity);
        this.isFreshLaunch = savedInstanceState == null;

        rootViewElement = findViewById(R.id.inAppTriggerRoot);
        inAppListenerWeakReference = new WeakReference<>(CooeeSDK.getDefaultInstance(this));

        try {
            triggerData = getIntent()
                    .getBundleExtra("bundle")
                    .getParcelable(Constants.INTENT_TRIGGER_DATA_KEY);

            if (triggerData == null || triggerData.getInAppTrigger() == null) {
                throw new Exception("Couldn't render In-App because trigger data is null");
            }

            inAppData = triggerData.getInAppTrigger();
            this.setViewGroupForBlurry((ViewGroup) lastActiveWindow.getDecorView());
            renderContainer();
            renderLayers();
            updateEntrance();
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
        safeHTTPService.sendEvent(event);
    }

    /**
     * Update the trigger entrance
     */
    private void updateEntrance() {
        int transitionId = R.anim.slide_in_right;
        if (inAppData.getContainer().getAnimation() != null) {
            switch (inAppData.getContainer().getAnimation().getEnter()) {
                case SLIDE_IN_LEFT: {
                    transitionId = android.R.anim.slide_in_left;
                    break;
                }
                case SLIDE_IN_RIGHT: {
                    transitionId = R.anim.slide_in_right;
                    break;
                }
                case SLIDE_IN_TOP: {
                    transitionId = R.anim.slide_in_up;
                    break;
                }
                case SLIDE_IN_DOWN: {
                    transitionId = R.anim.slide_in_down;
                    break;
                }
                default: {
                    transitionId = R.anim.slide_in_right;
                    break;
                }
            }
        }
        overridePendingTransition(transitionId, R.anim.no_change);
    }

    private void updateExit() {
        int transitionId = R.anim.slide_out_right;
        if (inAppData.getContainer().getAnimation() != null)
            switch (inAppData.getContainer().getAnimation().getExit()) {
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

    private void manualCloseTrigger(String behaviour, ClickAction action) {
        this.closeBehaviour = behaviour;
        this.finish();

        if (action != null) {
            // Invoke the CTA only after this activity is finished
            nonCloseActionTaken(action);
        }
    }

    /**
     * Action/data to be sent to application i.e. the callback of CTA.
     *
     * @param action instance of {@link ClickAction}
     */
    private void nonCloseActionTaken(ClickAction action) {
        InAppTriggerActivity.InAppListener listener = inAppListenerWeakReference.get();
        if (action.getKv() != null) {
            listener.inAppNotificationDidClick((HashMap<String, Object>) action.getKv());
        }

        if (action.getUp() != null) {
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("userData", new HashMap<>());
            userProfile.put("userProperties", action.getUp());
            CooeeFactory.getSafeHTTPService().updateUserProfile(userProfile);
        }
    }

    private void renderLayers() {
        if (inAppData.getLayers() == null) {
            return;
        }

        for (Layer layer : inAppData.getLayers()) {
            ViewGroup layout = (ViewGroup) new LayerRenderer(this, containerView, layer, globalData).render();
            layout.setClipToOutline(true);
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

    /**
     * Will read {@link Container} data and apply effect accordingly
     */
    private void renderContainer() {
        Container container = inAppData.getContainer();
        containerView = (ViewGroup) new ContainerRenderer(this, rootViewElement, container).render();
    }

    private void addAction(View view, ClickAction action) {
        if (action == null) {
            return;
        }

        view.setOnClickListener(v -> {
            if (action.getExternal() != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(action.getExternal().getUrl()));
                startActivity(browserIntent);
            } else if (action.getIab() != null) {

                if (!TextUtils.isEmpty(action.getIab().getUrl())) {
                    Intent intent = new Intent(this, InAppBrowserActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.INTENT_BUNDLE_KEY, action.getIab());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            } else if (action.getUpdateApp() != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(action.getUpdateApp().getUrl()));
                startActivity(browserIntent);
            } else if (action.getShare() != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String shareBody = Objects.requireNonNull(action.getShare().get("text")).toString();
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(intent, "Share with"));
            } else if (action.getPrompts() != null && action.getPrompts().length != 0) {
                List<String> permissionList = new ArrayList<>();
                for (String permission : action.getPrompts()) {
                    if (permission.equalsIgnoreCase("location")) {
                        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
                        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);

                    } else if (permission.equalsIgnoreCase("camera")) {
                        permissionList.add(Manifest.permission.CAMERA);
                    } else if (permission.equalsIgnoreCase("phone_state")) {
                        permissionList.add(Manifest.permission.READ_PHONE_STATE);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissionList.toArray(new String[0]), Constants.REQUEST_LOCATION);
                }
            }
            if (action.isClose()) {
                manualCloseTrigger("Action Press", action);
            }
        });
    }

    @Override
    public void onBackPressed() {
        manualCloseTrigger("Back Press", null);
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

        Map<String, Object> closedEventProps = globalData.getClosedEventProps();
        int duration = (int) ((new Date().getTime() - startTime.getTime()) / 1000);

        closedEventProps.put("Duration", duration);
        closedEventProps.put("Close Behaviour", closeBehaviour);

        Event event = new Event("CE Trigger Closed", closedEventProps);
        event.withTrigger(triggerData);
        safeHTTPService.sendEvent(event);

        updateExit();
    }

    /**
     * Set Bitmap which can be used by {@link Blurry}. Mostly used by Flutter plugin.
     *
     * @param bitmap
     */
    public void setBitmapForBlurry(Bitmap bitmap) {
        this.bitmapForBlurry = bitmap;
    }
}
