package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letscooee.models.trigger.blocks.Alignment;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.trigger.inapp.TriggerContext;

/**
 * @author shashank
 */
public class TextRenderer extends FontRenderer {

    protected TextElement textData;
    protected TextElement commonTextData;

    public TextRenderer(Context context, ViewGroup parentView, BaseElement elementData, TriggerContext globalData) {
        super(context, parentView, elementData, globalData);
        this.textData = (TextElement) elementData;
    }

    public TextRenderer(Context context, ViewGroup parentView, BaseElement elementData,
                        TriggerContext globalData, TextElement commonTextData) {
        super(context, parentView, elementData, globalData);
        this.textData = (TextElement) elementData;
        this.commonTextData = commonTextData;
    }

    @Override
    public View render() {
        if (textData.getParts() != null && !textData.getParts().isEmpty()) {
            this.processParts();
        } else {
            TextView textView = new TextView(context);
            this.processTextData(textView);
            this.processFont();
        }

        return newElement;
    }

    private void processParts() {
        newElement = new LinearLayout(context);
        insertNewElementInHierarchy();
        processCommonBlocks();

        for (BaseElement child : textData.getParts()) {
            new TextRenderer(context, (ViewGroup) newElement, child, globalData, textData).render();
        }
    }

    protected void processTextData(TextView textView) {
        textView.setText(textData.getText());
        this.newElement = textView;

        this.processFontBlock();
        this.processColourBlock();
        this.processAlignmentBlock();

        insertNewElementInHierarchy();
        processCommonBlocks();
    }

    protected void processAlignmentBlock() {
        if (commonTextData != null && commonTextData.getAlignment() != null) {
            ((TextView) newElement).setGravity(commonTextData.getAlignment().getAlign());
        }

        Alignment alignment = textData.getAlignment();
        if (alignment == null) {
            return;
        }

        ((TextView) newElement).setGravity(alignment.getAlign());
    }

    protected void processColourBlock() {
        if (commonTextData != null && commonTextData.getColor() != null) {
            ((TextView) newElement).setTextColor(commonTextData.getColor().getHexColor());
        }

        if (textData.getColor() == null) {
            return;
        }

        ((TextView) newElement).setTextColor(textData.getColor().getHexColor());
    }

    protected void processFontBlock() {
        if (commonTextData != null && commonTextData.getFont() != null) {
            ((TextView) newElement).setTextSize(commonTextData.getFont().getSize());
        }

        if (textData.getFont() == null) {
            return;
        }

        ((TextView) newElement).setTextSize(textData.getFont().getSize());
    }
}