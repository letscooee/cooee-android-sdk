package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.letscooee.models.trigger.blocks.Border;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.PartElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.trigger.inapp.TriggerContext;

/**
 * @author shashank
 */
public class TextRenderer extends FontRenderer {

    protected TextElement textData;

    public TextRenderer(Context context, ViewGroup parentView, BaseElement elementData, TriggerContext globalData) {
        super(context, parentView, elementData, globalData);
        this.textData = (TextElement) elementData;
    }

    @Override
    public View render() {
        String textData = this.processParts();

        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);

        this.processTextData(textView, textData);
        this.processFont();
        this.processFontBlock();

        // resize background image when text view is updated/rendered.
        newElement.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                newElement.removeOnLayoutChangeListener(this);
                backgroundImage.setLayoutParams(new FrameLayout.LayoutParams(right, bottom));
            }
        });

        // Resize element to adjust with border
        Border border = elementData.getBorder();
        if (border == null) {
            return newElement;
        }

        int calculatedBorder = getScaledPixelAsInt(border.getWidth());
        baseFrameLayout.setPadding(calculatedBorder, calculatedBorder, calculatedBorder, 0);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) newElement.getLayoutParams();
        layoutParams.width -= calculatedBorder * 2;
        layoutParams.height -= calculatedBorder * 2;
        newElement.setLayoutParams(layoutParams);

        if (border.getStyle() != Border.Style.DASH) {
            return newElement;
        }
        int borderColor = border.getColor().getHexColor();
        float dashWidth = calculatedBorder * 2;

        int w = getScaledPixelAsInt(elementData.getWidth());
        baseFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(w, WC));

        GradientDrawable elementDrawable = new GradientDrawable();
        elementDrawable.setStroke(calculatedBorder, borderColor, dashWidth, calculatedBorder);
        elementDrawable.setCornerRadius(getScaledPixelAsFloat(border.getRadius()) - (calculatedBorder / 2));
        baseFrameLayout.setBackground(elementDrawable);

        return newElement;
    }

    protected String processParts() {
        String allText = "";

        for (PartElement child : (textData).getParts()) {
            String partText = child.getText();

            if (child.isBold()) {
                partText = "<b>" + partText + "</b>";
            }

            if (child.isItalic()) {
                partText = "<i>" + partText + "</i>";
            }

            if (child.isStrikeTrough()) {
                partText = "<strike>" + partText + "</strike>";
            }

            if (child.isUnderline()) {
                partText = "<u>" + partText + "</u>";
            }

            String color = child.getPartTextColour();
            if (color != null) {
                partText = "<font color='" + color + "'>" + partText + "</font>";
            }

            allText = allText.concat(partText);
        }

        return replaceNewLineToHTMLNewLine(allText);
    }

    private String replaceNewLineToHTMLNewLine(String text) {
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        return text.replaceAll("\n", "<br/>");
    }

    protected void processTextData(TextView textView, String text) {
        textView.setText(HtmlCompat.fromHtml(text, 0));

        // setBreakStrategy is exposed in Android API 23 and its parameters from LinearBreak
        // are exposed in Android API 29, Hence we need to check the API level
        // Textview.setBreakStrategy Ref.: https://developer.android.com/reference/android/widget/TextView#setBreakStrategy(int)
        // LinearBreak.BREAK_STRATEGY_BALANCED: https://developer.android.com/reference/android/graphics/text/LineBreaker#constants_1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            textView.setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED);
        }

        this.newElement = textView;

        this.processColourBlock();
        this.processAlignmentBlock();

        insertNewElementInHierarchy();
        processCommonBlocks();
    }

    protected void processAlignmentBlock() {
        int gravity = textData.getAlignment();

        // setJustificationMode is exposed in Android API 26, but required fags to make text justify
        // are exposed in LinearBreaker class with Android API 29. Hence, applying Justify alignment on
        // Android API 29 & onward
        // LinearBreaker Ref.: https://developer.android.com/reference/android/graphics/text/LineBreaker#constants_1
        // TextView.setJustificationMode() Ref.: https://developer.android.com/reference/android/widget/TextView#setJustificationMode(int)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && gravity == -1) {
            ((TextView) newElement).setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        } else {
            ((TextView) newElement).setGravity(gravity);
        }
    }

    protected void processColourBlock() {
        ((TextView) newElement).setTextColor((textData).getColor().getHexColor());
    }

    protected void processFontBlock() {
        ((TextView) newElement).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getScaledPixelAsFloat(textData.getFont().getSize()));
    }
}