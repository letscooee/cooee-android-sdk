package com.letscooee.trigger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.letscooee.CooeeSDK;
import com.letscooee.R;
import com.letscooee.models.Event;
import com.letscooee.models.SidePopSetting;
import com.letscooee.models.TriggerBackground;
import com.letscooee.models.TriggerBehindBackground;
import com.letscooee.models.TriggerButton;
import com.letscooee.models.TriggerButtonAction;
import com.letscooee.models.TriggerCloseBehaviour;
import com.letscooee.models.TriggerData;
import com.letscooee.models.TriggerText;
import com.letscooee.retrofit.HttpCallsHelper;
import com.letscooee.utils.BlurBuilder;
import com.letscooee.utils.OnInAppCloseListener;
import com.letscooee.utils.OnInAppPopListener;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.sentry.Sentry;
import jp.wasabeef.blurry.Blurry;

public class EngagementTriggerActivity extends AppCompatActivity {

    private static final String TAG = "EngagementTrigger";
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
    private static Bitmap flutterBitmap;
    public static OnInAppPopListener onInAppPopListener;
    public static OnInAppCloseListener onInAppCloseListener;

    public static void setBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        flutterBitmap = decodedByte;
    }

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
        } catch (Exception e) {

            Log.e(TAG, "Engagement Trigger Failed: ", e);
            Sentry.captureException(e);
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
        String color = triggerButton.getColor().isEmpty() ? "#0000FF" : triggerButton.getColor();
        button.setTextColor(Color.parseColor(color));
        button.setPadding(30, 20, 25, 25);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setElevation(20);
        button.setTranslationZ(20);

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
     * Action/data to be sent to application will be defined here
     */
    private void didClick(TriggerButtonAction action) {
        InAppListener listener = inAppListenerWeakReference.get();
        if (action.getKv() != null) {
            listener.inAppNotificationDidClick(action.getKv());
        }

        if (action.getUserProperty() != null) {
            Map<String, Object> userProfile = new HashMap<>();
            Map recieved = new HashMap<String, Object>();
            recieved.put("triggerID", triggerData.getId());
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
        if (TextUtils.isEmpty(triggerData.getMessage().getText()) && TextUtils.isEmpty(triggerData.getTitle().getText())) {
            findViewById(R.id.textLayout).setVisibility(View.GONE);
        }
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
            runnable = this::finish;
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
                didClick(triggerData.getBackground().getAction());
                closeBehaviour = "Trigger Touch";
                finish();
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
                didClick(triggerData.getSidePopSetting().getAction());
                closeBehaviour = "Action Button";
                finish();
            });
        }
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
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView imageView = new ImageView(EngagementTriggerActivity.this);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Glide.with(getApplicationContext()).load(triggerData.getImageUrl()).into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (triggerData.getFill() != TriggerData.Fill.HALF_INTERSTITIAL) {
                    if (resource.getIntrinsicHeight() > resource.getIntrinsicWidth()) {
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    } else {
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                } else {
                    if (resource.getIntrinsicHeight() > resource.getIntrinsicWidth()) {
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } else {
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                }
                imageView.setImageDrawable(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

        ImageView layeredImageView = new ImageView(EngagementTriggerActivity.this);
        layeredImageView.setLayoutParams(layoutParams);
        layeredImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (triggerData.getFill() == TriggerData.Fill.SIDE_POP) {
            layeredImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        Glide.with(getApplicationContext()).load(triggerData.getImageUrl()).into(imageView);
        Glide.with(getApplicationContext()).load(triggerData.getLayeredImageUrl()).into(layeredImageView);

        layeredImageView.setLayoutParams(layoutParams);
        insideMediaFrameLayout.addView(imageView);
        insideMediaFrameLayout.addView(layeredImageView);
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

        RelativeLayout relativeLayoutClose = findViewById(R.id.relativeLayoutClose);

        ProgressBar progressBarClose = findViewById(R.id.progressBarClose);
        progressBarClose.setProgress(100);
        int progressTextColor = TextUtils.isEmpty(triggerData.getCloseBehaviour().getCountDownTextColor()) ? Color.parseColor("#000000")
                : Color.parseColor(triggerData.getCloseBehaviour().getCountDownTextColor());
        textViewTimer.setTextColor(progressTextColor);


        int progressColor = TextUtils.isEmpty(triggerData.getCloseBehaviour().getProgressBarColor()) ? Color.parseColor("#4285f4") :
                Color.parseColor(triggerData.getCloseBehaviour().getProgressBarColor());
        progressBarClose.getIndeterminateDrawable().setColorFilter(progressColor, PorterDuff.Mode.SRC_IN);

        int closeButtonColor = TextUtils.isEmpty(triggerData.getCloseBehaviour().getCloseButtonColor()) ? Color.parseColor("#000000")
                : Color.parseColor(triggerData.getCloseBehaviour().getCloseButtonColor());
        closeImageButton.setColorFilter(closeButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);

        if (!triggerData.getCloseBehaviour().isAuto() || triggerData.getCloseBehaviour().getTimeToClose() != 0) {
            closeImageButton.setVisibility(View.INVISIBLE);
            closeImageButton.setEnabled(false);
            new CountDownTimer(triggerData.getCloseBehaviour().getTimeToClose() * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    textViewTimer.setText(String.valueOf((millisUntilFinished / 1000) + 1));
                    progressBarClose.setProgress(progressBarClose.getProgress() - (100 / triggerData.getCloseBehaviour().getTimeToClose() + 1));
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
        Map eventProps = new HashMap<String, Object>();
        if (triggerData != null)
            eventProps.put("triggerID", triggerData.getId());
        Event event = new Event("CE Trigger Displayed", eventProps);
        HttpCallsHelper.sendEvent(getApplicationContext(), event, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (triggerData.getTriggerBackground().getType() == TriggerBehindBackground.Type.BLURRED) {
            Blurry.with(getApplicationContext())
                    .radius(triggerData.getTriggerBackground().getBlur() != 0
                            ? triggerData.getTriggerBackground().getBlur()
                            : 25)
                    .sampling(2)
                    .animate(500)
                    .onto((ViewGroup) _window.getDecorView());

            if (onInAppPopListener != null) {
                onInAppPopListener.onInAppTriggered();
            }
        } else if (triggerData.getTriggerBackground().getType() == TriggerBehindBackground.Type.SOLID_COLOR) {
            ImageView imageView = findViewById(R.id.blurImage);
            if (triggerData.getTriggerBackground() != null) {
                if (!TextUtils.isEmpty(triggerData.getTriggerBackground().getColor())) {
                    Bitmap bmp = Bitmap.createBitmap(500, 1024, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bmp);
                    canvas.drawColor(Color.parseColor("" + triggerData.getTriggerBackground().getColor()));

                    imageView.setImageBitmap(BlurBuilder.blur(this, bmp));
                } else {
                    imageView.setBackgroundColor(Color.parseColor("#828282"));
                }
            } else {
                imageView.setBackgroundColor(Color.parseColor("#828282"));
            }
            imageView.setAlpha((float) triggerData.getTriggerBackground().getBlur() / 10);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Blurry.delete((ViewGroup) _window.getDecorView());
        if (onInAppCloseListener != null) {
            onInAppCloseListener.onInAppClosed();
        }
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

        Map<String, Object> kpiMap = new HashMap<>();
        kpiMap.put("Duration", duration);
        kpiMap.put("Close Behaviour", closeBehaviour);
        kpiMap.put("triggerID", triggerData.getId());

        if (triggerData.getType() == TriggerData.Type.VIDEO) {
            kpiMap.put("Video Duration", videoDuration);
            kpiMap.put("Watched Till", watchedTill);
            kpiMap.put("Total Watched", totalWatched);
            kpiMap.put("Video Unmuted", isVideoUnmuted);

        }

        Event event = new Event("CE Trigger Closed", kpiMap);
        HttpCallsHelper.sendEvent(getApplicationContext(), event, null);

        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }

        updateExit();
    }
}
