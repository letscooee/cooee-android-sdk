package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Transform implements Parcelable {

    private int rotate;

    protected Transform(Parcel in) {
        rotate = in.readInt();
    }

    public static final Creator<Transform> CREATOR = new Creator<Transform>() {
        @Override
        public Transform createFromParcel(Parcel in) {
            return new Transform(in);
        }

        @Override
        public Transform[] newArray(int size) {
            return new Transform[size];
        }
    };

    public int getRotate() {
        return rotate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rotate);
    }
}
