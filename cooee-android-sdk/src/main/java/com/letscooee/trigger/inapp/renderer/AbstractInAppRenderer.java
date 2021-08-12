package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.*;
import com.google.android.material.card.MaterialCardView;
import com.letscooee.BuildConfig;
import com.letscooee.R;
import com.letscooee.models.trigger.blocks.*;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.action.ClickActionExecutor;
import com.letscooee.trigger.inapp.InAppGlobalData;

import jp.wasabeef.blurry.Blurry;

import static com.letscooee.utils.Constants.TAG;

/**
 * @author Ashish Gaikwad 09/07/21
 * @since 1.0.0
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class AbstractInAppRenderer implements InAppRenderer {

    protected final InAppGlobalData globalData;
    protected final Context context;
    protected final ViewGroup parentElement;
    protected final BaseElement elementData;

    protected final MaterialCardView materialCardView;
    protected final RelativeLayout parentLayoutOfNewElement;
    protected final ImageView backgroundImage;

    /**
     * The newest element that will be rendered by the instance of this renderer.
     */
    protected View newElement;

    protected AbstractInAppRenderer(Context context, ViewGroup parentElement, BaseElement element,
                                    InAppGlobalData globalData) {
        this.context = context;
        this.parentElement = parentElement;
        this.elementData = element;
        this.globalData = globalData;

        this.materialCardView = new MaterialCardView(context);
        // First and only element inside MaterialCardView
        this.parentLayoutOfNewElement = new RelativeLayout(context);
        this.backgroundImage = new ImageView(context);
        this.setupWrapperForNewElement();
    }

    /**
     * To achieve background image, border stroke or corner radius for all the elements (container/layer/group/text
     * etc.), we need to wrap every element in the following hierarchy-
     *
     * <pre>
     *     |--- {@link #parentElement}
     *              |
     *              |--- {@link #materialCardView}
     *                      |
     *                      |--- {@link #parentLayoutOfNewElement} Relative Layout
     *                              |
     *                              |--- {@link #backgroundImage} Image View for Glossy/solid/image
     *                              |--- {@link #newElement} Our new element being inserted by this class. This can
     *                                      be {@link RelativeLayout}/{@link FlexboxLayout}/TextView/Button
     *
     * </pre>
     */
    private void setupWrapperForNewElement() {
        parentLayoutOfNewElement.addView(backgroundImage);
        materialCardView.addView(parentLayoutOfNewElement);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        parentLayoutOfNewElement.setLayoutParams(layoutParams);

        backgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (parentElement instanceof FlexboxLayout
                && elementData.getPosition().getType() == Position.PositionType.ABSOLUTE) {
            ((RelativeLayout) parentElement.getParent()).addView(materialCardView);
        } else {
            parentElement.addView(materialCardView);
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Parent " + parentElement.getClass().getSimpleName());
        }
    }

    protected void processCommonBlocks() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Process blocks for " + elementData.getClass().getSimpleName());
        }

        this.newElement.setTag(this.getClass().getSimpleName());
        this.processBackground();
        this.processOverFlow();
        this.processBorderBlock();
        this.processShadowBlock();
        this.registerListenerOnParentElement();
        this.processTransformBlock();
        this.processClickBlock();
        this.applyFlexParentProperties();
        this.applyFlexItemProperties();
    }

    private void processOverFlow() {
        if (elementData.getOverflow()==null){
            return;
        }
        materialCardView.setClipChildren(false);
        materialCardView.setClipToOutline(false);
        parentLayoutOfNewElement.setClipChildren(false);
        parentLayoutOfNewElement.setClipToOutline(false);
        parentElement.setClipChildren(false);
        parentElement.setClipToOutline(false);

    }

    protected void insertNewElementInHierarchy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Inserting new element " + newElement.getClass().getSimpleName());
        }

        this.parentLayoutOfNewElement.addView(newElement);
    }

    protected void applyFlexItemProperties() {
        if (!(this.parentElement instanceof FlexboxLayout)) {
            return;
        }

        if (elementData.getPosition().getType() == Position.PositionType.ABSOLUTE) {
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
            materialCardView.setElevation(0);
            return;
        }

        // TODO: 26/07/21 Use shadow
        materialCardView.setElevation(2);
    }

    protected void processClickBlock() {
        ClickAction clickAction = elementData.getClickAction();
        if (clickAction == null) return;

        newElement.setOnClickListener(v -> new ClickActionExecutor(context, clickAction, globalData).execute());
    }

    protected void processTransformBlock() {
        Transform transform = elementData.getTransform();
        if (transform == null) return;

        newElement.setRotation(transform.getRotate());
    }

    protected void processSizeBlock() {
        final Size size = elementData.getSize();
        ViewGroup.MarginLayoutParams layoutParams;

        int width, height;
        if (size.getDisplay() == Size.Display.BLOCK || size.getDisplay() == Size.Display.FLEX) {
            width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        height = ViewGroup.LayoutParams.WRAP_CONTENT;

        Integer calculatedWidth = size.getCalculatedWidth(parentElement);
        if (calculatedWidth != null) {
            width = calculatedWidth;
        }

        Integer calculatedHeight = size.getCalculatedHeight(parentElement);
        if (calculatedHeight != null) {
            height = calculatedHeight;
        }

        this.newElement.setLayoutParams(new RelativeLayout.LayoutParams(width, height));

        Position.PositionType positionType = elementData.getPosition().getType();

        if (parentElement instanceof FlexboxLayout && positionType != Position.PositionType.ABSOLUTE) {
            layoutParams = new FlexboxLayout.LayoutParams(width, height);
        } else if (parentElement instanceof FlexboxLayout) {
            layoutParams = new RelativeLayout.LayoutParams(width, height);
        } else if (parentElement instanceof RelativeLayout) {
            layoutParams = new RelativeLayout.LayoutParams(width, height);
        } else if (parentElement instanceof LinearLayout) {
            layoutParams = new LinearLayout.LayoutParams(width, height);
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
        });
    }

    private void registerListenerOnNewElement() {
        newElement.addOnLayoutChangeListener((v1, left1, top1, right1, bottom1, oldLeft1, oldTop1, oldRight1, oldBottom1) -> {
            this.processMaxSize();
            this.processSpacing();
            // Position calculation should be done after size and spacing are
            // applied to the new element
            this.applyPositionBlock();
        });
    }

    private void processMaxSize() {
        final Size size = elementData.getSize();
        ViewGroup.LayoutParams layoutParams = newElement.getLayoutParams();

        int currentWidth = newElement.getMeasuredWidth();
        int currentHeight = newElement.getMeasuredHeight();

        Integer maxWidth = size.getCalculatedMaxWidth(parentElement);
        if (maxWidth != null && maxWidth < currentWidth) {
            layoutParams.width = maxWidth;
        }

        Integer maxHeight = size.getCalculatedMaxHeight(parentElement);
        if (maxHeight != null && maxHeight < currentHeight) {
            layoutParams.height = maxHeight;
        }

        newElement.setLayoutParams(layoutParams);
    }

    private void applyPositionBlock() {
        Position position = this.elementData.getPosition();
        if (position == null || !position.isNonStatic()) {
            return;
        }

        int top = position.getTop(parentElement);
        int bottom = position.getBottom(parentElement);
        int left = position.getLeft(parentElement);
        int right = position.getRight(parentElement);

        float parentX = parentElement.getX();
        float parentY = parentElement.getY();
        float parentHeight = parentElement.getMeasuredHeight();
        float parentWidth = parentElement.getMeasuredWidth();

        float currentHeight = materialCardView.getMeasuredHeight();
        float currentWidth = materialCardView.getMeasuredWidth();

        if (top != 0) {
            materialCardView.setY(top);
        }
        if (left != 0) {
            materialCardView.setX(left);
        }
        if (bottom != 0) {
            float parentBottom = parentY + parentHeight;
            float currentViewBottom = parentBottom - bottom;
            materialCardView.setY(currentViewBottom - currentHeight);
        }
        if (right != 0) {
            float parentRight = parentX + parentWidth;
            float currentViewRight = parentRight - right;
            materialCardView.setX(currentViewRight - currentWidth);
        }
    }

    protected void processBackground() {
        Background background = elementData.getBg();
        materialCardView.setCardBackgroundColor(Color.parseColor("#00ffffff"));

        if (background != null) {
            if (background.getSolid() != null) {
                processSolidBackground(background.getSolid());

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
            blurryComposer.color(glossy.getColor().getSolidColor());

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
            if (border.getColor() != null)
                materialCardView.setStrokeColor(border.getColor().getSolidColor());
            else
                materialCardView.setStrokeColor(context.getResources().getColor(R.color.colorPrimary));

            Integer calculatedBorder = border.getWidth(parentElement);
            if (calculatedBorder != null) {
                materialCardView.setStrokeWidth(calculatedBorder);
            }

            if (border.getRadius() > 0) {
                materialCardView.setRadius(border.getRadius());
            }
        }
    }

    private void processSolidBackground(Colour solid) {
        GradientDrawable drawable;
        if (solid.getGrad() == null) {
            drawable = new GradientDrawable();
            drawable.setColor(solid.getSolidColor());
        } else {
            drawable = solid.getGrad().getGradient();
        }

        materialCardView.setBackground(drawable);
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

        ((ViewGroup.MarginLayoutParams) this.materialCardView.getLayoutParams())
                .setMargins(marginLeft, marginTop, marginRight, marginBottom);

        int paddingLeft = spacing.getPaddingLeft(parentElement);
        int paddingRight = spacing.getPaddingRight(parentElement);
        int paddingTop = spacing.getPaddingTop(parentElement);
        int paddingBottom = spacing.getPaddingBottom(parentElement);

        this.newElement.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    private void applyFlexParentProperties() {
        if (!(newElement instanceof FlexboxLayout)) {
            return;
        }

        Size size = elementData.getSize();
        FlexboxLayout layout = (FlexboxLayout) newElement;

        layout.setFlexDirection(size.getDirection());
        layout.setFlexWrap(size.getWrap());
        layout.setJustifyContent(size.getJustifyContent());
        layout.setAlignItems(size.getAlignItems());
        layout.setAlignContent(size.getAlignContent());
    }
}
