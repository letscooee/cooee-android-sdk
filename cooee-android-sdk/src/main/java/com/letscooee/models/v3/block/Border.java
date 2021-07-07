package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Border implements Parcelable {

    // TODO: 07/07/21 Discus for dash type stroke
    protected Border(Parcel in) {
        radius = in.readInt();
        width = in.readInt();
        dashWidth = in.readInt();
        dashGap = in.readInt();
        color = in.readParcelable(Color.class.getClassLoader());
        style = Style.valueOf(in.readString());
        unit = Unit.valueOf(in.readString());
    }

    public static final Creator<Border> CREATOR = new Creator<Border>() {
        @Override
        public Border createFromParcel(Parcel in) {
            return new Border(in);
        }

        @Override
        public Border[] newArray(int size) {
            return new Border[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(radius);
        dest.writeInt(width);
        dest.writeInt(dashWidth);
        dest.writeInt(dashGap);
        dest.writeParcelable(color, flags);
        dest.writeString(style.name());
        dest.writeString(unit.name());
    }

    public enum Style {SOLID, DASH}

    private int radius;
    private Unit unit;
    private int width;
    private int dashWidth;
    private int dashGap;
    private Color color;
    private Style style;

    public int getRadius() {
        return radius;
    }

    public Unit getUnit() {
        return unit;
    }

    public int getWidth() {
        return width;
    }

    public int getDashWidth() {
        return dashWidth;
    }

    public int getDashGap() {
        return dashGap;
    }

    public Color getColor() {
        return color;
    }

    public Style getStyle() {
        return style;
    }
}
