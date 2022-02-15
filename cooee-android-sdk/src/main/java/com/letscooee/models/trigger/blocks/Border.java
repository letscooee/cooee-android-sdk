package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Border implements Parcelable {

    public enum Style {SOLID, DASH}

    @SerializedName("r")
    @Expose
    private final float radius;

    @SerializedName("w")
    @Expose
    private final float width;

    @SerializedName("dw")
    @Expose
    private final float dashWidth;

    @SerializedName("dg")
    @Expose
    private final float dashGap;

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
        radius = in.readFloat();
        width = in.readFloat();
        dashWidth = in.readFloat();
        dashGap = in.readFloat();
        colour = in.readParcelable(Colour.class.getClassLoader());
        style = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(radius);
        dest.writeFloat(width);
        dest.writeFloat(dashWidth);
        dest.writeFloat(dashGap);
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

    public float getRadius() {
        return radius;
    }

    public float getWidth() {
        return width;
    }

    public float getDashWidth(View parent) {
        return dashWidth;
    }

    public float getDashGap(View parent) {
        return dashGap;
    }
}
