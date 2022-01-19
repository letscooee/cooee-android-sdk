package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

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
        newElement.addOnLayoutChangeListener(
                (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                        backgroundImage.setLayoutParams(new FrameLayout.LayoutParams(right, bottom))
        );

        // Resize element to adjust with border
        if (elementData.getBorder() != null) {
            int calculatedBorder = (int) elementData.getBorder().getWidth(parentElement);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) newElement.getLayoutParams();
            layoutParams.width -= calculatedBorder * 2;
            layoutParams.height -= calculatedBorder * 2;
            newElement.setLayoutParams(layoutParams);
            materialCardView.setContentPadding(calculatedBorder, calculatedBorder, calculatedBorder, calculatedBorder);
        }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED);
        }

        this.newElement = textView;

        this.processColourBlock();
        this.processAlignmentBlock();

        insertNewElementInHierarchy();
        processCommonBlocks();
    }

    protected void processAlignmentBlock() {
        ((TextView) newElement).setGravity((textData).getAlignment());
    }

    protected void processColourBlock() {
        ((TextView) newElement).setTextColor((textData).getColor().getHexColor());
    }

    protected void processFontBlock() {
        ((TextView) newElement).setTextSize(TypedValue.COMPLEX_UNIT_PX, textData.getFont().getSize());
    }
}