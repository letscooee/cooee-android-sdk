package com.letscooee.trigger.inapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
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
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.R;
import com.letscooee.models.v3.CoreTriggerData;
import com.letscooee.models.v3.elemeent.Children;
import com.letscooee.models.v3.elemeent.property.Alignment;
import com.letscooee.models.v3.inapp.Container;
import com.letscooee.models.v3.inapp.InAppData;
import com.letscooee.models.v3.inapp.Layers;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.utils.Constants;
import com.letscooee.utils.SentryHelper;
import com.letscooee.utils.Timer;
import com.letscooee.utils.UIUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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
        } catch (Exception e) {
            sentryHelper.captureException(e);
            finish();
        }

    }

    private void loadLayers() {
        if (inAppData.getLayers() != null) {
            for (Layers layers : inAppData.getLayers()) {
                RelativeLayout layout = new RelativeLayout(this);
                ViewGroup.LayoutParams layoutParams = uiUtil.generateLayoutParams(layers.getSize(), layers.getPosition());
                layout.setLayoutParams(layoutParams);
                uiUtil.processSpacing(layout, layers.getSpacing());
                Drawable backgroundImage = bitmapForBlurry == null ?
                        uiUtil.processBackground(layers, viewGroupForBlurry) :
                        uiUtil.processBackground(layers, bitmapForBlurry);
                layout.setBackground(backgroundImage);

                addChildren(layers.getElements(), layout);
                viewInAppTriggerRoot.addView(layout);
            }
        }
    }

    private void addChildren(ArrayList<Children> elements, View layout) {
        for (Children children : elements) {


            View view = null;
            if (children.getType() == Children.ElementType.IMAGE) {
                view = new ImageView(this);

                Glide.with(this).load(children.getUrl()).into((ImageView) view);

            } else if (children.getType() == Children.ElementType.BUTTON) {
                view = new Button(this);

                ((Button) view).setText(children.getText());
                ((Button) view).setTextColor(children.getColor().getSolidColor());
                if (children.getFont() != null) {
                    float fontSizeInSP = children.getFont().getSizeFloat() / getResources()
                            .getDisplayMetrics().scaledDensity;
                    ((Button) view).setTextSize(fontSizeInSP);
                }
                ((Button) view).setGravity(
                        children.getAlignment().getAlign() == Alignment.Align.LEFT ?
                                Gravity.START : Gravity.END);

            } else if (children.getType() == Children.ElementType.TEXT) {
                view = new TextView(this);

                ((TextView) view).setText(children.getText());
                if (children.getColor() != null)
                    ((TextView) view).setTextColor(children.getColor().getSolidColor());
                if (children.getFont() != null) {
                    float fontSizeInSP = children.getFont().getSizeFloat() / getResources()
                            .getDisplayMetrics().scaledDensity;
                    ((TextView) view).setTextSize(fontSizeInSP);
                }
                if (children.getAlignment() != null)
                    ((TextView) view).setGravity(
                            children.getAlignment().getAlign() == Alignment.Align.LEFT ?
                                    Gravity.START : Gravity.END);

            } else if (children.getType() == Children.ElementType.VIDEO) {
                view = new RelativeLayout(this);
                createVideoView(children, view);


            } else if (children.getType() == Children.ElementType.GROUP) {

                view = new FlexboxLayout(this);
                ((FlexboxLayout) view).setFlexDirection(FlexDirection.COLUMN);
                addChildren(children.getChildren(), view);


            }
            assert view != null;
            if (children.getTransform() != null)
                view.setRotation(children.getTransform().getRotate());
            RelativeLayout.LayoutParams layoutParams = uiUtil.generateLayoutParams(children.getSize(), children.getPosition());

            Drawable backgroundDrawable = bitmapForBlurry == null ?
                    uiUtil.processBackground(children, viewGroupForBlurry) :
                    uiUtil.processBackground(children, bitmapForBlurry);
            view.setLayoutParams(layoutParams);
            view.setBackground(backgroundDrawable);
            if (backgroundDrawable == null) {
                uiUtil.setOnImageLoad(view::setBackground);
            }
            uiUtil.processSpacing(view, children.getSpacing());
            if (layout instanceof RelativeLayout)

                ((RelativeLayout) layout).addView(view);
            else
                ((FlexboxLayout) layout).addView(view);
        }
    }

    private void createVideoView(Children children, View view) {

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        VideoView videoView = new VideoView(this);

        videoView.setLayoutParams(layoutParams);
        videoView.setVideoPath(children.getUrl());
        videoView.seekTo(30);
        videoView.start();


        ImageView playPauseImage = new ImageView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        RelativeLayout.LayoutParams layoutParams = uiUtil.generateLayoutParams(container.getSize(),
                container.getPosition());

        Drawable backgroundImage = bitmapForBlurry == null ?
                uiUtil.processBackground(container, viewGroupForBlurry) :
                uiUtil.processBackground(container, bitmapForBlurry);

        uiUtil.processSpacing(viewInAppTriggerRoot, container.getSpacing());
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(backgroundImage);
        viewInAppTriggerRoot.addView(imageView);

    }


}
