package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.letscooee.CooeeFactory;
import com.letscooee.models.trigger.blocks.Alignment;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.trigger.inapp.InAppGlobalData;

/**
 * @author shashank
 */
public class TextRenderer extends AbstractInAppRenderer {

    protected final TextElement textData;

    public TextRenderer(Context context, ViewGroup parentView, BaseElement elementData, InAppGlobalData globalData) {
        super(context, parentView, elementData, globalData);
        this.textData = (TextElement) elementData;
    }

    @Override
    public View render() {
        TextView textView = new TextView(context);
        this.processTextData(textView);

        return newElement;
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
        Alignment alignment = textData.getAlignment();
        if (alignment == null) {
            return;
        }

        if (alignment.getAlign() == Alignment.Align.LEFT) {
            ((TextView) newElement).setGravity(Gravity.START);
        } else if (alignment.getAlign() == Alignment.Align.RIGHT) {
            ((TextView) newElement).setGravity(Gravity.END);
        } else if (alignment.getAlign() == Alignment.Align.CENTER) {
            ((TextView) newElement).setGravity(Gravity.CENTER);
        }
    }

    protected void processColourBlock() {
        if (textData.getColor() == null) {
            return;
        }

        ((TextView) newElement).setTextColor(textData.getColor().getSolidColor());
    }

    protected void processFontBlock() {
        if (textData.getFont() == null) {
            return;
        }

        float fontSizeInSP = textData.getFont().getSize() / CooeeFactory.getDeviceInfo().getScaledDensity();
        ((TextView) newElement).setTextSize(fontSizeInSP);
    }
}