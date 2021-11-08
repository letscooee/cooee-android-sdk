package com.letscooee.models.trigger.blocks;

import static com.letscooee.utils.ui.UnitUtils.getCalculatedValue;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

public class Size implements Parcelable {

    private String width;
    private String height;
    private String maxWidth;
    private String maxHeight;
    private Display display;

    // The default value constructor
    public Size() {
        this.display = Display.BLOCK;
    }

    protected Size(Parcel in) {
        width = in.readString();
        height = in.readString();
        maxWidth = in.readString();
        maxHeight = in.readString();
        display = (Display) in.readSerializable();
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
        dest.writeSerializable(display);
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public enum Display {BLOCK, INLINE_BLOCK, FLEX, INLINE_FLEX}

    public Display getDisplay() {
        return display != null ? display : Display.BLOCK;
    }

    public boolean isDisplayFlex() {
        return (this.getDisplay() == Display.FLEX || this.getDisplay() == Display.INLINE_FLEX);
    }

    public Integer getCalculatedMaxWidth(View parent) {
        return getCalculatedValue(parent, maxWidth);
    }

    public Integer getCalculatedMaxHeight(View parent) {
        return getCalculatedValue(parent, maxHeight, true);
    }
}
