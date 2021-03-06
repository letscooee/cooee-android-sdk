package com.letscooee.trigger.inapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.flexbox.FlexboxLayout;
import com.letscooee.CooeeFactory;
import com.letscooee.CooeeSDK;
import com.letscooee.R;
import com.letscooee.models.*;
import com.letscooee.network.SafeHTTPService;
import com.letscooee.utils.Constants;
import com.letscooee.utils.SentryHelper;

import jp.wasabeef.blurry.Blurry;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InAppTriggerActivity extends AppCompatActivity implements PreventBlurActivity {

    private static Window lastActiveWindow;

    private TriggerData triggerData;
    private Bitmap bitmapForBlurry;
    private ViewGroup viewGroupForBlurry;

    ImageButton closeImageButton;
    RelativeLayout secondParentLayout;
    TextView textViewTimer;

    private WeakReference<InAppListener> inAppListenerWeakReference;

    private Date startTime;
    private String closeBehaviour;
    private String ctaLabel;
    private Handler handler;
    private Runnable runnable;
    private int videoDuration;
    private int watchedTill;
    private int videoSeenCounter = 0;
    private boolean isVideoUnmuted;
    private boolean isManualClose;
    private boolean isSuccessfullyStarted;
    /**
     * This flag tells that this In-App activity was created for the first time and not when Android re-created the activity
     * because of certain configuration change like orientation, background <-> foreground.
     */
    private boolean isFreshLaunch;

    private final SafeHTTPService safeHTTPService;
    private final SentryHelper sentryHelper;

    public InAppTriggerActivity() {
        safeHTTPService = CooeeFactory.getSafeHTTPService();
        sentryHelper = CooeeFactory.getSentryHelper();
    }

    public interface InAppListener {
        void inAppNotificationDidClick(HashMap<String, Object> payload);
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
     * Set Bitmap which can be used by {@link Blurry}. Mostly used by Flutter plugin.
     *
     * @param bitmap
     */
    public void setBitmapForBlurry(Bitmap bitmap) {
        this.bitmapForBlurry = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.isFreshLaunch = savedInstanceState == null;
        setContentView(R.layout.activity_engagement_trigger);

        closeImageButton = findViewById(R.id.buttonClose);
        closeImageButton.setOnClickListener(view -> {
            this.manualCloseTrigger("Close Button", null);
        });

        secondParentLayout = findViewById(R.id.secondParentRelative);
        textViewTimer = findViewById(R.id.textViewTimer);
        inAppListenerWeakReference = new WeakReference<>(CooeeSDK.getDefaultInstance(this));

        try {
            triggerData = getIntent()
                    .getBundleExtra("bundle")
                    .getParcelable(Constants.INTENT_TRIGGER_DATA_KEY);

            if (triggerData == null) {
                throw new Exception("Couldn't render In-App because trigger data is null");
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
            setL1Background();
            sendTriggerDisplayedEvent();
        } catch (Exception e) {
            sentryHelper.captureException(e);
            finish();
        }
    }

    private void manualCloseTrigger(String behaviour, TriggerButtonAction action) {
        manualCloseTrigger(behaviour, null, action);
    }

    private void manualCloseTrigger(String behaviour, String ctaPressed, TriggerButtonAction action) {
        this.isManualClose = true;
        this.closeBehaviour = behaviour;
        this.ctaLabel = ctaPressed;
        this.finish();

        if (action != null) {
            // Invoke the CTA only after this activity is finished
            nonCloseActionTaken(action);
        }
    }

    /**
     * Create defined action buttons for the trigger
     */
    private void createActionButtons() {
        if (triggerData.getButtons() != null && triggerData.getButtons().length > 0) {
            if (triggerData.getButtons()[0] != null || !triggerData.getButtons()[0].getText().isEmpty()) {
                for (TriggerButton triggerButton : triggerData.getButtons()) {
                    createButton(triggerButton);
                }

            } else {
                findViewById(R.id.actionLayout).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.actionLayout).setVisibility(View.GONE);
        }
    }

    /**
     * Create and add action button
     *
     * @param triggerButton trigger button data
     */
    @SuppressLint("ClickableViewAccessibility")
    private void createButton(TriggerButton triggerButton) {
        FlexboxLayout flexboxLayout = findViewById(R.id.actionFlexLayout);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(15, 15, 15, 15);

        TextView button = new TextView(this);
        button.setLayoutParams(params);
        button.setText(triggerButton.getText());

        button.setTextColor(triggerButton.getParsedColor());
        button.setPadding(30, 20, 25, 25);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        //button.setElevation(20);
        button.setTranslationZ(20);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(triggerButton.getRadius());
        drawable.setColor(Color.parseColor(triggerButton.getBackground()));
        button.setBackground(drawable);

        button.setOnClickListener(view -> {
            // Sending the text of button which is clicked as event props
            this.manualCloseTrigger("Action Button", triggerButton.getText(), triggerButton.getAction());
        });

        button.setOnTouchListener((v, event) -> {
            if (MotionEvent.ACTION_DOWN == event.getAction()) {
                drawable.setCornerRadius(triggerButton.getRadius());
                drawable.setStroke(1, Color.BLACK);
                drawable.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
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
     * Action/data to be sent to application i.e. the callback of CTA.
     */
    private void nonCloseActionTaken(TriggerButtonAction action) {
        InAppListener listener = inAppListenerWeakReference.get();
        if (action.getKv() != null) {
            listener.inAppNotificationDidClick(action.getKv());
        }

        if (action.getUserProperty() != null) {
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("userData", new HashMap<>());
            userProfile.put("userProperties", action.getUserProperty());
            CooeeFactory.getSafeHTTPService().updateUserProfile(userProfile);
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
        if (TextUtils.isEmpty(triggerData.getMessage().getText()) && TextUtils.isEmpty(triggerData.getTitle().getText())) {
            findViewById(R.id.textLayout).setVisibility(View.GONE);
        }
    }

    /**
     * Update the close configuration
     * eg. if it should be auto close or close button is provided
     */
    private void updateClose() {
        if (triggerData.getCloseBehaviour().isAuto()) {
            int autoClose = triggerData.getCloseBehaviour().getTimeToClose();
            closeImageButton.setVisibility(View.GONE);
            textViewTimer.setVisibility(View.GONE);
            handler = new Handler();
            runnable = () -> {
                this.manualCloseTrigger("Auto Closed", null);
            };
            handler.postDelayed(runnable, autoClose * 1000);
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
        if (triggerData.getBackground().getType() == TriggerBackground.TriggerType.SOLID_COLOR) {
            String color = triggerData.getBackground().getColor();

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
                            drawable.setCornerRadius(triggerData.getBackground().getRadius());
                            ((ImageView) findViewById(R.id.imageViewBackground)).setImageDrawable(drawable);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }

        if (triggerData.getBackground().getAction() != null) {
            secondParentLayout.setOnClickListener(v -> {
                this.manualCloseTrigger("Trigger Touch", triggerData.getBackground().getAction());
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
            //findViewById(R.id.contentLinearLayout).setLayoutParams(layoutParams);
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

        } else if (triggerData.getFill() == TriggerData.Fill.SIDE_POP) {

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ((LinearLayout) findViewById(R.id.contentLinearLayout)).setOrientation(LinearLayout.VERTICAL);
                layoutParams = new RelativeLayout.LayoutParams((int) (dm.widthPixels * 0.15), (int) (dm.heightPixels * 0.9));
            } else {
                layoutParams = new RelativeLayout.LayoutParams((int) (dm.widthPixels * 0.35), (int) (dm.heightPixels * 0.6));
            }
            switch (triggerData.getSidePopSetting().getPosition()) {
                case CENTER:
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    break;
                case TOP_LEFT:
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    layoutParams.setMargins(10, 10, 0, 0);
                    break;
                case TOP_CENTER:
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    layoutParams.setMargins(0, 10, 0, 0);
                    break;
                case TOP_RIGHT:
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                    layoutParams.setMargins(0, 10, 10, 0);
                    break;
                case BOTTOM_LEFT:
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                    layoutParams.setMargins(10, 0, 0, 10);
                    break;
                case LEFT_CENTER:
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                    layoutParams.setMargins(10, 0, 0, 0);
                    break;
                case BOTTOM_RIGHT:
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                    layoutParams.setMargins(0, 0, 10, 10);
                    break;
                case RIGHT_CENTER:
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                    layoutParams.setMargins(0, 0, 10, 0);
                    break;
                case BOTTOM_CENTER:
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    layoutParams.setMargins(0, 0, 0, 10);
                    break;
            }
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            );
            LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    2.0f
            );
            param1.setMargins(0, 0, 0, 0);
            findViewById(R.id.textLayout).setLayoutParams(param);
            findViewById(R.id.mediaRelativeLayout).setLayoutParams(param);
            RelativeLayout actionLayout = findViewById(R.id.actionLayout);
            actionLayout.setLayoutParams(param1);

            RelativeLayout.LayoutParams actionFlexLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            findViewById(R.id.actionFlexLayout).setLayoutParams(actionFlexLayoutParam);
            findViewById(R.id.textViewContent).setVisibility(View.GONE);

            setActionLayout();
        }

        secondParentLayout.setLayoutParams(layoutParams);
    }

    private void setActionLayout() {
        if (triggerData.getSidePopSetting() != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            if (triggerData.getSidePopSetting().getType() == SidePopSetting.Type.TEXT) {
                TextView textView = new TextView(this);
                textView.setLayoutParams(params);
                textView.setText(triggerData.getSidePopSetting().getText());
                textView.setTextColor(Color.parseColor(triggerData.getSidePopSetting().getTextColor()));
                textView.setBackgroundColor(Color.parseColor(triggerData.getSidePopSetting().getBackgroundColor()));
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) triggerData.getSidePopSetting().getTextSize());
                ((RelativeLayout) findViewById(R.id.actionLayout)).addView(textView);
            } else if (triggerData.getSidePopSetting().getType() == SidePopSetting.Type.IMAGE) {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(params);
                Glide.with(getApplicationContext()).load(triggerData.getSidePopSetting().getImageUrl()).into(imageView);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                ((RelativeLayout) findViewById(R.id.actionLayout)).addView(imageView);
            }
            secondParentLayout.setOnClickListener(v -> {
                this.manualCloseTrigger("Action Button", triggerData.getSidePopSetting().getAction());
            });
        }
    }

    private void hideMediaLayout() {
        RelativeLayout mediaRelativeLayout = findViewById(R.id.mediaRelativeLayout);
        mediaRelativeLayout.setVisibility(View.GONE);
    }

    /**
     * Create the media view given by server
     * eg. IMAGE, VIDEO (as of now)
     */
    private void addMediaView() {
        if (triggerData.getType() == TriggerData.Type.IMAGE) {
            if (TextUtils.isEmpty(triggerData.getImageUrl())) {
                hideMediaLayout();
                return;
            } else {
                createImageView();
            }

        } else if (triggerData.getType() == TriggerData.Type.VIDEO) {
            if (TextUtils.isEmpty(triggerData.getVideoUrl())) {
                hideMediaLayout();
                return;
            } else {
                createVideoView();
            }
        }

        CardView mediaFrameLayout = findViewById(R.id.mediaFrameLayout);
        if (triggerData.isShowImageShadow()) {
            if (triggerData.getImageShadow() != null) {
                mediaFrameLayout.setElevation((float) triggerData.getImageShadow());
                mediaFrameLayout.setElevation((float) triggerData.getImageShadow());
            }
        } else {
            mediaFrameLayout.setElevation(0f);
            mediaFrameLayout.setTranslationZ(0f);
        }
    }

    private void createImageView() {
        RelativeLayout insideMediaFrameLayout = findViewById(R.id.insideMediaFrameLayout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView imageView = new ImageView(InAppTriggerActivity.this);
        imageView.setLayoutParams(layoutParams);

        Glide.with(getApplicationContext()).load(triggerData.getImageUrl()).into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                // While loading vertical images, action button goes outside the layout if image height is big
                // so this condition fixes the height issue
                if (resource.getMinimumHeight() > resource.getMinimumWidth()) {
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1.0f
                    );
                    findViewById(R.id.mediaRelativeLayout).setLayoutParams(param);
                }

                // https://stackoverflow.com/a/19286130
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setImageDrawable(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

        ImageView layeredImageView = new ImageView(InAppTriggerActivity.this);
        layeredImageView.setLayoutParams(layoutParams);
        layeredImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (triggerData.getFill() == TriggerData.Fill.SIDE_POP) {
            layeredImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        Glide.with(getApplicationContext()).load(triggerData.getLayeredImageUrl()).into(layeredImageView);

        layeredImageView.setLayoutParams(layoutParams);
        insideMediaFrameLayout.addView(imageView);
        insideMediaFrameLayout.addView(layeredImageView);
    }

    private void createVideoView() {
        RelativeLayout insideMediaFrameLayout = findViewById(R.id.insideMediaFrameLayout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        VideoView videoView = new VideoView(InAppTriggerActivity.this);

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
        insideMediaFrameLayout.addView(muteButtonBackground);

        videoView.setOnClickListener(view -> {
            playPauseImage.setVisibility(View.VISIBLE);
            if (videoView.isPlaying()) {
                videoView.pause();
                playPauseImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_play_arrow_24));
            } else {
                videoView.start();
                playPauseImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_24));
            }
            handler = new Handler();
            handler.postDelayed(() -> playPauseImage.setVisibility(View.INVISIBLE), 2000);
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
        executorService.scheduleWithFixedDelay(() -> videoView.post(() ->
                watchedTill = videoView.getCurrentPosition() / 1000), 1000, 1000, TimeUnit.MILLISECONDS);

    }

    /**
     * Update the position of close button(if should be given)
     * eg. TOP_LEFT(default), TOP_RIGHT, DOWN_RIGHT and DOWN_LEFT
     */
    private void closeButtonPosition() {
        TriggerCloseBehaviour closeBehaviour = triggerData.getCloseBehaviour();
        RelativeLayout relativeLayoutClose = findViewById(R.id.relativeLayoutClose);

        ProgressBar progressBarClose = findViewById(R.id.progressBarClose);
        progressBarClose.setProgress(100);
        textViewTimer.setTextColor(closeBehaviour.getParsedCountDownTextColor());

        progressBarClose.getIndeterminateDrawable().setColorFilter(closeBehaviour.getParsedProgressBarColor(), PorterDuff.Mode.SRC_IN);

        closeImageButton.setColorFilter(closeBehaviour.getParsedCloseButtonColor(), android.graphics.PorterDuff.Mode.SRC_IN);

        if (closeBehaviour.shouldShowButton()) {
            if (!closeBehaviour.isAuto() || closeBehaviour.getTimeToClose() != 0) {
                closeImageButton.setVisibility(View.INVISIBLE);
                closeImageButton.setEnabled(false);
                new CountDownTimer(closeBehaviour.getTimeToClose() * 1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        textViewTimer.setText(String.valueOf((millisUntilFinished / 1000) + 1));
                        progressBarClose.setProgress(progressBarClose.getProgress() - (100 / closeBehaviour.getTimeToClose() + 1));
                    }

                    public void onFinish() {
                        textViewTimer.setVisibility(View.GONE);
                        progressBarClose.setVisibility(View.GONE);
                        closeImageButton.setVisibility(View.VISIBLE);
                        closeImageButton.setEnabled(true);
                    }
                }.start();
            } else {
                textViewTimer.setVisibility(View.GONE);
                progressBarClose.setVisibility(View.GONE);
            }
        } else {
            closeImageButton.setVisibility(View.GONE);
            progressBarClose.setVisibility(View.GONE);
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (closeBehaviour.getPosition() == TriggerCloseBehaviour.Position.TOP_RIGHT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        } else if (closeBehaviour.getPosition() == TriggerCloseBehaviour.Position.DOWN_RIGHT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else if (closeBehaviour.getPosition() == TriggerCloseBehaviour.Position.DOWN_LEFT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }

        closeImageButton.setLayoutParams(layoutParams);
        relativeLayoutClose.setLayoutParams(layoutParams);
        textViewTimer.setGravity(Gravity.CENTER);
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
        isSuccessfullyStarted = true;
    }

    private void sendTriggerDisplayedEvent() {
        if (!this.isFreshLaunch) {
            return;
        }

        Event event = new Event("CE Trigger Displayed", triggerData);
        safeHTTPService.sendEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setL1Background() {
        ImageView imageView = findViewById(R.id.blurImage);

        if (triggerData.getTriggerBackground().getType() == TriggerBehindBackground.Type.BLURRED) {
            this.setViewGroupForBlurry((ViewGroup) lastActiveWindow.getDecorView());

            Blurry.Composer blurryComposer = Blurry.with(getApplicationContext())
                    .radius(triggerData.getTriggerBackground().getBlurRadius())
                    .color(triggerData.getTriggerBackground().getParsedColor())
                    .sampling(triggerData.getTriggerBackground().getBlurSampling())
                    .animate(500);

            if (bitmapForBlurry != null) {
                blurryComposer
                        .from(bitmapForBlurry)
                        .into(findViewById(R.id.blurImage));
            } else {
                blurryComposer
                        .capture(viewGroupForBlurry)
                        .into(findViewById(R.id.blurImage));
            }

        } else if (triggerData.getTriggerBackground().getType() == TriggerBehindBackground.Type.SOLID_COLOR) {
            imageView.setBackgroundColor(triggerData.getTriggerBackground().getParsedColor());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
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
        kpiMap.put("CTA Label", this.ctaLabel);

        if (triggerData.getType() == TriggerData.Type.VIDEO) {
            kpiMap.put("Video Duration", videoDuration);
            kpiMap.put("Watched Till", watchedTill);
            kpiMap.put("Total Watched", totalWatched);
            kpiMap.put("Video Unmuted", isVideoUnmuted);
        }

        Event event = new Event("CE Trigger Closed", kpiMap);
        event.withTrigger(triggerData);
        safeHTTPService.sendEvent(event);

        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }

        updateExit();
    }

    public TriggerData getTriggerData() {
        return triggerData;
    }

    public boolean isManualClose() {
        return isManualClose;
    }
}
