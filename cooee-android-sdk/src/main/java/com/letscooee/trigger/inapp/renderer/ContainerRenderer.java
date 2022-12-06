package com.letscooee.trigger.inapp.renderer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.RestrictTo;
import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.device.DeviceInfo;
import com.letscooee.models.trigger.blocks.AutoClose;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.ButtonElement;
import com.letscooee.models.trigger.elements.ImageElement;
import com.letscooee.models.trigger.elements.ShapeElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.models.trigger.elements.VideoElement;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.trigger.inapp.TriggerContext;
import com.letscooee.utils.RuntimeData;

/**
 * Renders the topmost container of the in-app.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ContainerRenderer extends AbstractInAppRenderer {

    private final InAppTrigger inAppTrigger;
    private final DeviceInfo deviceInfo;
    private final RuntimeData runtimeData;
    private double displayWidth;
    private double displayHeight;

    public ContainerRenderer(Context context, ViewGroup parentView, BaseElement element, InAppTrigger inAppTrigger,
                             TriggerContext globalData) {
        super(context, parentView, element, globalData);
        this.inAppTrigger = inAppTrigger;
        this.deviceInfo = CooeeFactory.getDeviceInfo();
        this.runtimeData = CooeeFactory.getRuntimeData();
        updateScalingFactor();
    }

    @Override
    public View render() {
        newElement = new FrameLayout(context);

        insertNewElementInHierarchy();
        processCommonBlocks();
        if (inAppTrigger != null) {
            processChildren(getElementElevation(this.elementData));
        }

        // For container, it is necessary to be of size of device i.e. match_parent
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(MP, MP);
        baseFrameLayout.setLayoutParams(frameLayoutParams);
        newElement.setLayoutParams(frameLayoutParams);

        // Removing and Adding material view to render it on top layer
        globalData.getTriggerParentLayout().removeView(materialCardView);
        globalData.getTriggerParentLayout().addView(materialCardView);
        materialCardView.setRadius(0);

        processAutoClose();

        return newElement;
    }

    /**
     * Process {@link InAppTrigger} autoClose properties.
     *
     * @since 1.4.2
     */
    @SuppressLint("SetTextI18n")
    private void processAutoClose() {
        AutoClose autoClose = inAppTrigger.getAutoClose();
        if (autoClose == null) {
            return;
        }

        int seconds = autoClose.getSeconds();
        if (seconds <= 0) {
            return;
        }

        String hex = autoClose.getProgressBarColour();
        if (TextUtils.isEmpty(hex)) {
            hex = "#000000";
        }

        int color = Color.parseColor(hex);

        @SuppressLint("InflateParams") View progressView =
                LayoutInflater.from(context).inflate(R.layout.view_horizontal_progress_countdown, null);
        ProgressBar progressBar = progressView.findViewById(R.id.pbAutoCloseProgressBar);
        TextView progressText = progressView.findViewById(R.id.tvAutoCloseCountDown);

        progressBar.setProgressTintList(ColorStateList.valueOf(color));

        progressText.setText("Closes in " + seconds + " second");
        progressView.setVisibility(autoClose.isHideProgress() ? View.INVISIBLE : View.VISIBLE);

        CountDownTimer autoCloseTimer = new CountDownTimer(((long) seconds * 1000), 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int timeRemaining = (int) (millisUntilFinished / 1000);
                int progressPercent = (timeRemaining * 100) / seconds;

                ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress",
                        progressBar.getProgress(), progressPercent);
                progressAnimator.setDuration(850);
                progressAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        // Noting to do
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressText.setText("Closes in " + timeRemaining + " second");
                        progressBar.setProgress(progressPercent);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // Noting to do
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // Noting to do
                    }
                });
                progressAnimator.start();

            }

            @Override
            public void onFinish() {
                globalData.closeInApp("Auto");
            }
        };

        addCountDownView(progressView);
        globalData.setAutoCloseInAppCountDown(autoCloseTimer);
    }

    /**
     * Add AutoClose UI to the Containers bottom
     *
     * @param progressView {@link View} to be added in container
     * @since 1.4.2
     */
    private void addCountDownView(View progressView) {
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new FrameLayout.LayoutParams(MP, MP));
        relativeLayout.addView(progressView);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(MP, WC);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        progressView.setLayoutParams(layoutParams);
        materialCardView.addView(relativeLayout);
    }

    private int getElementElevation(BaseElement baseElement) {
        if (baseElement.getShadow() == null) {
            return 0;
        }

        return baseElement.getShadow().getElevation();
    }

    protected void processChildren(int alpha) {

        int lastElevation = Math.max(alpha, 0);

        for (BaseElement child : inAppTrigger.getElements()) {
            lastElevation = Math.max(lastElevation, getElementElevation(child)) + 1;
            child.setZ(lastElevation);

            if (child instanceof ImageElement) {
                new ImageRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof ButtonElement) {
                new ButtonRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof TextElement) {
                new TextRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof VideoElement) {
                new VideoRenderer(context, (ViewGroup) newElement, child, globalData).render();

            } else if (child instanceof ShapeElement) {
                new ShapeRenderer(context, (ViewGroup) newElement, child, globalData).render();

            }
        }
    }

    @Override
    protected void processSizeBlock() {
        super.processSizeBlock();
        ViewGroup.LayoutParams layoutParams = parentElement.getLayoutParams();
        layoutParams.height = AbstractInAppRenderer.MP;
        layoutParams.width = AbstractInAppRenderer.MP;
        parentElement.setLayoutParams(layoutParams);
    }

    /**
     * Override applyPositionBlock to prevent position of container.
     */
    @Override
    protected void applyPositionBlock() {
        // No position block for container
    }

    /**
     * Calculates the scaling factor for the container and add it to {@link TriggerContext}.
     */
    private void updateScalingFactor() {
        getDisplayHeightAndWidth();
        logger.debug("Display width: " + displayWidth + ", height: " + displayHeight);

        double containerWidth = elementData.getWidth();
        double containerHeight = elementData.getHeight();

        double scalingFactor;
        if (displayWidth / displayHeight < containerWidth / containerHeight) {
            scalingFactor = displayWidth / containerWidth;
        } else {
            scalingFactor = displayHeight / containerHeight;
        }

        globalData.setScalingFactor(scalingFactor);
    }

    /**
     * Updates the height and width of the container. If the container is
     * {@link TriggerContext#isCurrentActivityFullscreen()}
     * <p>
     * returns {@code true}.
     */
    private void getDisplayHeightAndWidth() {
        /*
         * If application configuration is present and height & width are not undefined
         * then there is no need to check anything else.
         */
        Configuration configuration = runtimeData.getAppCurrentConfiguration();
        if (configuration != null && configuration.screenWidthDp != Configuration.SCREEN_WIDTH_DP_UNDEFINED
                && configuration.screenHeightDp != Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
            double density = deviceInfo.getDensity();

            displayWidth = configuration.screenWidthDp * density;
            displayHeight = configuration.screenHeightDp * density;
            return;
        }

        if (!globalData.isCurrentActivityFullscreen()) {
            displayWidth = deviceInfo.getRunTimeDisplayWidth();
            displayHeight = deviceInfo.getRunTimeDisplayHeight();
            return;
        }

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Rect rect = windowManager.getCurrentWindowMetrics().getBounds();
            displayWidth = rect.width();
            displayHeight = rect.height();
        } else {
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            displayWidth = point.x;
            displayHeight = point.y;
        }
    }

}
