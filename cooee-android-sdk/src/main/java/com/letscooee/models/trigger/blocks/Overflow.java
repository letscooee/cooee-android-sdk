package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

public class Overflow implements Parcelable {

    public enum Type {VISIBLE, HIDDEN}

    private final Type x;
    private final Type y;

    protected Overflow(Parcel in) {
        x = (Type) in.readSerializable();
        y = (Type) in.readSerializable();
    }

    public static final Creator<Overflow> CREATOR = new Creator<Overflow>() {
        @Override
        public Overflow createFromParcel(Parcel in) {
            return new Overflow(in);
        }

        @Override
        public Overflow[] newArray(int size) {
            return new Overflow[size];
        }
    };

    public Type getX() {
        return x;
    }

    public Type getY() {
        return y;
    }

    public boolean hideOverFlow() {
        // Accessing only x value to check HIDDEN/VISIBLE as in Android there is no option to operate
        // on x or y
        return x == Type.HIDDEN;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(x);
        dest.writeSerializable(y);
    }
}
