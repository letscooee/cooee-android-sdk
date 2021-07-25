package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import androidx.core.content.ContextCompat;
import com.letscooee.R;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.VideoElement;
import com.letscooee.trigger.inapp.InAppGlobalData;
import com.letscooee.utils.Timer;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class VideoRenderer extends AbstractInAppRenderer {

    private final VideoElement elementData;
    private final VideoView videoView;
    private final ImageView playPauseImage = new ImageView(context);
    private final Map<String, Object> closedEventProps;

    private int videoDuration;
    private int videoSeenCounter;

    public VideoRenderer(Context context, ViewGroup parentView, BaseElement element, InAppGlobalData globalData) {
        super(context, parentView, element, globalData);
        this.elementData = (VideoElement) element;
        this.videoView = new VideoView(context);
        this.closedEventProps = globalData.getClosedEventProps();
    }

    @Override
    public View render() {
        RelativeLayout videoView = new RelativeLayout(context);
        videoView.setGravity(Gravity.CENTER);
        newElement = videoView;
        this.renderVideoView();

        parentElement.addView(newElement);
        processCommonBlocks();

        return newElement;
    }

    private void renderVideoView() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        videoView.setLayoutParams(layoutParams);
        videoView.setVideoPath(elementData.getUrl());
        // TODO: 25/07/21 Why this is 30?
        videoView.seekTo(30);
        videoView.start();

        this.addPlayPauseButton();
        this.addMuteButton();
        this.addListeners();

        ((RelativeLayout) newElement).addView(videoView);
    }

    private void addPlayPauseButton() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        playPauseImage.setLayoutParams(params);
        playPauseImage.setVisibility(View.INVISIBLE);
        playPauseImage.setTranslationZ(10);

        ((RelativeLayout) newElement).addView(playPauseImage);
    }

    private void addMuteButton() {
        RelativeLayout.LayoutParams muteButtonBackgroundParams = new RelativeLayout.LayoutParams(90, 90);
        muteButtonBackgroundParams.setMargins(20, 20, 0, 0);

        RelativeLayout muteButtonBackground = new RelativeLayout(context);
        muteButtonBackground.setLayoutParams(muteButtonBackgroundParams);
        muteButtonBackground.setElevation(5f);

        RelativeLayout.LayoutParams muteButtonParams = new RelativeLayout.LayoutParams(50, 50);
        muteButtonParams.setMargins(0, 0, 20, 20);

        Button muteUnmuteButton = new Button(context);
        muteUnmuteButton.setLayoutParams(muteButtonParams);
        muteUnmuteButton.setBackground(ContextCompat.getDrawable(context, R.drawable.mute_background));
        muteUnmuteButton.setOnClickListener(v -> {
            closedEventProps.put("Video UnMuted", true);
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (am.isStreamMute(AudioManager.STREAM_MUSIC)) {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
                    muteUnmuteButton.setBackground(ContextCompat.getDrawable(context, R.drawable.unmute_background));
                } else {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                    muteUnmuteButton.setBackground(ContextCompat.getDrawable(context, R.drawable.mute_background));
                }
            }
        });

        muteButtonBackground.addView(muteUnmuteButton);
        ((RelativeLayout) newElement).addView(muteButtonBackground);
    }

    private void addListeners() {
        videoView.setOnClickListener(v -> {
            playPauseImage.setVisibility(View.VISIBLE);
            if (videoView.isPlaying()) {
                videoView.pause();
                playPauseImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_play_arrow_24));
            } else {
                videoView.start();
                playPauseImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_pause_24));
            }

            Timer handler = new Timer();
            handler.schedule(() -> playPauseImage.setVisibility(View.INVISIBLE), 2000);
        });

        videoView.setOnPreparedListener(mp -> {
            videoDuration = videoView.getDuration() / 1000;
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setStreamMute(AudioManager.STREAM_MUSIC, true);
        });

        videoView.setOnCompletionListener(mp -> {
            playPauseImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_replay_24));
            playPauseImage.setVisibility(View.VISIBLE);
            videoSeenCounter++;
        });

        calculateCurrentPositionThread();
    }

    /**
     * Calculate how much video is seen. Triggered only if VIDEO media is given by server
     */
    private void calculateCurrentPositionThread() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(this::updateDurationWatched, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void updateDurationWatched() {
        videoView.post(() -> {
            int watchedTill = videoView.getCurrentPosition() / 1000;
            int totalWatched = videoDuration * videoSeenCounter + watchedTill;

            closedEventProps.put("Video Duration", videoDuration);
            closedEventProps.put("Watched Till", watchedTill);
            closedEventProps.put("Total Watched", totalWatched);
        });
    }
}