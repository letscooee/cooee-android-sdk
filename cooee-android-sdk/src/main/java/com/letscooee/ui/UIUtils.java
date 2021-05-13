package com.letscooee.ui;

import android.view.ViewGroup;

import com.letscooee.models.DataBlock;

public class UIUtils {

    public static ViewGroup.LayoutParams setHeightWidth(DataBlock data) {

        ViewGroup.LayoutParams layoutParams;
        if (data.getWidth() != null && data.getHeight() != null) {
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
