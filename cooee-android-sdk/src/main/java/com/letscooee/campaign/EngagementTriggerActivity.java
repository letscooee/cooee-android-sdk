package com.letscooee.campaign;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.letscooee.R;
import com.letscooee.models.TriggerBackground;
import com.letscooee.models.TriggerData3;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EngagementTriggerActivity extends AppCompatActivity {

    TriggerData3 triggerData3;
    ImageButton closeImageButton;
    RelativeLayout secondParentLayout;
    TextView textViewTimer;

    private float completionRate = 0f;
    private boolean isViewed = false;
    private boolean isPlayed = false;
    private boolean isEngaged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engagement_trigger);
        closeImageButton = findViewById(R.id.buttonClose);
        closeImageButton.setOnClickListener(view -> finish());
        secondParentLayout = findViewById(R.id.secondParentRelative);
        textViewTimer = findViewById(R.id.textViewTimer);

        triggerData3 = (TriggerData3) Objects.requireNonNull(getIntent().getBundleExtra("bundle")).getParcelable("triggerData");

        if (triggerData3 == null) finish();

//        triggerData3 = new randomCode().x();

        updateFill();
        addMediaView();
        closeButtonPosition();
        updateBackground();
        updateEntrance();
        updateClose();
        updateText();
        updateMessage();
        updateTextPosition();
    }

    /**
     * Update text/media position as given by the server
     */
    private void updateTextPosition() {
        if (triggerData3.getTextPosition() == TriggerData3.TextPosition.BOTTOM || triggerData3.getTextPosition() == TriggerData3.TextPosition.RIGHT) {
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
        textView.setText(triggerData3.getText().getData());
        int color = triggerData3.getText().getColor() == null || triggerData3.getText().getColor().isEmpty()
                ? getResources().getColor(R.color.colorText)
                : Color.parseColor(triggerData3.getText().getColor());
        textView.setTextColor(color);
        textView.setTextSize(triggerData3.getText().getFontSize());
    }

    /**
     * Update message data
     */
    private void updateMessage() {
        TextView textView = findViewById(R.id.textViewContent);
        textView.setText(triggerData3.getMessage().getData());
        int color = triggerData3.getMessage().getColor() == null || triggerData3.getMessage().getColor().isEmpty()
                ? getResources().getColor(R.color.colorText)
                : Color.parseColor(triggerData3.getMessage().getColor());
        textView.setTextColor(color);
        textView.setTextSize(triggerData3.getMessage().getFontSize());
    }

    /**
     * Update the close configuration
     * eg. if it should be auto close or close button is provided
     */
    private void updateClose() {
        if (triggerData3.isAutoClose()) {
            int autoClose = (Integer) triggerData3.getAutoClose();
            closeImageButton.setVisibility(View.GONE);
            textViewTimer.setVisibility(View.GONE);
            new Handler().postDelayed(this::finish, autoClose * 1000);
        }
    }

    /**
     * Update the trigger entrance
     */
    private void updateEntrance() {
        int transitionId;
        switch (triggerData3.getEntranceAnimation()) {
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
        if (triggerData3.getBackground().getType() == TriggerBackground.TriggerType.SOLID_COLOR) {
            int color = triggerData3.getBackground().getColor() == null || triggerData3.getBackground().getColor().isEmpty()
                    ? Color.parseColor("#DDDDDD")
                    : Color.parseColor(triggerData3.getBackground().getColor());

            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(color);
            drawable.setCornerRadius(20);
            secondParentLayout.setBackground(drawable);
        } else if (triggerData3.getBackground().getType() == TriggerBackground.TriggerType.IMAGE) {
            if (triggerData3.getBackground().getImage() == null || triggerData3.getBackground().getImage().isEmpty()) {
                secondParentLayout.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                return;
            }

            Glide.with(this)
                    .asBitmap()
                    .load(triggerData3.getBackground().getImage())
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
        } else if (triggerData3.getBackground().getType() == TriggerBackground.TriggerType.BLURRED) {
            int z;

            if (triggerData3.getBackground().getBlur() == 0) {
                z = 255 - (int) (20 * 255 / 100);
            } else {
                z = 255 - (int) (triggerData3.getBackground().getBlur() * 255 / 100);
            }

            String y = Integer.toHexString(z);

            if (y.length() == 1) {
                y = "0".concat(y);
            }

            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(20f);
            drawable.setStroke(1, 0xFFFFFF);
            drawable.setColor(Color.parseColor("#" + y + "FFFFFF"));
            secondParentLayout.setBackground(drawable);
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

        if (triggerData3.getFill() == TriggerData3.Fill.COVER) {
            findViewById(R.id.contentLinearLayout).setLayoutParams(layoutParams);
            layoutParams.setMargins(0, 0, 0, 0);
        } else if (triggerData3.getFill() == TriggerData3.Fill.INTERSTITIAL) {
            layoutParams = new RelativeLayout.LayoutParams((int) (dm.widthPixels * 0.9), (int) (dm.heightPixels * 0.9));
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        } else if (triggerData3.getFill() == TriggerData3.Fill.HALF_INTERSTITIAL) {
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
        if (triggerData3.getType() == TriggerData3.Type.IMAGE) {
            if (triggerData3.getImageUrl() == null || triggerData3.getImageUrl().isEmpty()) {
                finish();
            }
            createImageView();
        } else if (triggerData3.getType() == TriggerData3.Type.VIDEO) {
            if (triggerData3.getVideoUrl() == null || triggerData3.getVideoUrl().isEmpty()) {
                finish();
            }
            createVideoView();
        }
    }

    private void createImageView(){
        RelativeLayout insideMediaFrameLayout = findViewById(R.id.insideMediaFrameLayout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView imageView = new ImageView(EngagementTriggerActivity.this);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(getApplicationContext()).load(triggerData3.getImageUrl()).into(imageView);

        new Handler().postDelayed(() -> isEngaged = true, 5000);

        insideMediaFrameLayout.addView(imageView);
    }

    private void createVideoView(){
        RelativeLayout insideMediaFrameLayout = findViewById(R.id.insideMediaFrameLayout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        VideoView videoView = new VideoView(EngagementTriggerActivity.this);

        videoView.setLayoutParams(layoutParams);
        videoView.setVideoPath(triggerData3.getVideoUrl());
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
        muteUnmuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlayed = true;
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (am.isStreamMute(AudioManager.STREAM_MUSIC)) {
                        am.setStreamMute(AudioManager.STREAM_MUSIC, false);
                        muteUnmuteButton.setBackground(getDrawable(R.drawable.unmute_background));
                    } else {
                        am.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        muteUnmuteButton.setBackground(getDrawable(R.drawable.mute_background));
                    }
                }
            }
        });

        insideMediaFrameLayout.addView(muteUnmuteButton);

        videoView.setOnClickListener(view -> {
            isPlayed = true;
            playPauseImage.setVisibility(View.VISIBLE);
            if (videoView.isPlaying()) {
                videoView.pause();
                playPauseImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
            } else {
                videoView.start();
                playPauseImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_pause_24));
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playPauseImage.setVisibility(View.INVISIBLE);
                }
            }, 2000);
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playPauseImage.setImageDrawable(getDrawable(R.drawable.ic_baseline_replay_24));
                playPauseImage.setVisibility(View.VISIBLE);
            }
        });

        calculateCurrentPositionThread(videoView);

        insideMediaFrameLayout.addView(videoView);
    }

    /**
     * Calculate how much video is seen. Triggered only if VIDEO media is given by server
     *
     * @param videoView
     */
    private void calculateCurrentPositionThread(VideoView videoView) {
        ScheduledExecutorService mScheduledExecutorService;
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
        mScheduledExecutorService.scheduleWithFixedDelay(() -> videoView.post(() -> {
            int currentPosition = videoView.getCurrentPosition();
            completionRate = ((float) currentPosition / (float) videoView.getDuration()) * 100;

            if (completionRate >= 20) {
                isEngaged = true;
            }
        }), 3000, 1000, TimeUnit.MILLISECONDS);

    }

    /**
     * Update the position of close button(if should be given)
     * eg. TOP_LEFT(default), TOP_RIGHT, DOWN_RIGHT and DOWN_LEFT
     */
    private void closeButtonPosition() {
        closeImageButton.setVisibility(View.INVISIBLE);
        closeImageButton.setEnabled(false);

        new CountDownTimer(6000, 1000) {

            public void onTick(long millisUntilFinished) {
                textViewTimer.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                textViewTimer.setVisibility(View.GONE);
                closeImageButton.setVisibility(View.VISIBLE);
                closeImageButton.setEnabled(true);
            }
        }.start();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (triggerData3.getCloseButtonPosition() == TriggerData3.CloseButtonPosition.TOP_RIGHT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        } else if (triggerData3.getCloseButtonPosition() == TriggerData3.CloseButtonPosition.DOWN_RIGHT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else if (triggerData3.getCloseButtonPosition() == TriggerData3.CloseButtonPosition.DOWN_LEFT) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }

        closeImageButton.setLayoutParams(layoutParams);
        textViewTimer.setLayoutParams(layoutParams);
        textViewTimer.setGravity(Gravity.CENTER);
        textViewTimer.setBackground(getDrawable(R.drawable.counter_ring));
    }

    private void updateExit() {
        int transitionId;
        switch (triggerData3.getExitAnimation()) {
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
        isViewed = true;
    }

    /**
     * Send trigger KPIs to the next activity(FeedbackActivity) to be sent back to the server
     */
    @Override
    public void finish() {
        super.finish();
        Bundle bundle = new Bundle();
        bundle.putString("CE Id", String.valueOf(triggerData3.getId()));
        bundle.putString("CE Viewed", String.valueOf(isViewed));
        bundle.putString("CE Engaged", String.valueOf(isEngaged));

        if (triggerData3.getType() == TriggerData3.Type.VIDEO) {
            bundle.putString("CE Completion Rate", completionRate + "");
            bundle.putString("CE Played", String.valueOf(isPlayed));
        }
        //TODO send feedback KPIs to server
        updateExit();
    }

    /**
     * Create a background screenshot that is used to create blur background
     *
     * @param view View whose background is needed
     * @return background screenshot as bitmap
     */
    public static Bitmap captureScreenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Drawable backgroundDrawable = view.getBackground();
        if (backgroundDrawable != null) {
            backgroundDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.parseColor("#80000000"));
        }
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Create blurred image of the bitmap given
     *
     * @param context context of the activity
     * @param image   bitmap image to be blurred
     * @return blurred bitmap image
     */
    public static Bitmap blur(Context context, Bitmap image) {
        float BITMAP_SCALE = 0.4f;
        float BLUR_RADIUS = 23f;

        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }
}
