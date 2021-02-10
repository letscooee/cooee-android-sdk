package com.letscooee.trigger;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.flexbox.FlexboxLayout;
import com.letscooee.CooeeSDK;
import com.letscooee.R;
import com.letscooee.models.Event;
import com.letscooee.models.TriggerBackground;
import com.letscooee.models.TriggerButton;
import com.letscooee.models.TriggerButtonAction;
import com.letscooee.models.TriggerCloseBehaviour;
import com.letscooee.models.TriggerData;
import com.letscooee.models.TriggerText;
import com.letscooee.retrofit.HttpCallsHelper;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.blurry.Blurry;

public class EngagementTriggerActivity extends AppCompatActivity {

    TriggerData triggerData;
    ImageButton closeImageButton;
    RelativeLayout secondParentLayout;
    TextView textViewTimer;

    private static Window _window;

    private WeakReference<InAppListener> inAppListenerWeakReference;

    private Date startTime;
    private String closeBehaviour;
    private Handler handler;
    private Runnable runnable;
    private int videoDuration;
    private int watchedTill;
    private int videoSeenCounter = 0;
    private boolean isVideoUnmuted;

    public interface InAppListener {
        void inAppNotificationDidClick(HashMap<String, String> payload);
    }

    public static void setWindow(Window window) {
        _window = window;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engagement_trigger);

        closeImageButton = findViewById(R.id.buttonClose);
        closeImageButton.setOnClickListener(view -> {
            closeBehaviour = "Close Button";
            finish();
        });

        secondParentLayout = findViewById(R.id.secondParentRelative);
        textViewTimer = findViewById(R.id.textViewTimer);
        inAppListenerWeakReference = new WeakReference<>(CooeeSDK.getDefaultInstance(this));

