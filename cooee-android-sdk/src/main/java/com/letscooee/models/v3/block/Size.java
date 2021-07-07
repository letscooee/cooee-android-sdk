package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Size implements Parcelable {

    protected Size(Parcel in) {
        width = in.readDouble();
        height = in.readDouble();
        maxWidth = in.readDouble();
        maxHeight = in.readDouble();
        display = Display.valueOf(in.readString());
        unit = Unit.valueOf(in.readString());
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
        dest.writeDouble(width);
        dest.writeDouble(height);
        dest.writeDouble(maxWidth);
        dest.writeDouble(maxHeight);
        dest.writeString(display.name());
        dest.writeString(unit.name());
    }

    public enum Display {BLOCK, INLINE_BLOCK}

    private double width;
    private double height;
    private double maxWidth;
    private double maxHeight;
    private Display display;
    private Unit unit;

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public Display getDisplay() {
        return display;
    }

    public Unit getUnit() {
        return unit;
    }
}
