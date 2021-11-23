package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.PartElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.trigger.inapp.TriggerContext;

/**
 * @author shashank
 */
public class TextRenderer extends FontRenderer {

    protected Object textData;
    protected TextElement commonTextData;

    public TextRenderer(Context context, ViewGroup parentView, Object elementData, TriggerContext globalData) {
        super(context, parentView, (BaseElement) elementData, globalData);
        this.textData = elementData;
    }

    public TextRenderer(Context context, ViewGroup parentView, Object elementData,
                        TriggerContext globalData, TextElement commonTextData) {
        super(context, parentView, commonTextData, globalData);
        this.textData = elementData;
        this.commonTextData = commonTextData;
    }

    @Override
    public View render() {
        if (textData instanceof TextElement) {
            this.processParts();
        } else {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            this.processTextData(textView);
            this.processFont();
            this.processPartStyle();
        }

        return newElement;
    }

    protected void processParts() {
        newElement = new LinearLayout(context);
        ((LinearLayout) newElement).setGravity(Gravity.CENTER);
        insertNewElementInHierarchy();
        processCommonBlocks();

        for (PartElement child : ((TextElement) textData).getParts()) {
            new TextRenderer(context, (ViewGroup) newElement, child, globalData, (TextElement) textData).render();
        }
    }

    private void processPartStyle() {
        TextView textView = (TextView) newElement;
        Typeface typeface = textView.getTypeface();
        PartElement partElement = (PartElement) textData;

        if (partElement.isBold() && partElement.isItalic()) {
            typeface = Typeface.create(typeface, Typeface.BOLD_ITALIC);
        } else if (partElement.isBold()) {
            typeface = Typeface.create(typeface, Typeface.BOLD);
        } else if (partElement.isItalic()) {
            typeface = Typeface.create(typeface, Typeface.ITALIC);
        }

        textView.setTypeface(typeface);
    }

    protected void processTextData(TextView textView) {
        textView.setText(((PartElement) textData).getText());
        this.newElement = textView;

        this.processFontBlock();
        this.processColourBlock();
        this.processAlignmentBlock();

        insertNewElementInHierarchy();
    }

    protected void processAlignmentBlock() {
        if (commonTextData != null) {
            ((TextView) newElement).setGravity(commonTextData.getAlignment());
        }
    }

    protected void processColourBlock() {
        if (commonTextData != null && commonTextData.getColor() != null) {
            ((TextView) newElement).setTextColor(commonTextData.getColor().getHexColor());
        }

        PartElement partElement = (PartElement) textData;
        if (partElement.getPartTextColour() == null) {
            return;
        }

        ((TextView) newElement).setTextColor(partElement.getPartTextColour());
    }

    protected void processFontBlock() {
        if (commonTextData != null && commonTextData.getFont() != null) {
            ((TextView) newElement).setTextSize(commonTextData.getFont().getSize());
        }
    }
}