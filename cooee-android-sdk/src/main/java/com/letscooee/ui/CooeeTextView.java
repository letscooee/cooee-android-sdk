package com.letscooee.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import com.letscooee.models.DataBlock;

/**
 * @author: Ashish Gaikwad on 10/5/21
 */
public class CooeeTextView {

    /**
     * Create a TextView with specific data passed in DataBlock
     *
     * @param context will be Context
     * @param data    will be DataBlock
     * @return Returns a TextView widget
     */
    public static TextView generateTextView(Context context, DataBlock data) {
        TextView textView = new TextView(context);

        textView.setText(data.getTextContent());
        switch (data.getTextStyle()) {
            case BOLD:
                textView.setTypeface(null, Typeface.BOLD);
                break;
            case Italic:
                textView.setTypeface(null, Typeface.ITALIC);
                break;
            case BOLD_ITALIC:
                textView.setTypeface(null, Typeface.BOLD_ITALIC);
                break;
            default:
                textView.setTypeface(null, Typeface.NORMAL);
                break;
        }

        if (data.getTextSize() != null) {
            textView.setTextSize(data.getTextSize());
        }

        if (!TextUtils.isEmpty(data.getColor())) {
            textView.setTextColor(Color.parseColor(data.getColor()));
        }

        ViewGroup.LayoutParams layoutParams = UIUtils.setHeightWidth(data);
        textView.setLayoutParams(layoutParams);

        if (data.getxPosition() != null) {
            textView.setX((data.getxPosition()).floatValue());
        }
        if (data.getyPosition() != null) {
            textView.setY((data.getyPosition()).floatValue());
        }

        return textView;
    }
}