        try {
            triggerData = (TriggerData) Objects.requireNonNull(getIntent().getBundleExtra("bundle")).getParcelable("triggerData");

            if (triggerData == null) {
                finish();
            }

            updateFill();
            addMediaView();
            closeButtonPosition();
            updateBackground();
            updateEntrance();
            updateClose();
            updateText();
            updateMessage();
            updateTextPosition();
            createActionButtons();
        } catch (Exception ignored) {
        }
    }

    /**
     * Create defined action buttons for the trigger
     */
    private void createActionButtons() {
        if (triggerData.getButtons()[0] != null || !triggerData.getButtons()[0].getText().isEmpty()) {
            for (TriggerButton triggerButton : triggerData.getButtons()) {
                createButton(triggerButton);
            }

        }
    }

    /**
     * Create and add action button
     *
     * @param triggerButton trigger button data
     */
    private void createButton(TriggerButton triggerButton) {
        FlexboxLayout flexboxLayout = findViewById(R.id.actionFlexLayout);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(15, 15, 15, 15);

        TextView button = new TextView(this);
        button.setLayoutParams(params);
        button.setText(triggerButton.getText());
        String color = triggerButton.getColor().isEmpty() ? "#0000FF" : triggerButton.getColor();
        button.setTextColor(Color.parseColor(color));
        button.setPadding(15, 15, 15, 15);
        button.setTypeface(Typeface.DEFAULT_BOLD);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(triggerButton.getRadius());
        drawable.setColor(Color.parseColor(triggerButton.getBackground()));
        button.setBackground(drawable);

        button.setOnClickListener(view -> {
            didClick(triggerButton.getAction());
            closeBehaviour = "Action Button";
            finish();
        });

        button.setOnTouchListener((v, event) -> {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                drawable.setCornerRadius(triggerButton.getRadius());
                drawable.setStroke(1, Color.BLACK);
                drawable.setColor(Color.GRAY);
                button.setBackground(drawable);
            } else if (MotionEvent.ACTION_UP == event.getAction()) {
                drawable.setCornerRadius(triggerButton.getRadius());
                drawable.setStroke(0, Color.BLACK);
                drawable.setColor(Color.parseColor(triggerButton.getBackground()));
                button.setBackground(drawable);
            }
            return false;
        });

        flexboxLayout.addView(button);
    }

    /**
     * Action/data to be sent to application will be defined here
     */
    private void didClick(TriggerButtonAction action) {
        InAppListener listener = inAppListenerWeakReference.get();
        if (action.getKv() != null) {
            listener.inAppNotificationDidClick(action.getKv());
        }

        if (action.getUserProperty() != null) {
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("userData", new HashMap<>());
            userProfile.put("userProperties", action.getUserProperty());
            HttpCallsHelper.sendUserProfile(userProfile, "Trigger Property", null);
        }
    }

    /**
     * Update text/media position as given by the server
     */
    private void updateTextPosition() {
        if (triggerData.getTitle().getPosition() == TriggerText.Position.BOTTOM || triggerData.getTitle().getPosition() == TriggerText.Position.RIGHT) {
            RelativeLayout textRelativeLayout = findViewById(R.id.textLayout);
            RelativeLayout mediaRelativeLayout = findViewById(R.id.mediaRelativeLayout);
            RelativeLayout actionRelativeLayout = findViewById(R.id.actionLayout);
            ((ViewGroup) textRelativeLayout.getParent()).removeView(textRelativeLayout);
            ((ViewGroup) mediaRelativeLayout.getParent()).removeView(mediaRelativeLayout);
            ((ViewGroup) actionRelativeLayout.getParent()).removeView(actionRelativeLayout);
            LinearLayout linearLayout = findViewById(R.id.contentLinearLayout);
            linearLayout.addView(mediaRelativeLayout);
            linearLayout.addView(textRelativeLayout);
            linearLayout.addView(actionRelativeLayout);
        }
    }

    /**
     * Update text/heading data
     */
    private void updateText() {
        TextView textView = findViewById(R.id.textViewTitle);
        textView.setText(triggerData.getTitle().getText());
        int color = triggerData.getTitle().getColor() == null || triggerData.getTitle().getColor().isEmpty()
                ? getResources().getColor(R.color.colorText)
                : Color.parseColor(triggerData.getTitle().getColor());
        textView.setTextColor(color);
        textView.setTextSize(triggerData.getTitle().getSize());
    }

    /**
     * Update message data
     */
    private void updateMessage() {
        TextView textView = findViewById(R.id.textViewContent);
        textView.setText(triggerData.getMessage().getText());
        int color = triggerData.getMessage().getColor() == null || triggerData.getMessage().getColor().isEmpty()
                ? getResources().getColor(R.color.colorText)
                : Color.parseColor(triggerData.getMessage().getColor());
        textView.setTextColor(color);
        textView.setTextSize(triggerData.getMessage().getSize());
    }

    /**
     * Update the close configuration
     * eg. if it should be auto close or close button is provided
     */
    private void updateClose() {
        if (triggerData.getCloseBehaviour().isAuto() || triggerData.getCloseBehaviour().getTimeToClose() == 0) {
            int autoClose = triggerData.getCloseBehaviour().getTimeToClose();
            closeImageButton.setVisibility(View.GONE);
            textViewTimer.setVisibility(View.GONE);
            handler = new Handler();
            runnable = () -> finish();
            handler.postDelayed(runnable, autoClose * 1000);

            closeBehaviour = "Auto";
        }
    }

    /**
     * Update the trigger entrance
     */
    private void updateEntrance() {
        int transitionId;
        switch (triggerData.getEntranceAnimation()) {
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
        overridePendingTransition(transitionId, R.anim.no_change);
    }

    /**
     * Update the background type
     * eg. SOLID_COLOR, IMAGE and BLURRED
     */
    private void updateBackground() {
        if (_window == null) {
            finish();
        }

        if (triggerData.getBackground().getType() == TriggerBackground.TriggerType.SOLID_COLOR) {
            String color = triggerData.getBackground().getColor() == null || triggerData.getBackground().getColor().isEmpty()
                    ? "#DDDDDD"
                    : triggerData.getBackground().getColor();

            int z;

            if (triggerData.getBackground().getOpacity() == 0) {
                z = 255 - (20 * 255 / 100);
            } else {
                z = 255 - (triggerData.getBackground().getOpacity() * 255 / 100);
            }

            String y = Integer.toHexString(z);

            if (y.length() == 1) {
                y = "0".concat(y);
            }

            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(triggerData.getBackground().getRadius());
            drawable.setStroke(1, 0xFFFFFF);
            drawable.setColor(Color.parseColor("#" + y + color.substring(1)));
            secondParentLayout.setBackground(drawable);
        } else if (triggerData.getBackground().getType() == TriggerBackground.TriggerType.IMAGE) {
            if (triggerData.getBackground().getImage() == null || triggerData.getBackground().getImage().isEmpty()) {
                secondParentLayout.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                return;
            }

            Glide.with(this)
                    .asBitmap()
                    .load(triggerData.getBackground().getImage())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                            drawable.setCornerRadius(20);
                            secondParentLayout.setBackground(drawable);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
    }

    /**
     * Update the type of trigger
     * eg. COVER, HALF_INTERSTITIAL and INTERSTITIAL
     */
    private void updateFill() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (triggerData.getFill() == TriggerData.Fill.COVER) {
            findViewById(R.id.contentLinearLayout).setLayoutParams(layoutParams);
            layoutParams.setMargins(0, 0, 0, 0);
        } else if (triggerData.getFill() == TriggerData.Fill.INTERSTITIAL) {
            layoutParams = new RelativeLayout.LayoutParams((int) (dm.widthPixels * 0.9), (int) (dm.heightPixels * 0.9));
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        } else if (triggerData.getFill() == TriggerData.Fill.HALF_INTERSTITIAL) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layoutParams = new RelativeLayout.LayoutParams((int) (dm.widthPixels * 0.5), (int) (dm.heightPixels * 0.9));
            } else {
                layoutParams = new RelativeLayout.LayoutParams((int) (dm.widthPixels * 0.9), (int) (dm.heightPixels * 0.5));
            }

            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        }

        secondParentLayout.setLayoutParams(layoutParams);
    }

    /**
     * Create the media view given by server
     * eg. IMAGE, VIDEO (as of now)
     */
    private void addMediaView() {
        if (triggerData.getType() == TriggerData.Type.IMAGE) {
            if (triggerData.getImageUrl() == null || triggerData.getImageUrl().isEmpty()) {
                finish();
            }
            createImageView();
        } else if (triggerData.getType() == TriggerData.Type.VIDEO) {
            if (triggerData.getVideoUrl() == null || triggerData.getVideoUrl().isEmpty()) {
                finish();
            }
            createVideoView();
        }
    }

    private void createImageView() {
        RelativeLayout insideMediaFrameLayout = findViewById(R.id.insideMediaFrameLayout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView imageView = new ImageView(EngagementTriggerActivity.this);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(getApplicationContext()).load(triggerData.getImageUrl()).into(imageView);

        insideMediaFrameLayout.addView(imageView);
    }

    private void createVideoView() {
        RelativeLayout insideMediaFrameLayout = findViewById(R.id.insideMediaFrameLayout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        VideoView videoView = new VideoView(EngagementTriggerActivity.this);

        videoView.setLayoutParams(layoutParams);
        videoView.setVideoPath(triggerData.getVideoUrl());
        videoView.seekTo(30);
        videoView.start();

        ImageView playPauseImage = new ImageView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        playPauseImage.setLayoutParams(params);
        playPauseImage.setVisibility(View.INVISIBLE);
        playPauseImage.setTranslationZ(10);

        insideMediaFrameLayout.addView(playPauseImage);

        RelativeLayout.LayoutParams muteButtonParams = new RelativeLayout.LayoutParams(50, 50);
        muteButtonParams.setMargins(50, 50, 50, 50);

        Button muteUnmuteButton = new Button(this);
        muteUnmuteButton.setLayoutParams(muteButtonParams);
        muteUnmuteButton.setBackground(getDrawable(R.drawable.mute_background));
        muteUnmuteButton.setOnClickListener(v -> {
            isVideoUnmuted = true;
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (am.isStreamMute(AudioManager.STREAM_MUSIC)) {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                    muteUnmuteButton.setBackground(getDrawable(R.drawable.unmute_background));
                } else {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                    muteUnmuteButton.setBackground(getDrawable(R.drawable.mute_background));
                }
            }
        });

        insideMediaFrameLayout.addView(muteUnmuteButton);

        videoView.setOnClickListener(view -> {
            playPauseImage.setVisibility(View.VISIBLE);
            if (videoView.isPlaying()) {
                videoView.pause();
                playPauseImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
            } else {
                videoView.start();
                playPauseImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_pause_24));
            }
            handler.postDelayed(() -> playPauseImage.setVisibility(View.INVISIBLE), 2000);
        });

        videoView.setOnPreparedListener(mp -> {
            videoDuration = videoView.getDuration() / 1000;
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamMute(AudioManager.STREAM_MUSIC, true);
        });

        videoView.setOnCompletionListener(mp -> {
            playPauseImage.setImageDrawable(getDrawable(R.drawable.ic_baseline_replay_24));
            playPauseImage.setVisibility(View.VISIBLE);
            videoSeenCounter++;
        });

        calculateCurrentPositionThread(videoView);

        insideMediaFrameLayout.addView(videoView);
    }

    /**
     * Calculate how much video is seen. Triggered only if VIDEO media is given by server
     *
     * @param videoView dynamic videoview
     */
    private void calculateCurrentPositionThread(VideoView videoView) {
        ScheduledExecutorService executorService;
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(() -> videoView.post(() -> {
            watchedTill = videoView.getCurrentPosition() / 1000;
        }), 1000, 1000, TimeUnit.MILLISECONDS);

    }

    /**
     * Update the position of close button(if should be given)
     * eg. TOP_LEFT(default), TOP_RIGHT, DOWN_RIGHT and DOWN_LEFT
     */
    private void closeButtonPosition() {
        closeImageButton.setVisibility(View.INVISIBLE);
        closeImageButton.setEnabled(false);

        if (!triggerData.getCloseBehaviour().isAuto() || triggerData.getCloseBehaviour().getTimeToClose() == 0) {
            new CountDownTimer(5000, 1000) {

                public void onTick(long millisUntilFinished) {
                    textViewTimer.setText(String.valueOf((millisUntilFinished / 1000) + 1));
                }

                public void onFinish() {
                    textViewTimer.setVisibility(View.GONE);
                    closeImageButton.setVisibility(View.VISIBLE);
                    closeImageButton.setEnabled(true);
                }
            }.start();
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (triggerData.getCloseBehaviour().getPosition() == TriggerCloseBehaviour.Position.TOP_RIGHT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        } else if (triggerData.getCloseBehaviour().getPosition() == TriggerCloseBehaviour.Position.DOWN_RIGHT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else if (triggerData.getCloseBehaviour().getPosition() == TriggerCloseBehaviour.Position.DOWN_LEFT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }

        closeImageButton.setLayoutParams(layoutParams);
        textViewTimer.setLayoutParams(layoutParams);
        textViewTimer.setGravity(Gravity.CENTER);
        textViewTimer.setBackground(getDrawable(R.drawable.counter_ring));
    }

    private void updateExit() {
        int transitionId;
        switch (triggerData.getExitAnimation()) {
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

    @Override
    protected void onStart() {
        super.onStart();
        startTime = new Date();
        Event event = new Event("CE Trigger Displayed", new HashMap<>());
        HttpCallsHelper.sendEvent(event, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Blurry.with(getApplicationContext())
                .radius(triggerData.getTriggerBackground().getBlur() != 0
                        ? triggerData.getTriggerBackground().getBlur()
                        : 25)
                .sampling(2)
                .async()
                .animate(500)
                .onto((ViewGroup) _window.getDecorView());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Blurry.delete((ViewGroup) _window.getDecorView());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
    }

    /**
     * Send trigger KPIs to the next activity(FeedbackActivity) to be sent back to the server
     */
    @Override
    public void finish() {
        super.finish();
        int duration = (int) ((new Date().getTime() - startTime.getTime()) / 1000);
        int totalWatched = videoDuration * videoSeenCounter + watchedTill;

        Map<String, String> kpiMap = new HashMap<>();
        kpiMap.put("Duration", String.valueOf(duration));
        kpiMap.put("Close Behaviour", closeBehaviour);

        if (triggerData.getType() == TriggerData.Type.VIDEO) {
            kpiMap.put("Video Duration", String.valueOf(videoDuration));
            kpiMap.put("Watched Till", String.valueOf(watchedTill));
            kpiMap.put("Total Watched", String.valueOf(totalWatched));
            kpiMap.put("Video Unmuted", String.valueOf(isVideoUnmuted));
        }

        Event event = new Event("CE Trigger Closed", kpiMap);
        HttpCallsHelper.sendEvent(event, null);

        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }

        updateExit();
    }
}
