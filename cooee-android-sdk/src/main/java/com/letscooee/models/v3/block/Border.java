package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

import com.letscooee.utils.ValueUtil;

import static com.letscooee.utils.Constants.PERCENT;
import static com.letscooee.utils.Constants.PIXEL;
import static com.letscooee.utils.Constants.VIEWPORT_HEIGHT;
import static com.letscooee.utils.Constants.VIEWPORT_WIDTH;
import static com.letscooee.utils.ValueUtil.getCalculatedValue;

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

    public int getRadius() {
        return ValueUtil.getCalculatedValue(radius);
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
