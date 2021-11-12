package com.letscooee.trigger.inapp.renderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import androidx.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.card.MaterialCardView;
import com.letscooee.BuildConfig;
import com.letscooee.models.trigger.blocks.*;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.action.ClickActionExecutor;
import com.letscooee.trigger.inapp.TriggerContext;

import jp.wasabeef.blurry.Blurry;

import static com.letscooee.utils.Constants.TAG;

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
     *                                      be {@link RelativeLayout}/{@link FlexboxLayout}/TextView/Button
     *
     * </pre>
     */
    private void setupWrapperForNewElement() {
        baseFrameLayout.addView(backgroundImage);
        materialCardView.addView(baseFrameLayout);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(MP, MP);
        baseFrameLayout.setLayoutParams(layoutParams);
        backgroundImage.setLayoutParams(layoutParams);

        backgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        reassignParentIfAbsolute();

        // Adds MaterialCardView to the base RelativeLayout of the In-App
        // if position is ABSOLUTE
        if (elementData.isAbsolute()) {
            globalData.getTriggerParentLayout().addView(materialCardView);
        } else {
            parentElement.addView(materialCardView);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Parent " + parentElement.getClass().getSimpleName());
        }
    }

    /**
     * Check if element's position is {@link com.letscooee.enums.trigger.PositionType#FREE_FLOATING}
     * then re-initialize {@link #parentElement} with parent of {@link #parentElement}.
     * If {@link #newElement} is in {@link FlexboxLayout} overlapping of the element is not possible
     * Hence accessing parent of {@link #parentElement} and placing {@link #newElement} in it.
     */
    private void reassignParentIfAbsolute() {
        if (elementData.isAbsolute()) {
            parentElement = (FrameLayout) parentElement.getParent();
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
        this.processShadowBlock();
        //this.registerListenerOnParentElement();
        this.processTransformBlock();
        this.processClickBlock();
        this.applyFlexItemProperties();
    }

    protected void insertNewElementInHierarchy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Inserting new element " + newElement.getClass().getSimpleName());
        }

        this.baseFrameLayout.addView(newElement);
    }

    protected void applyFlexItemProperties() {
        if (!(this.parentElement instanceof FlexboxLayout)) {
            return;
        }

        if (elementData.isAbsolute()) {
            return;
        }

        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) materialCardView.getLayoutParams();

        if (elementData.getFlexGrow() != null) lp.setFlexGrow(elementData.getFlexGrow());
        if (elementData.getFlexShrink() != null) lp.setFlexShrink(elementData.getFlexShrink());
        if (elementData.getFlexOrder() != null) lp.setOrder(elementData.getFlexOrder());

        materialCardView.setLayoutParams(lp);
    }

    protected void processShadowBlock() {
        Shadow shadow = this.elementData.getShadow();
        if (shadow == null) {
            materialCardView.setCardElevation(0);
            return;
        }

        materialCardView.setCardElevation(shadow.getElevation());
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

        Integer calculatedWidth = elementData.getCalculatedWidth(parentElement);
        if (calculatedWidth != null) {
            width = calculatedWidth;
        }

        Integer calculatedHeight = elementData.getCalculatedHeight(parentElement);
        if (calculatedHeight != null) {
            height = calculatedHeight;
        }

        this.newElement.setLayoutParams(new FrameLayout.LayoutParams(width, height));

        if (parentElement instanceof RelativeLayout || elementData.isAbsolute()) {
            layoutParams = new RelativeLayout.LayoutParams(width, height);
        } else if (parentElement instanceof FlexboxLayout) {
            layoutParams = new FlexboxLayout.LayoutParams(width, height);
        } else if (parentElement instanceof LinearLayout) {
            layoutParams = new LinearLayout.LayoutParams(width, height);
        } else if (parentElement instanceof FrameLayout) {
            layoutParams = new FrameLayout.LayoutParams(width, height);
        } else {
            throw new RuntimeException("Unknown type of parentElement- " + parentElement);
        }

        this.materialCardView.setLayoutParams(layoutParams);
    }

    /**
     * Directly applying absolute position parameters (top, left, right, bottom) does not work. So to achieve that,
     * adding an observer to the new view to check once the view is rendered on a screen and then
     * apply position to that view.
     */
    protected void registerListenerOnParentElement() {
        parentElement.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            boolean layoutChanged =
                    // Check if height changed
                    (bottom - top) != (oldBottom - oldTop) ||
                            // Check if width changed
                            (right - left) != (oldRight - oldLeft);

            if (layoutChanged) {
                this.processSizeBlock();
                this.registerListenerOnNewElement();
            }
            this.applyPositionBlock();
        });
    }

    private void registerListenerOnNewElement() {
        newElement.addOnLayoutChangeListener((v1, left1, top1, right1, bottom1, oldLeft1, oldTop1, oldRight1, oldBottom1) -> {
            this.processSpacing();
            // Position calculation should be done after size and spacing are
            // applied to the new element
            this.applyPositionBlock();
        });
    }

    private void applyPositionBlock() {
        if (!elementData.isAbsolute()) {
            return;
        }

        float top = elementData.getY();
        float left = elementData.getX();

        // get the onscreen location of parent element as getX, getY return 0 always
        /*int[] location = new int[2];
        parentElement.getLocationOnScreen(location);

        float parentX = location[0];
        float parentY = location[1] - getTitleBarHeight();

        float elementX = parentX;
        float elementY = parentY;
        if (top != 0) {
            elementY += top;
        }
        if (left != 0) {
            elementX += left;
        }*/

        materialCardView.setX(left);
        materialCardView.setY(top);

        Integer zIndex = elementData.getZ();

        if (zIndex == null) {
            materialCardView.setTranslationZ(0);
            return;
        }

        materialCardView.setTranslationZ(zIndex);
    }

    /**
     * Finds TitleBar height of the device
     *
     * @return <code>int</code> height of the TitleBar
     */
    private float getTitleBarHeight() {
        Rect rectangle = new Rect();
        Window window = ((Activity) context).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        float contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return rectangle.top - contentViewTop;
    }

    protected void processBackground() {
        Background background = elementData.getBg();
        materialCardView.setCardBackgroundColor(Color.TRANSPARENT);

        if (background != null) {
            if (background.getSolid() != null) {
                background.getSolid().updateDrawable(backgroundDrawable);

            } else if (background.getGlossy() != null) {
                applyGlassmorphism(background.getGlossy());

            } else if (background.getImage() != null) {
                Glide.with(context).asBitmap().load(background.getImage().getUrl()).into(backgroundImage);
            }
        }
    }

    private void applyGlassmorphism(Glossy glossy) {
        Blurry.Composer blurryComposer = Blurry.with(context)
                .animate(500);

        blurryComposer.radius(glossy.getRadius());
        blurryComposer.sampling(glossy.getSampling());

        if (glossy.getColor() != null)
            blurryComposer.color(glossy.getColor().getHexColor());

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

        if (border != null) {
            int borderColor = border.getColor().getHexColor();
            materialCardView.setStrokeColor(borderColor);

            Integer calculatedBorder = border.getWidth(parentElement);
            if (calculatedBorder != null) {
                materialCardView.setStrokeWidth(calculatedBorder);
                backgroundDrawable.setStroke(calculatedBorder, borderColor);
            }

            int calculatedRadius = border.getRadius();
            if (calculatedRadius > 0) {
                backgroundDrawable.setCornerRadius(calculatedRadius);
                materialCardView.setRadius(calculatedRadius);
            }
        }
    }

    protected void processSpacing() {
        Spacing spacing = elementData.getSpacing();
        if (spacing == null) {
            return;
        }

        spacing.calculatedPaddingAndMargin(parentElement);

        int marginLeft = spacing.getMarginLeft(parentElement);
        int marginRight = spacing.getMarginRight(parentElement);
        int marginTop = spacing.getMarginTop(parentElement);
        int marginBottom = spacing.getMarginBottom(parentElement);

        ViewGroup.MarginLayoutParams layoutParams = ((ViewGroup.MarginLayoutParams) this.materialCardView.getLayoutParams());
        layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
        materialCardView.setLayoutParams(layoutParams);

        int paddingLeft = spacing.getPaddingLeft(parentElement);
        int paddingRight = spacing.getPaddingRight(parentElement);
        int paddingTop = spacing.getPaddingTop(parentElement);
        int paddingBottom = spacing.getPaddingBottom(parentElement);

        this.newElement.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }
}
