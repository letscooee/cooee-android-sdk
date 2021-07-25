package com.letscooee.trigger.inapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.card.MaterialCardView;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.R;
import com.letscooee.models.Event;
import com.letscooee.models.v3.CoreTriggerData;
import com.letscooee.models.v3.block.ClickAction;
import com.letscooee.models.v3.block.Position;
import com.letscooee.models.v3.block.Size;
import com.letscooee.models.v3.element.*;
import com.letscooee.models.v3.inapp.Container;
import com.letscooee.models.v3.inapp.InAppData;
import com.letscooee.models.v3.inapp.Layer;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.utils.Constants;
import com.letscooee.utils.SentryHelper;
import com.letscooee.utils.Timer;
import com.letscooee.utils.UIUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.blurry.Blurry;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class InAppTriggerActivityNew extends AppCompatActivity implements PreventBlurActivity {

    private static Window lastActiveWindow;

    private boolean isFreshLaunch;
    private RelativeLayout viewInAppTriggerRoot;

    private CoreTriggerData triggerData;
    private InAppData inAppData;
    private Bitmap bitmapForBlurry;
    private ViewGroup viewGroupForBlurry;
    private UIUtil uiUtil;

    private final SafeHTTPService safeHTTPService;
    private final SentryHelper sentryHelper;
    private WeakReference<InAppTriggerActivity.InAppListener> inAppListenerWeakReference;
    private int videoDuration;
    private int watchedTill;
    private int videoSeenCounter = 0;
    private boolean isVideoUnmuted;
    private String closeBehaviour;
    private Date startTime;
    private boolean isSuccessfullyStarted;
    private View baseView;

    public InAppTriggerActivityNew() {
        safeHTTPService = CooeeFactory.getSafeHTTPService();
        sentryHelper = CooeeFactory.getSentryHelper();
    }

    public interface InAppListener {
        void inAppNotificationDidClick(HashMap<String, Object> payload);
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.isFreshLaunch = savedInstanceState == null;
        setContentView(R.layout.activity_engagement_trigger_new);
        viewInAppTriggerRoot = findViewById(R.id.inAppTriggerRoot);
        inAppListenerWeakReference = new WeakReference<>(CooeeSDK.getDefaultInstance(this));
        uiUtil = new UIUtil(this);
        try {
            triggerData = getIntent()
                    .getBundleExtra("bundle")
                    .getParcelable(Constants.INTENT_TRIGGER_DATA_KEY);

            if (triggerData == null || triggerData.getIan() == null) {
                throw new Exception("Couldn't render In-App because trigger data is null");
            }

            inAppData = triggerData.getIan();
            this.setViewGroupForBlurry((ViewGroup) lastActiveWindow.getDecorView());
            generateContainer();
            loadLayers();
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

    private void loadLayers() {
        if (inAppData.getLayers() != null) {
            for (Layer layer : inAppData.getLayers()) {
                RelativeLayout layout = new RelativeLayout(this);
                ViewGroup.LayoutParams layoutParams = uiUtil.generateLayoutParams(layer.getSize(), layer.getPosition());

                layout.setLayoutParams(layoutParams);
                uiUtil.processSpacing(layout, layer.getSpacing());

                MaterialCardView backgroundImage = bitmapForBlurry == null ?
                        uiUtil.processBackground(layer, viewGroupForBlurry) :
                        uiUtil.processBackground(layer, bitmapForBlurry);

                if (backgroundImage != null) {
                    backgroundImage.setLayoutParams(layoutParams);
                    uiUtil.processSpacing(backgroundImage, layer.getSpacing());
                }

                if (layer.getAction() != null) {
                    ClickAction action = layer.getAction();
                    addAction(layout, action);
                }
                if (baseView instanceof FlexboxLayout) {
                    if (backgroundImage != null) {
                        ((RelativeLayout) backgroundImage.getChildAt(0)).addView(layout);
                        ((FlexboxLayout) baseView).addView(backgroundImage);
                    } else
                        ((FlexboxLayout) baseView).addView(layout);
                } else {
                    if (backgroundImage != null) {
                        ((RelativeLayout) backgroundImage.getChildAt(0)).addView(layout);
                        ((RelativeLayout) baseView).addView(backgroundImage);
                    } else
                        ((RelativeLayout) baseView).addView(layout);
                }

                if (backgroundImage != null) {
                    if (layer.getPosition() != null)
                        if (layer.getPosition().getType() == Position.PositionType.ABSOLUTE) {
                            addObserver(layer.getPosition(), backgroundImage, baseView,
                                    inAppData.getContainer());
                        } else if (layer.getPosition().getType() == Position.PositionType.FIXED) {
                            addObserver(layer.getPosition(), backgroundImage, viewInAppTriggerRoot,
                                    inAppData.getContainer());
                        }
                } else {
                    if (layer.getPosition() != null)
                        if (layer.getPosition().getType() == Position.PositionType.ABSOLUTE) {
                            addObserver(layer.getPosition(), layout,
                                    baseView, inAppData.getContainer());
                        } else if (layer.getPosition().getType() == Position.PositionType.FIXED) {
                            addObserver(layer.getPosition(), layout,
                                    viewInAppTriggerRoot, inAppData.getContainer());
                        }
                }
                addChildren(layer.getChildren(), layout, layer);

                layout.setClipToOutline(true);
            }
        }
    }

    /**
     * loops arraylist of {@link TextElement} to generate view accordingly and apply then properties.
     *
     * @param children {@link ArrayList} of {@link TextElement}
     * @param layout   instance of parent {@link View} in which all element to be added
     * @param layers   it can be {@link Container}, {@link Layer}, {@link TextElement} came
     *                 for instance of layout
     */
    private void addChildren(ArrayList<BaseElement> children, View layout, Object layers) {
        for (BaseElement child : children) {
            View view = null;
            if (child instanceof ImageElement) {
                view = new ImageView(this);
                ((ImageView) view).setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                Glide.with(this).load(((ImageElement) child).getUrl()).into((ImageView) view);

            } else if (child instanceof ButtonElement) {
                view = new Button(this);
                ButtonElement element = (ButtonElement) child;

                ((Button) view).setText(element.getText());
                ((Button) view).setTextColor(element.getColor().getSolidColor());
                if (element.getFont() != null) {
                    float fontSizeInSP = element.getFont().getSizeFloat() / getResources()
                            .getDisplayMetrics().scaledDensity;
                    ((Button) view).setTextSize(fontSizeInSP);
                }
                if (element.getAlignment() != null)
                    if (element.getAlignment().getAlign().equalsIgnoreCase("left"))
                        ((TextView) view).setGravity(Gravity.START);
                    else if (element.getAlignment().getAlign().equalsIgnoreCase("right"))
                        ((TextView) view).setGravity(Gravity.END);

            } else if (child instanceof TextElement) {
                view = new TextView(this);
                TextElement element = (TextElement) child;

                ((TextView) view).setText(element.getText());
                if (element.getColor() != null)
                    ((TextView) view).setTextColor(element.getColor().getSolidColor());
                if (element.getFont() != null) {
                    float fontSizeInSP = element.getFont().getSizeFloat() / getResources()
                            .getDisplayMetrics().scaledDensity;
                    ((TextView) view).setTextSize(element.getFont().getSizeFloat());
                }
                if (element.getAlignment() != null)
                    if (element.getAlignment().getAlign().equalsIgnoreCase("center"))
                        ((TextView) view).setGravity(Gravity.CENTER);
                    else if (element.getAlignment().getAlign().equalsIgnoreCase("right"))
                        ((TextView) view).setGravity(Gravity.END);

            } else if (child instanceof VideoElement) {
                view = new RelativeLayout(this);
                VideoElement element = (VideoElement) child;
                ((RelativeLayout) view).setGravity(Gravity.CENTER);
                createVideoView(element, view);

            } else if (child instanceof GroupElement) {
                view = new FlexboxLayout(this);
                GroupElement element = (GroupElement) child;
                ((FlexboxLayout) view).setFlexDirection(FlexDirection.COLUMN);
                addChildren(element.getChildren(), view, child);
            }

            assert view != null;
            if (child.getTransform() != null)
                view.setRotation(child.getTransform().getRotate());

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) uiUtil.generateLayoutParams(child.getSize(), child.getPosition());

            MaterialCardView backgroundImage = bitmapForBlurry == null ?
                    uiUtil.processBackground(child, viewGroupForBlurry) :
                    uiUtil.processBackground(child, bitmapForBlurry);

            view.setLayoutParams(layoutParams);
            uiUtil.processSpacing(view, child.getSpacing());

            if (child.getAction() != null) {
                ClickAction action = child.getAction();
                addAction(view, action);
            }

            if (backgroundImage != null) {
                backgroundImage.setLayoutParams(layoutParams);
                uiUtil.processSpacing(backgroundImage, child.getSpacing());
                if (layout instanceof RelativeLayout) {
                    ((RelativeLayout) backgroundImage.getChildAt(0)).addView(view);
                    ((RelativeLayout) layout).addView(backgroundImage);
                } else if (layout instanceof FrameLayout) {
                    ((FrameLayout) layout).addView(backgroundImage);
                } else {
                    ((FlexboxLayout) layout).addView(backgroundImage);
                }
            } else {
                if (layout instanceof RelativeLayout) {
                    ((RelativeLayout) layout).addView(view);
                } else if (layout instanceof FrameLayout) {
                    ((FrameLayout) layout).addView(view);
                } else {
                    ((FlexboxLayout) layout).addView(view);
                }
            }

            if (backgroundImage != null) {
                if (child.getPosition() != null)
                    if (child.getPosition().getType() == Position.PositionType.ABSOLUTE) {
                        addObserver(child.getPosition(), backgroundImage, layout, layers);
                    } else if (child.getPosition().getType() == Position.PositionType.FIXED) {
                        addObserver(child.getPosition(), backgroundImage, viewInAppTriggerRoot,
                                inAppData.getContainer());
                    }

            } else {
                if (child.getPosition() != null)
                    if (child.getPosition().getType() == Position.PositionType.ABSOLUTE) {
                        addObserver(child.getPosition(), view, layout, layers);
                    } else if (child.getPosition().getType() == Position.PositionType.FIXED) {
                        addObserver(child.getPosition(), view, viewInAppTriggerRoot,
                                inAppData.getContainer());
                    }
            }

        }
    }

    /**
     * Create a view to render video on In-App
     *
     * @param element instance of {@link TextElement} which holds all properties of view
     * @param view     instance of {@link View} in which newly generated view to add
     */
    private void createVideoView(VideoElement element, View view) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        VideoView videoView = new VideoView(this);

        videoView.setLayoutParams(layoutParams);
        videoView.setVideoPath(element.getUrl());
        videoView.seekTo(30);
        videoView.start();

        ImageView playPauseImage = new ImageView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        playPauseImage.setLayoutParams(params);
        playPauseImage.setVisibility(View.INVISIBLE);
        playPauseImage.setTranslationZ(10);

        ((RelativeLayout) view).addView(playPauseImage);

        RelativeLayout.LayoutParams muteButtonBackgroundParams = new RelativeLayout.LayoutParams(90, 90);
        muteButtonBackgroundParams.setMargins(20, 20, 0, 0);
        RelativeLayout muteButtonBackground = new RelativeLayout(this);
        muteButtonBackground.setLayoutParams(muteButtonBackgroundParams);
        muteButtonBackground.setElevation(5f);


        RelativeLayout.LayoutParams muteButtonParams = new RelativeLayout.LayoutParams(50, 50);
        muteButtonParams.setMargins(0, 0, 20, 20);

        Button muteUnmuteButton = new Button(this);
        muteUnmuteButton.setLayoutParams(muteButtonParams);
        muteUnmuteButton.setBackground(ContextCompat.getDrawable(this, R.drawable.mute_background));
        muteUnmuteButton.setOnClickListener(v -> {
            isVideoUnmuted = true;
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (am.isStreamMute(AudioManager.STREAM_MUSIC)) {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                    muteUnmuteButton.setBackground(ContextCompat.getDrawable(this, R.drawable.unmute_background));
                } else {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                    muteUnmuteButton.setBackground(ContextCompat.getDrawable(this, R.drawable.mute_background));
                }
            }
        });
        muteButtonBackground.setOnClickListener(v -> {
            isVideoUnmuted = true;
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (am.isStreamMute(AudioManager.STREAM_MUSIC)) {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                    muteUnmuteButton.setBackground(ContextCompat.getDrawable(this, R.drawable.unmute_background));
                } else {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                    muteUnmuteButton.setBackground(ContextCompat.getDrawable(this, R.drawable.mute_background));
                }
            }
        });
        muteButtonBackground.addView(muteUnmuteButton);
        ((RelativeLayout) view).addView(muteButtonBackground);

        videoView.setOnClickListener(v -> {
            playPauseImage.setVisibility(View.VISIBLE);
            if (videoView.isPlaying()) {
                videoView.pause();
                playPauseImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_play_arrow_24));
            } else {
                videoView.start();
                playPauseImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_24));
            }
            Timer handler = new Timer();
            handler.schedule(() -> playPauseImage.setVisibility(View.INVISIBLE), 2000);
        });

        videoView.setOnPreparedListener(mp -> {
            videoDuration = videoView.getDuration() / 1000;
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamMute(AudioManager.STREAM_MUSIC, true);
        });

        videoView.setOnCompletionListener(mp -> {
            playPauseImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_replay_24));
            playPauseImage.setVisibility(View.VISIBLE);
            videoSeenCounter++;
        });

        calculateCurrentPositionThread(videoView);

        ((RelativeLayout) view).addView(videoView);
    }

    /**
     * Calculate how much video is seen. Triggered only if VIDEO media is given by server
     *
     * @param videoView dynamic videoview
     */
    private void calculateCurrentPositionThread(VideoView videoView) {
        ScheduledExecutorService executorService;
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(() -> videoView.post(() ->
                watchedTill = videoView.getCurrentPosition() / 1000), 1000, 1000, TimeUnit.MILLISECONDS);

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
    private void generateContainer() {
        Container container = inAppData.getContainer();

        baseView = viewInAppTriggerRoot;

        if (container.getSize() != null) {
            if (container.getSize().getDisplay() == Size.Display.FLEX) {
                baseView = new FlexboxLayout(this);
            }
        }
        ViewGroup.MarginLayoutParams layoutParams = uiUtil.generateLayoutParams(container.getSize(),
                container.getPosition());


        MaterialCardView backgroundImage = bitmapForBlurry == null ?
                uiUtil.processBackground(container, viewGroupForBlurry) :
                uiUtil.processBackground(container, bitmapForBlurry);

        uiUtil.processSpacing(viewInAppTriggerRoot, container.getSpacing());

        if (backgroundImage != null) {
            backgroundImage.setLayoutParams(layoutParams);
        }

        viewInAppTriggerRoot.addView(backgroundImage);

        if (baseView instanceof FlexboxLayout) {
            baseView.setLayoutParams(layoutParams);
            uiUtil.addFlexProperty((FlexboxLayout) baseView, container.getSize());
        }

        if (backgroundImage != null)
            ((RelativeLayout) backgroundImage.getChildAt(0)).addView(baseView);
        else
            viewInAppTriggerRoot.addView(baseView);

        if (backgroundImage != null) {
            backgroundImage.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                if (container.getPosition() != null)
                    if (container.getPosition().getType() == Position.PositionType.ABSOLUTE ||
                            container.getPosition().getType() == Position.PositionType.FIXED)
                        addObserver(container.getPosition(), backgroundImage,
                                viewInAppTriggerRoot, null);
            });
        } else {
            if (container.getPosition() != null)
                if (container.getPosition().getType() == Position.PositionType.ABSOLUTE ||
                        container.getPosition().getType() == Position.PositionType.FIXED)
                    addObserver(container.getPosition(), baseView, viewInAppTriggerRoot, null);
        }
        if (container.getAction() != null) {
            ClickAction action = container.getAction();
            addAction(baseView, action);
        }


    }

    /**
     * Add observer to the view to check once view is rendered on a screen and then
     * apply position to that view
     *
     * @param position       {@link Position} came with the current view
     * @param currentView    {@link View} to which we want to assign observer and apply position
     * @param parentView     is {@link View} to whome current view going to check for position
     * @param parentProperty it can be {@link Container}, {@link Layer}, {@link TextElement} came
     *                       for instance of parentView
     */
    private void addObserver(Position position, View currentView, View parentView, Object parentProperty) {
        currentView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            uiUtil.processPosition(position, currentView,
                    parentView, parentProperty);
        });
    }

    private void addAction(View view, ClickAction action) {
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
                closeBehaviour = "Action Press";
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

        int duration = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
        int totalWatched = videoDuration * videoSeenCounter + watchedTill;

        Map<String, Object> kpiMap = new HashMap<>();
        kpiMap.put("Duration", duration);
        kpiMap.put("Close Behaviour", closeBehaviour);

        // TODO: 16/07/21 need to discuss these point for video parameters
        //if (triggerData.getType() == TriggerData.Type.VIDEO) {
        kpiMap.put("Video Duration", videoDuration);
        kpiMap.put("Watched Till", watchedTill);
        kpiMap.put("Total Watched", totalWatched);
        kpiMap.put("Video Unmuted", isVideoUnmuted);
        //}

        Event event = new Event("CE Trigger Closed", kpiMap);
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
