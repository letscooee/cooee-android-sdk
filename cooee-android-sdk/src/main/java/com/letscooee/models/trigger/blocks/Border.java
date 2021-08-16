package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import com.letscooee.utils.ui.UnitUtils;

import static com.letscooee.utils.ui.UnitUtils.getCalculatedValue;

public class Border implements Parcelable {

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
    private final String radius;
    private final String width;
    private final String dashWidth;
    private final String dashGap;
    private final Colour colour;
    private final Style style;

    // TODO: 07/07/21 Discus for dash type stroke
    protected Border(Parcel in) {
        radius = in.readString();
        width = in.readString();
        dashWidth = in.readString();
        dashGap = in.readString();
        colour = in.readParcelable(Colour.class.getClassLoader());
        style = Style.valueOf(in.readString());
    }

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
        dest.writeParcelable(colour, flags);
        if (style == null)
            dest.writeString(Style.SOLID.name());
        else
            dest.writeString(style.name());
    }

    public Colour getColor() {
        if (colour == null) return new Colour();
        return colour;
    }

    public Style getStyle() {
        return style;
    }

    public int getRadius() {
        return !TextUtils.isEmpty(radius) ? UnitUtils.getCalculatedPixel(radius) : 0;
    }

    public Integer getWidth(View parent) {
        return getCalculatedValue(parent, width);
    }

    public Integer getDashWidth(View parent) {
        return getCalculatedValue(parent, dashWidth);
    }

    public Integer getDashGap(View parent) {
        return getCalculatedValue(parent, dashGap);
    }

    public enum Style {SOLID, DASH}
}
