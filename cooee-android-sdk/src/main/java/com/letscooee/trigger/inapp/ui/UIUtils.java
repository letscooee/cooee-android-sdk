package com.letscooee.trigger.inapp.ui;

import android.util.Log;
import android.view.ViewGroup;

import com.letscooee.models.DataBlock;
import com.letscooee.utils.Constants;

public class UIUtils {

    public static ViewGroup.LayoutParams setHeightWidth(DataBlock data) {

        ViewGroup.LayoutParams layoutParams;
        if (data.getFillType() == DataBlock.FillType.FILL || data.getFillType() == DataBlock.FillType.COVER) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else if (data.getWidth() != null && data.getHeight() != null) {
            Log.d(Constants.TAG, "setHeightWidth 15: " + Math.round(data.getWidth()) + "::" + Math.round(data.getHeight()));
            layoutParams = new ViewGroup.LayoutParams(((int) Math.round(data.getWidth())), ((int) Math.round(data.getHeight())));
        } else if (data.getWidth() != null && data.getHeight() == null) {
            layoutParams = new ViewGroup.LayoutParams(((int) Math.round(data.getWidth())), ViewGroup.LayoutParams.WRAP_CONTENT);
        } else if (data.getWidth() == null && data.getHeight() != null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ((int) Math.round(data.getHeight())));
        } else {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return layoutParams;

    }
}