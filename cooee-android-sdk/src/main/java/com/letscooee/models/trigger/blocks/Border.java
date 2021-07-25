package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.letscooee.utils.ValueUtil;

import static com.letscooee.utils.ValueUtil.getCalculatedValue;

public class Border implements Parcelable {

    // TODO: 07/07/21 Discus for dash type stroke
    protected Border(Parcel in) {
        radius = in.readString();
        width = in.readString();
        dashWidth = in.readString();
        dashGap = in.readString();
        colour = in.readParcelable(Color.class.getClassLoader());
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
        dest.writeParcelable(colour, flags);
        if (style == null)
            dest.writeString(Style.SOLID.name());
        else
            dest.writeString(style.name());
    }

    public enum Style {SOLID, DASH}

    private String radius;
    private String width;
    private String dashWidth;
    private String dashGap;
    private Color colour;
    private Style style;

    public Color getColor() {
        return colour;
    }

    public Style getStyle() {
        return style;
    }

    public int getRadius() {
        return !TextUtils.isEmpty(radius) ? ValueUtil.getCalculatedPixel(radius) : 0;
    }

    public int getWidth(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, width);
    }

    public int getDashWidth(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, dashWidth);
    }

    public int getDashGap(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, dashGap);
    }


}
