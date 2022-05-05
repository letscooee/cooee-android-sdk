package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.letscooee.CooeeFactory;
import com.letscooee.device.DeviceInfo;
import com.letscooee.models.trigger.elements.*;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.trigger.inapp.TriggerContext;

import static com.letscooee.utils.Constants.TAG;

/**
 * Renders the topmost container of the in-app.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class ContainerRenderer extends AbstractInAppRenderer {

    private final InAppTrigger inAppTrigger;
    private final DeviceInfo deviceInfo;

    public ContainerRenderer(Context context, ViewGroup parentView, BaseElement element, InAppTrigger inAppTrigger,
                             TriggerContext globalData) {
        super(context, parentView, element, globalData);
        this.inAppTrigger = inAppTrigger;
        this.deviceInfo = CooeeFactory.getDeviceInfo();
        updateScalingFactor();
    }

    @Override
    public View render() {
        newElement = new FrameLayout(context);

        insertNewElementInHierarchy();
        processCommonBlocks();
        if (inAppTrigger != null) {
            processChildren();
        }

        // For container, it is necessary to be of size of device i.e. match_parent
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(MP, MP);
        baseFrameLayout.setLayoutParams(frameLayoutParams);
        newElement.setLayoutParams(frameLayoutParams);

        // Removing and Adding material view to render it on top layer
        globalData.getTriggerParentLayout().removeView(materialCardView);
        globalData.getTriggerParentLayout().addView(materialCardView);
        materialCardView.setRadius(0);

        return newElement;
    }

    protected void processChildren() {
        for (BaseElement child : inAppTrigger.getElements()) {
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
        double displayWidth = deviceInfo.getRunTimeDisplayWidth();
        double displayHeight = deviceInfo.getRunTimeDisplayHeight();
        Log.d(TAG, "Display width: " + displayWidth + ", height: " + displayHeight);

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
}
