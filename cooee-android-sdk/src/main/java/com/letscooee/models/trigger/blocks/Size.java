package com.letscooee.models.trigger.blocks;

import static com.letscooee.utils.ui.UnitUtils.getCalculatedValue;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.letscooee.enums.trigger.FlexProperty;

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
        justifyContent = (FlexProperty.JustifyContent) in.readSerializable();
        alignItems = (FlexProperty.AlignItems) in.readSerializable();
        wrap = (FlexProperty.Wrap) in.readSerializable();
        alignContent = (FlexProperty.AlignContent) in.readSerializable();
        direction = (FlexProperty.Direction) in.readSerializable();
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

    public int getJustifyContent() {
        return justifyContent == null ?
                FlexProperty.JustifyContent.FLEX_START.getValue() : justifyContent.getValue();
    }

    public int getAlignItems() {
        return alignItems == null ? FlexProperty.AlignItems.FLEX_START.getValue() : alignItems.getValue();
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
        dest.writeSerializable(justifyContent);
        dest.writeSerializable(alignItems);
        dest.writeSerializable(wrap);
        dest.writeSerializable(alignContent);
        dest.writeSerializable(direction);
        dest.writeSerializable(display);
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public enum Display {BLOCK, INLINE_BLOCK, FLEX, INLINE_FLEX}

    private String width;
    private String height;
    private String maxWidth;
    private String maxHeight;
    private Display display;
    private FlexProperty.JustifyContent justifyContent;
    private FlexProperty.AlignItems alignItems;
    private FlexProperty.Wrap wrap;
    private FlexProperty.AlignContent alignContent;
    private FlexProperty.Direction direction;

    public int getDirection() {
        return direction == null ? FlexProperty.Direction.ROW.getValue() : direction.getValue();
    }

    public Display getDisplay() {
        return display != null ? display : Display.BLOCK;
    }

    public boolean isDisplayFlex() {
        return (this.getDisplay() == Display.FLEX || this.getDisplay() == Display.INLINE_FLEX);
    }

    public Integer getCalculatedMaxWidth(View parent) {
        return getCalculatedValue(parent, width);
    }

    public Integer getCalculatedMaxHeight(View parent) {
        return getCalculatedValue(parent, maxHeight, true);
    }

    public int getWrap() {
        return wrap == null ? FlexProperty.Wrap.WRAP.getValue() : wrap.getValue();
    }

    public int getAlignContent() {
        return alignContent == null ?
                FlexProperty.AlignContent.FLEX_START.getValue() : alignContent.getValue();
    }
}
