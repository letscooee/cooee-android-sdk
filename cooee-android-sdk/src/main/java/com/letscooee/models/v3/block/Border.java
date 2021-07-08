package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Border implements Parcelable {

    // TODO: 07/07/21 Discus for dash type stroke
    protected Border(Parcel in) {
        radius = in.readString();
        width = in.readString();
        dashWidth = in.readString();
        dashGap = in.readString();
        color = in.readParcelable(Color.class.getClassLoader());
        style = Style.valueOf(in.readString());
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
        dest.writeString(radius);
        dest.writeString(width);
        dest.writeString(dashWidth);
        dest.writeString(dashGap);
        dest.writeParcelable(color, flags);
        dest.writeString(style.name());
    }

    public enum Style {SOLID, DASH}

    private String radius;
    private String width;
    private String dashWidth;
    private String dashGap;
    private Color color;
    private Style style;


    public Color getColor() {
        return color;
    }

    public Style getStyle() {
        return style;
    }

    public String getRadius() {
        return radius;
    }

    public String getWidth() {
        return width;
    }

    public String getDashWidth() {
        return dashWidth;
    }

    public String getDashGap() {
        return dashGap;
    }
}
