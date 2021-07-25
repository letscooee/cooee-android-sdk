package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

public class Overflow implements Parcelable {

    private String x;
    private String y;

    protected Overflow(Parcel in) {
        x = in.readString();
        y = in.readString();
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

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(x);
        dest.writeString(y);
    }
}
