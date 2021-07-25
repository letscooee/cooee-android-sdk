package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

public class Colour implements Parcelable {

    private String hex;
    private Gradient grad;

    protected Colour(Parcel in) {
        hex = in.readString();
        grad = in.readParcelable(Gradient.class.getClassLoader());
    }

    public static final Creator<Colour> CREATOR = new Creator<Colour>() {
        @Override
        public Colour createFromParcel(Parcel in) {
            return new Colour(in);
        }

        @Override
        public Colour[] newArray(int size) {
            return new Colour[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hex);
        dest.writeParcelable(grad, flags);
    }

    public String getHex() {
        return hex;
    }

    public Gradient getGrad() {
        return grad;
    }

    public int getSolidColor() {
        return android.graphics.Color.parseColor(hex);
    }
}
