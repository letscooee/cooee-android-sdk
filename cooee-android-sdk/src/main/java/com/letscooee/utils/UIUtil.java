package com.letscooee.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.card.MaterialCardView;
import com.letscooee.R;
import com.letscooee.models.v3.block.Background;
import com.letscooee.models.v3.block.Border;
import com.letscooee.models.v3.block.Color;
import com.letscooee.models.v3.block.Glossy;
import com.letscooee.models.v3.block.Position;
import com.letscooee.models.v3.block.Size;
import com.letscooee.models.v3.block.Spacing;
import com.letscooee.models.v3.elemeent.Children;
import com.letscooee.models.v3.inapp.Container;
import com.letscooee.models.v3.inapp.Layers;

import jp.wasabeef.blurry.Blurry;

import static android.text.TextUtils.isEmpty;

/**
 * @author Ashish Gaikwad 09/07/21
 * @since 1.0.0
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class UIUtil {
    private final Context context;
    private final int deviceHeight;
    private final int deviceWidth;

    private OnImageLoad onImageLoad;

    public interface OnImageLoad {
        void onImageLoad(BitmapDrawable drawable);
    }

    public void setOnImageLoad(OnImageLoad onImageLoad) {
        this.onImageLoad = onImageLoad;
    }

    public UIUtil(Context context) {
        this.context = context;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
    }

    public ViewGroup.MarginLayoutParams generateLayoutParams(Size size, Position position) {

        ViewGroup.MarginLayoutParams layoutParams;

        if (size != null) {
            if (size.getDisplay() == Size.Display.BLOCK) {
                layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            } else if (size.getDisplay() == Size.Display.FLEX) {
                layoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                int calculatedHeight = size.getCalculatedHeight(deviceWidth, deviceHeight);
                int calculatedWidth = size.getCalculatedWidth(deviceWidth, deviceHeight);

                if (calculatedHeight == 0 && calculatedWidth == 0)
                    layoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                else if (calculatedHeight == 0 && calculatedWidth > 0)
                    layoutParams = new FlexboxLayout.LayoutParams(calculatedWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                else if (calculatedHeight > 0 && calculatedWidth == 0)
                    layoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, calculatedHeight);
                else
                    layoutParams = new FlexboxLayout.LayoutParams(calculatedWidth, calculatedHeight);

            } else {
                int calculatedHeight = size.getCalculatedHeight(deviceWidth, deviceHeight);
                int calculatedWidth = size.getCalculatedWidth(deviceWidth, deviceHeight);

                if (calculatedHeight == 0 && calculatedWidth == 0)
                    layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                else if (calculatedHeight == 0 && calculatedWidth > 0)
                    layoutParams = new RelativeLayout.LayoutParams(calculatedWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                else if (calculatedHeight > 0 && calculatedWidth == 0)
                    layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, calculatedHeight);
                else
                    layoutParams = new RelativeLayout.LayoutParams(calculatedWidth, calculatedHeight);
            }

        } else {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }


        return layoutParams;
    }

    public void processPosition(Position position, View currentView, View parent,
                                Object parentProperty) {
        if (position == null) {
            return;
        }

        int top = position.getTop(deviceWidth, deviceHeight);
        int bottom = position.getBottom(deviceWidth, deviceHeight);
        int left = position.getLeft(deviceWidth, deviceHeight);
        int right = position.getRight(deviceWidth, deviceHeight);

        float parentX = parent.getX();
        float parentY = parent.getY();
        float parentHeight = parent.getMeasuredHeight();
        float parentWidth = parent.getMeasuredWidth();

        float currentHeight = currentView.getMeasuredHeight();
        float currentWidth = currentView.getMeasuredWidth();

        if (top != 0) {
            currentView.setY(top);
            //currentView.setTop(top);
        }
        if (left != 0) {
            currentView.setX(left);
            //currentView.setLeft(left);

        }
        if (bottom != 0) {
            float parentBottom = parentY + parentHeight;
            float currentViewBottom = parentBottom - bottom;
            currentView.setY(currentViewBottom - currentHeight);
            //currentView.setBottom(bottom);
        }
        if (right != 0) {
            float parentRight = parentX + parentWidth;
            float currentViewRight = parentRight - right;
            currentView.setX(currentViewRight - currentWidth);
            //currentView.setRight(right);
        }

    }

    @Nullable
    public MaterialCardView processBackground(Object container, Object imageContainer) {
        Background background;
        Border border;
        ImageView imageView = new ImageView(context);
        MaterialCardView materialCardView = new MaterialCardView(context);
        materialCardView.setCardBackgroundColor(android.graphics.Color.parseColor("#00ffffff"));

        if (container instanceof Container) {
            background = ((Container) container).getBg();
            border = ((Container) container).getBorder();
        } else if (container instanceof Children) {
            background = ((Children) container).getBg();
            border = ((Children) container).getBorder();
        } else {
            background = ((Layers) container).getBg();
            border = ((Layers) container).getBorder();
        }

        if (background != null) {
            if (background.getSolid() != null) {

                processesSolid(imageView, background.getSolid(), border);
                if (border != null)
                    addBorder(materialCardView, imageView, border);
            } else if (background.getGlossy() != null) {

                processesGlassMorphism(imageView, background.getGlossy(),
                        border, imageContainer);
                if (border != null) {

                    addBorder(materialCardView, imageView, border);
                    //imageView.setBackground(drawable);
                }

            } else if (background.getImage() != null) {

                Glide.with(context).asBitmap().load(background.getImage().getUrl()).into(imageView);
                addBorder(materialCardView, imageView, border);
            }
        } else {

            addBorder(materialCardView, imageView, border);

        }
        if (background == null && border == null) {
            return null;
        }
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.addView(imageView);
        materialCardView.addView(relativeLayout);
        return materialCardView;
    }

    private void processesGlassMorphism(ImageView imageView, Glossy glossy,
                                        Border border, Object imageContainer) {

        Blurry.Composer blurryComposer = Blurry.with(context)
                .animate(500);

        if (glossy.getRadius() != 0)
            blurryComposer.radius(glossy.getRadius());
        if (glossy.getColor() != null)
            blurryComposer.color(glossy.getColor().getSolidColor());
        if (glossy.getSampling() != 0)
            blurryComposer.sampling(glossy.getSampling());


        if (imageContainer instanceof Bitmap) {
            blurryComposer
                    .from((Bitmap) imageContainer)
                    .into(imageView);
        } else {
            blurryComposer
                    .capture((ViewGroup) imageContainer)
                    .into(imageView);
        }

    }

    // Removed as it was duplicate code
    /*private void processesSolidWithBorder(Drawable drawable, Color color, Border border) {
        //GradientDrawable drawable;

        if (color.getGrad() == null) {
            ((GradientDrawable) drawable).setColor(color.getSolidColor());
        } else {
            drawable = color.getGrad().getGradient();
        }

        addBorder(drawable, border);
        //imageView.setBackground(drawable);
    }*/

    private void addBorder(MaterialCardView materialCardView, ImageView imageView, Border border) {
        if (border != null) {
            GradientDrawable drawable = new GradientDrawable();

            if (border.getColor() != null)
                materialCardView.setStrokeColor(border.getColor().getSolidColor());
            else
                materialCardView.setStrokeColor(context.getResources().getColor(R.color.colorPrimary));

            if (border.getWidth(deviceWidth, deviceHeight) != 0) {
                materialCardView.setStrokeWidth(border.getWidth(deviceWidth, deviceHeight));
            }

            if (border.getRadius() > 0) {
                materialCardView.setRadius(border.getRadius());
            }

        }

    }

    private void processesSolid(ImageView imageView, Color solid, Border border) {
        GradientDrawable drawable = new GradientDrawable();
        if (solid.getGrad() == null) {
            drawable.setColor(solid.getSolidColor());
        } else {
            drawable = solid.getGrad().getGradient();
        }
        imageView.setImageDrawable(drawable);

    }

    public void processSpacing(View view, Spacing spacing) {
        if (spacing != null) {
            int margin = spacing.getMargin(deviceWidth, deviceHeight);
            int marginLeft = spacing.getMarginLeft(deviceWidth, deviceHeight);
            int marginRight = spacing.getMarginRight(deviceWidth, deviceHeight);
            int marginTop = spacing.getMarginTop(deviceWidth, deviceHeight);
            int marginBottom = spacing.getMarginBottom(deviceWidth, deviceHeight);
            if (margin > 0) {
                ((RelativeLayout.LayoutParams) view.getLayoutParams()).setMargins(margin, margin, margin, margin);
            } else if (marginTop > 0 || marginBottom > 0 || marginLeft > 0 || marginRight > 0) {

                ((RelativeLayout.LayoutParams) view.getLayoutParams())
                        .setMargins(marginLeft, marginTop, marginRight, marginBottom);
            }

            int padding = spacing.getPadding(deviceWidth, deviceHeight);
            int paddingLeft = spacing.getPaddingLeft(deviceWidth, deviceHeight);
            int paddingRight = spacing.getPaddingRight(deviceWidth, deviceHeight);
            int paddingTop = spacing.getPaddingTop(deviceWidth, deviceHeight);
            int paddingBottom = spacing.getPaddingBottom(deviceWidth, deviceHeight);
            if (padding > 0) {
                view.setPadding(padding, padding, padding, padding);
            } else if (paddingTop > 0 || paddingBottom > 0 || paddingLeft > 0 || paddingRight > 0) {
                view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            }
        }
    }

    public void addFlexProperty(FlexboxLayout layout, Size size) {

        if (size == null)
            return;
        if (!isEmpty(size.getDirection())) {
            if (size.getDirection().equalsIgnoreCase("row")) {
                layout.setFlexDirection(FlexDirection.ROW);
            } else if (size.getDirection().equalsIgnoreCase("column")) {
                layout.setFlexDirection(FlexDirection.COLUMN);
            } else if (size.getDirection().equalsIgnoreCase("row-reverse")) {
                layout.setFlexDirection(FlexDirection.ROW_REVERSE);
            } else if (size.getDirection().equalsIgnoreCase("column-reverse")) {
                layout.setFlexDirection(FlexDirection.COLUMN_REVERSE);
            }
        }
        if (!isEmpty(size.getWrap())) {
            if (size.getWrap().equalsIgnoreCase("wrap")) {
                layout.setFlexWrap(FlexWrap.WRAP);
            } else if (size.getWrap().equalsIgnoreCase("NOWRAP")) {
                layout.setFlexWrap(FlexWrap.NOWRAP);
            } else if (size.getWrap().equalsIgnoreCase("WRAP_REVERSE")) {
                layout.setFlexWrap(FlexWrap.WRAP_REVERSE);
            }
        }
        if (!isEmpty(size.getJustifyContent())) {
            if (size.getJustifyContent().equalsIgnoreCase("FLEX_START")) {
                layout.setJustifyContent(JustifyContent.FLEX_START);
            } else if (size.getJustifyContent().equalsIgnoreCase("FLEX_END")) {
                layout.setJustifyContent(JustifyContent.FLEX_END);
            } else if (size.getJustifyContent().equalsIgnoreCase("CENTER")) {
                layout.setJustifyContent(JustifyContent.CENTER);
            } else if (size.getJustifyContent().equalsIgnoreCase("SPACE_BETWEEN")) {
                layout.setJustifyContent(JustifyContent.SPACE_BETWEEN);
            } else if (size.getJustifyContent().equalsIgnoreCase("SPACE_AROUND")) {
                layout.setJustifyContent(JustifyContent.SPACE_AROUND);
            } else if (size.getJustifyContent().equalsIgnoreCase("SPACE_EVENLY")) {
                layout.setJustifyContent(JustifyContent.SPACE_EVENLY);
            }
        }

        if (!isEmpty(size.getAlignItems())) {
            if (size.getAlignItems().equalsIgnoreCase("FLEX_START")) {
                layout.setAlignItems(AlignItems.FLEX_START);
            } else if (size.getAlignItems().equalsIgnoreCase("FLEX_END")) {
                layout.setAlignItems(AlignItems.FLEX_END);
            } else if (size.getAlignItems().equalsIgnoreCase("CENTER")) {
                layout.setAlignItems(AlignItems.CENTER);
            } else if (size.getAlignItems().equalsIgnoreCase("BASELINE")) {
                layout.setAlignItems(AlignItems.BASELINE);
            } else if (size.getAlignItems().equalsIgnoreCase("STRETCH")) {
                layout.setAlignItems(AlignItems.STRETCH);
            }
        }

        if (!isEmpty(size.getAlignContent())) {
            if (size.getAlignContent().equalsIgnoreCase("FLEX_START")) {
                layout.setAlignContent(AlignContent.FLEX_START);
            } else if (size.getAlignContent().equalsIgnoreCase("FLEX_END")) {
                layout.setAlignContent(AlignContent.FLEX_END);
            } else if (size.getAlignContent().equalsIgnoreCase("CENTER")) {
                layout.setAlignContent(AlignContent.CENTER);
            } else if (size.getAlignContent().equalsIgnoreCase("SPACE_BETWEEN")) {
                layout.setAlignContent(AlignContent.SPACE_BETWEEN);
            } else if (size.getAlignContent().equalsIgnoreCase("SPACE_AROUND")) {
                layout.setAlignContent(AlignContent.SPACE_AROUND);
            } else if (size.getAlignContent().equalsIgnoreCase("STRETCH")) {
                layout.setAlignContent(AlignContent.STRETCH);
            }
        }
    }
}
