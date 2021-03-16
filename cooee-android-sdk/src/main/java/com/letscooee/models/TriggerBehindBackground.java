package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Abhishek Taparia
 */
public class TriggerBehindBackground implements Parcelable {
    public enum Type {
        BLURRED
    }

    private Type type;
    private int blur = 25;
    private String color = "828282";

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

    public int getBlur() {
        return blur;
    }

    public String getColor() {
        return color;
    }
}
