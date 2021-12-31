package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.utils.ui.UnitUtils;

import static com.letscooee.utils.ui.UnitUtils.getCalculatedValue;

public class Border implements Parcelable {

    public enum Style {SOLID, DASH}

    @SerializedName("r")
    @Expose
    private final String radius;

    @SerializedName("w")
    @Expose
    private final String width;

    @SerializedName("dw")
    @Expose
    private final String dashWidth;

    @SerializedName("dg")
    @Expose
    private final String dashGap;

    @SerializedName("c")
    @Expose
    private final Colour colour;

    @SerializedName("s")
    @Expose
    private final int style;

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

    protected Border(Parcel in) {
        radius = in.readString();
        width = in.readString();
        dashWidth = in.readString();
        dashGap = in.readString();
        colour = in.readParcelable(Colour.class.getClassLoader());
        style = in.readInt();
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
        dest.writeInt(style);
    }

    public Colour getColor() {
        if (colour == null) return new Colour();
        return colour;
    }

    public Style getStyle() {
        if (style == 2) {
            return Style.DASH;
        }

        return Style.SOLID;
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
}
