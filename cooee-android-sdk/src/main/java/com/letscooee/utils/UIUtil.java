package com.letscooee.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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

import org.jetbrains.annotations.NotNull;

import jp.wasabeef.blurry.Blurry;

import static android.text.TextUtils.isEmpty;
import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_END;
import static android.widget.RelativeLayout.ALIGN_PARENT_START;
import static android.widget.RelativeLayout.ALIGN_PARENT_TOP;
import static android.widget.RelativeLayout.CENTER_IN_PARENT;

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

    public RelativeLayout.LayoutParams generateLayoutParams(Size size, Position position) {

        RelativeLayout.LayoutParams layoutParams;

        if (size != null) {
            if (size.getDisplay() == Size.Display.BLOCK) {
                layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            } else if (size.getDisplay() == Size.Display.FLEX) {
                layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
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
            if (!TextUtils.isEmpty(size.getAlignItems()) || !TextUtils.isEmpty(size.getJustifyContent())) {
                if (size.getAlignItems().toLowerCase().equals("center")) {
                    layoutParams.addRule(CENTER_IN_PARENT);
                }
            }
        } else {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        processPosition(position, layoutParams);

        return layoutParams;
    }

    private void processPosition(Position position, RelativeLayout.LayoutParams layoutParams) {
        if (position != null) {
            int top = position.getTop(deviceWidth, deviceHeight);
            int bottom = position.getBottom(deviceWidth, deviceHeight);
            int left = position.getLeft(deviceWidth, deviceHeight);
            int right = position.getRight(deviceWidth, deviceHeight);


            if (top != 0 && bottom != 0 && left != 0 && right != 0) {
                layoutParams.addRule(CENTER_IN_PARENT);
            }
            if (top != 0) {
                layoutParams.addRule(ALIGN_PARENT_TOP);
            }
            if (bottom != 0) {
                layoutParams.addRule(ALIGN_PARENT_BOTTOM);
            }
            if (left != 0) {
                layoutParams.addRule(ALIGN_PARENT_START);
            }
            if (right != 0) {
                layoutParams.addRule(ALIGN_PARENT_END);
            }

            layoutParams.setMargins(left, top, right, bottom);
        }
    }

    @Nullable
    public ImageView processBackground(Object container, Object imageContainer) {
        Background background;
        Border border;
        ImageView imageView = new ImageView(context);

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

            } else if (background.getGlossy() != null) {

                processesGlassMorphism(imageView, background.getGlossy(),
                        border, imageContainer);

            } else if (background.getImage() != null) {

                Glide.with(context).asBitmap().load(background.getImage().getUrl()).into(imageView);
                addBorder(imageView, border);
            }
        } else {

            addBorder(imageView, border);

        }
        if (background == null && border == null) {
            imageView = null;
        }
        return imageView;
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


        if (border != null) {

            addBorder(imageView, border);
            //imageView.setBackground(drawable);
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

    private void addBorder(ImageView imageView, Border border) {
        if (border != null) {
            GradientDrawable drawable = new GradientDrawable();
            if (border.getStyle() == Border.Style.SOLID) {
                if (border.getColor() != null)
                    drawable.setStroke(border.getWidth(deviceWidth, deviceHeight),
                            border.getColor().getSolidColor());
                else
                    drawable.setStroke(border.getWidth(deviceWidth, deviceHeight),
                            context.getResources().getColor(R.color.colorPrimary));
            } else {
                drawable.setStroke(border.getWidth(deviceWidth, deviceHeight),
                        border.getColor().getSolidColor(),
                        border.getDashWidth(deviceWidth, deviceHeight),
                        border.getDashGap(deviceWidth, deviceHeight));
            }
            if (border.getRadius() > 0) {
                drawable.setCornerRadius(border.getRadius());
            }
            imageView.setBackground(drawable);
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

        if (border != null)
            addBorder(imageView, border);

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
