package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import static com.letscooee.utils.ValueUtil.getCalculatedValue;

public class Size implements Parcelable {

    protected Size(Parcel in) {
        width = in.readString();
        height = in.readString();
        maxWidth = in.readString();
        maxHeight = in.readString();
        justifyContent = in.readString();
        alignItems = in.readString();
        display = Display.valueOf(in.readString());
    }

    public String getJustifyContent() {
        return justifyContent;
    }

    public String getAlignItems() {
        return alignItems;
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
        if (display == null)
            dest.writeString(Display.INLINE_BLOCK.name());
        else
            dest.writeString(display.name());
    }

    public int getCalculatedHeight(int deviceWidth, int deviceHeight) {
        if (TextUtils.isEmpty(height))
            return 0;
        else
            return getCalculatedValue(deviceWidth, deviceHeight, getHeight(), true);
    }


    public int getCalculatedWidth(int deviceWidth, int deviceHeight) {
        if (TextUtils.isEmpty(width))
            return 0;
        else
            return getCalculatedValue(deviceWidth, deviceHeight, getWidth());
    }

    public enum Display {BLOCK, INLINE_BLOCK, FLEX}

    private String width;
    private String height;
    private String maxWidth;
    private String maxHeight;
    private Display display;
    private String justifyContent;
    private String alignItems;

    public Display getDisplay() {
        return display;
    }

    public String getWidth() {

        if (TextUtils.isEmpty(width))
            return null;
        else
            return width.toLowerCase();
    }

    public String getHeight() {
        if (TextUtils.isEmpty(height))
            return null;
        else
            return height.toLowerCase();
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
}
