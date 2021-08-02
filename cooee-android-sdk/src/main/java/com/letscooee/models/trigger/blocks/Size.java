package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import static com.letscooee.utils.ui.UnitUtils.getCalculatedValue;

public class Size implements Parcelable {

    // The default value constructor
    public Size() {
        this.display = Display.BLOCK;
    }

    protected Size(Parcel in) {
        width = in.readString();
        height = in.readString();
        maxWidth = in.readString();
        maxHeight = in.readString();
        justifyContent = in.readString();
        alignItems = in.readString();
        wrap = in.readString();
        alignContent = in.readString();
        direction = in.readString();
        display = Display.valueOf(in.readString());
    }

    public static final Creator<Size> CREATOR = new Creator<Size>() {
        @Override
        public Size createFromParcel(Parcel in) {
            return new Size(in);
        }

        @Override
        public Size[] newArray(int size) {
            return new Size[size];
        }
    };

    public String getJustifyContent() {
        return justifyContent;
    }

    public String getAlignItems() {
        return alignItems;
    }

    public Integer getCalculatedHeight(View parent) {
        return getCalculatedValue(parent, height, true);
    }

    public Integer getCalculatedWidth(View parent) {
        return getCalculatedValue(parent, width);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(width);
        dest.writeString(height);
        dest.writeString(maxWidth);
        dest.writeString(maxHeight);
        dest.writeString(justifyContent);
        dest.writeString(alignItems);
        dest.writeString(wrap);
        dest.writeString(alignContent);
        dest.writeString(direction);
        if (display == null)
            dest.writeString(Display.INLINE_BLOCK.name());
        else
            dest.writeString(display.name());
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public enum Display {BLOCK, INLINE_BLOCK, FLEX}

    private String width;
    private String height;
    private String maxWidth;
    private String maxHeight;
    private Display display;
    private String justifyContent;
    private String alignItems;
    private String wrap;
    private String alignContent;
    private String direction;

    public String getDirection() {
        return direction;
    }

    public Display getDisplay() {
        return display != null ? display : Display.BLOCK;
    }

    public String getMaxWidth() {
        if (TextUtils.isEmpty(maxWidth))
            return null;
        else
            return maxWidth.toLowerCase();
    }

    public String getMaxHeight() {
        if (TextUtils.isEmpty(maxHeight))
            return null;
        else
            return maxHeight.toLowerCase();
    }

    public String getWrap() {
        return wrap;
    }

    public String getAlignContent() {
        return alignContent;
    }
}
