package com.letscooee.models;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @author Abhishek Taparia
 */
public class TriggerBehindBackground implements Parcelable {

    private final static int DEFAULT_BLUR_RADIUS = 25;
    private final static String DEFAULT_COLOR = "#828282";     // Default colour- Gray

    public enum Type {
        BLURRED, SOLID_COLOR
    }

    private final Type type;
    private final int blur;
    private final String color;

    protected TriggerBehindBackground(Parcel in) {
        blur = in.readInt();
        type = Type.valueOf(in.readString());
        color = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(blur);
        dest.writeString(type.name());
        dest.writeString(color);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TriggerBehindBackground> CREATOR = new Creator<TriggerBehindBackground>() {
        @Override
        public TriggerBehindBackground createFromParcel(Parcel in) {
            return new TriggerBehindBackground(in);
        }

        @Override
        public TriggerBehindBackground[] newArray(int size) {
            return new TriggerBehindBackground[size];
        }
    };

    public Type getType() {
        return type;
    }

    @Deprecated
    public int getBlur() {
        return blur;
    }

    public int getBlurRadius() {
        return blur == 0 ? DEFAULT_BLUR_RADIUS : blur;
    }

    public String getColor() {
        return TextUtils.isEmpty(color) ? DEFAULT_COLOR : color;
    }

    public int getParsedColor() {
        return Color.parseColor(this.getColor());
    }
}
