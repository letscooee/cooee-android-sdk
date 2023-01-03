package com.letscooee.trigger.inapp.renderer;

import static com.letscooee.utils.Constants.TAG;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.RestrictTo;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.letscooee.BuildConfig;
import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.blocks.Border;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.models.trigger.blocks.Glassmorphism;
import com.letscooee.models.trigger.blocks.Shadow;
import com.letscooee.models.trigger.blocks.Spacing;
import com.letscooee.models.trigger.blocks.Transform;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.action.ClickActionExecutor;
import com.letscooee.trigger.inapp.TriggerContext;
import jp.wasabeef.blurry.Blurry;

/**
 * @author Ashish Gaikwad 09/07/21
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class AbstractInAppRenderer implements InAppRenderer {

    protected static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    protected static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    protected final TriggerContext globalData;
    protected final Context context;
    protected ViewGroup parentElement;
    protected final BaseElement elementData;
    protected final GradientDrawable backgroundDrawable = new GradientDrawable();

    protected final MaterialCardView materialCardView;
    protected final FrameLayout baseFrameLayout;
    protected final ImageView backgroundImage;

    /**
     * The newest element that will be rendered by the instance of this renderer.
     */
    protected View newElement;

    protected AbstractInAppRenderer(Context context, ViewGroup parentElement, BaseElement element,
                                    TriggerContext globalData) {
        this.context = context;
        this.parentElement = parentElement;
        this.elementData = element;
        this.globalData = globalData;

        this.materialCardView = new MaterialCardView(context);
        this.baseFrameLayout = new FrameLayout(context);
        this.backgroundImage = new ImageView(context);
        this.setupWrapperForNewElement();
    }

    /**
     * To achieve background image, border stroke or corner radius for all the elements (container/layer/group/text
     * etc.), we need to wrap every new element {@link #newElement} in the following hierarchy-
     *
     * <pre>
     *     |--- {@link #parentElement}
     *              |
     *              |--- {@link #materialCardView}
     *                      |
     *                      |--- {@link #baseFrameLayout} FrameLayout
     *                              |
     *                              |--- {@link #backgroundImage} Image View for Glossy/solid/image
     *                              |--- {@link #newElement} Our new element being inserted by this class. This can
     *                                      be {@link RelativeLayout}/TextView/Button
     *
     * </pre>
     */
    private void setupWrapperForNewElement() {
        baseFrameLayout.addView(backgroundImage);
        materialCardView.addView(baseFrameLayout);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(WC, WC);
        baseFrameLayout.setLayoutParams(layoutParams);
        backgroundImage.setLayoutParams(layoutParams);

        backgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        materialCardView.setCardBackgroundColor(Color.TRANSPARENT);
        materialCardView.setCardElevation(0);

        parentElement.addView(materialCardView);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Parent " + parentElement.getClass().getSimpleName());
        }
    }

    protected void setBackgroundDrawable() {
        baseFrameLayout.setBackground(backgroundDrawable);
    }

    protected void processCommonBlocks() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Process blocks for " + elementData.getClass().getSimpleName());
        }

        this.newElement.setTag(this.getClass().getSimpleName());
        this.processSizeBlock();
        this.applyPositionBlock();
        this.processSpacing();
        this.processBackground();
        this.setBackgroundDrawable();
        this.processBorderBlock();
        this.processTransformBlock();
        this.processClickBlock();
        this.processOpacity();
        this.processZIndex();
    }

    private void processZIndex() {
        boolean shadowApplied = this.processShadowBlock();
        Integer zIndex = elementData.getZ();
        if (zIndex == null || shadowApplied) {
            materialCardView.setTranslationZ(0f);
            return;
        }

        materialCardView.animate().z(zIndex.floatValue());
    }

    protected boolean processShadowBlock() {
        Shadow shadow = this.elementData.getShadow();
        if (shadow == null) {
            materialCardView.setElevation(0);
            /*
                To make overlapping work we assigning z index to all elements
                but card ads shadow to even if elevation is set to 0
                to remove shadow se are setting OutlineProvider to null
            */
            materialCardView.setOutlineProvider(null);
            return false;
        }
        materialCardView.setElevation(getScaledPixelAsInt(shadow.getElevation()));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return true;
        }

        materialCardView.setOutlineSpotShadowColor(shadow.getColor().getHexColor());
        return true;
    }

    protected void processOpacity() {
        Integer alpha = this.elementData.getAlpha();
        if (alpha == null) {
            return;
        }

        if (alpha < 0 || alpha > 100) {
            Log.e(TAG, "Received wrong alpha value: " + alpha);
            return;
        }

        this.materialCardView.setAlpha((((float) alpha) / 100));
    }

    protected void insertNewElementInHierarchy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Inserting new element " + newElement.getClass().getSimpleName());
        }

        this.baseFrameLayout.addView(newElement);
    }


    protected void processClickBlock() {
        ClickAction clickAction = elementData.getClickAction();
        if (clickAction == null) return;

        newElement.setOnClickListener(v -> new ClickActionExecutor(context, clickAction, globalData).execute());
    }

    protected void processTransformBlock() {
        Transform transform = elementData.getTransform();
        if (transform == null) return;

        materialCardView.setRotation(transform.getRotate());
    }

    protected void processSizeBlock() {
        ViewGroup.MarginLayoutParams layoutParams;

        int width = WC;
        int height = WC;

        int calculatedWidth = getScaledPixelAsInt(elementData.getWidth());
        if (calculatedWidth != 0) {
            width = calculatedWidth;
        }

        int calculatedHeight = getScaledPixelAsInt(elementData.getHeight());
        if (calculatedHeight != 0) {
            height = calculatedHeight;
        }

        this.newElement.setLayoutParams(new FrameLayout.LayoutParams(width, height));
        this.backgroundImage.setLayoutParams(new FrameLayout.LayoutParams(width, height));

        if (parentElement instanceof RelativeLayout) {
            layoutParams = new RelativeLayout.LayoutParams(width, height);
        } else if (parentElement instanceof LinearLayout) {
            layoutParams = new LinearLayout.LayoutParams(width, height);
        } else if (parentElement instanceof FrameLayout) {
            layoutParams = new FrameLayout.LayoutParams(width, height);
        } else {
            throw new RuntimeException("Unknown type of parentElement- " + parentElement);
        }

        this.materialCardView.setLayoutParams(layoutParams);
    }

    protected void applyPositionBlock() {

        float top = getScaledPixelAsFloat(elementData.getY());
        float left = getScaledPixelAsFloat(elementData.getX());

        materialCardView.setX(left);
        materialCardView.setY(top);
    }

    /**
     * Finds TitleBar height of the device
     *
     * @return <code>int</code> height of the TitleBar
     */
    @SuppressWarnings("unused")
    private float getTitleBarHeight() {
        Rect rectangle = new Rect();
        Window window = ((Activity) context).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        float contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return rectangle.top - contentViewTop;
    }

    protected void processBackground() {
        Background background = elementData.getBg();

        if (background != null) {
            if (background.getSolid() != null) {
                background.getSolid().updateDrawable(backgroundDrawable);

            } else if (background.getGlassmorphism() != null) {
                applyGlassmorphism(background.getGlassmorphism());

            } else if (background.getImage() != null) {
                Glide.with(context).asBitmap().load(background.getImage().getSrc()).into(backgroundImage);
            }
        }
    }

    private void applyGlassmorphism(Glassmorphism glassmorphism) {
        Blurry.Composer blurryComposer = Blurry.with(context)
                .animate(500);

        blurryComposer.radius(glassmorphism.getRadius());
        blurryComposer.sampling(glassmorphism.getSampling());

        if (glassmorphism.getColor() != null)
            blurryComposer.color(glassmorphism.getColor().getHexColor());

        if (globalData.getBitmapForBlurry() != null) {
            blurryComposer
                    .from(globalData.getBitmapForBlurry())
                    .into(backgroundImage);

        } else if (globalData.getViewGroupForBlurry() != null) {
            blurryComposer
                    .capture(globalData.getViewGroupForBlurry())
                    .into(backgroundImage);
        }

    }

    protected void processBorderBlock() {
        Border border = this.elementData.getBorder();

        if (border == null) {
            return;
        }

        int borderColor = border.getColor().getHexColor();
        int calculatedBorder = getScaledPixelAsInt(border.getWidth());
        float cornerRadius = getScaledPixelAsFloat(border.getRadius());

        if (border.getStyle() == Border.Style.SOLID) {
            materialCardView.setStrokeColor(borderColor);
            materialCardView.setStrokeWidth(calculatedBorder);
        } else if (border.getStyle() == Border.Style.DASH) {
            // Actual dash border code is there on respective element renderer. Here just adding
            // background color and corner radius.
            GradientDrawable materialDrawable = new GradientDrawable();
            materialDrawable.setCornerRadius(cornerRadius);
            if (this.elementData.getBg().getSolid() != null) {
                materialDrawable.setColor(this.elementData.getBg().getSolid().getHexColor());
            }
            materialCardView.setBackground(materialDrawable);
        }

        materialCardView.setRadius(cornerRadius);
        backgroundDrawable.setCornerRadius(cornerRadius);
    }

    protected void processSpacing() {
        Spacing spacing = elementData.getSpacing();
        if (spacing == null) {
            return;
        }

        int paddingLeft = getScaledPixelAsInt(spacing.getPaddingLeft());
        int paddingRight = getScaledPixelAsInt(spacing.getPaddingRight());
        int paddingTop = getScaledPixelAsInt(spacing.getPaddingTop());
        int paddingBottom = getScaledPixelAsInt(spacing.getPaddingBottom());

        this.newElement.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    /**
     * This method is used to get the scaled pixel value.
     *
     * @param value <code>double</code> value to be scaled
     * @return <code>double</code> value after scaling
     */
    protected double getScaledPixel(double value) {
        return value * globalData.getScalingFactor();
    }

    /**
     * This method is used to get the scaled pixel value in <code>float</code> format.
     *
     * @param value <code>double</code> value to be scaled
     * @return <code>float</code> value after scaling
     */
    protected float getScaledPixelAsFloat(double value) {
        return (float) getScaledPixel(value);
    }

    /**
     * This method is used to get the scaled pixel value in <code>int</code> format.
     *
     * @param value <code>double</code> value to be scaled
     * @return <code>int</code> value after scaling
     */
    protected int getScaledPixelAsInt(double value) {
        return (int) Math.round(getScaledPixel(value));
    }

}
