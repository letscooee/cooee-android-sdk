package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Glossy implements Parcelable {

    // TODO: 07/07/21 Glassmorphism can also has color
     private int radius;
     private int sampling;
     private Color color;

    protected Glossy(Parcel in) {
        radius = in.readInt();
        sampling = in.readInt();
        color = in.readParcelable(Color.class.getClassLoader());
    }

    public static final Creator<Glossy> CREATOR = new Creator<Glossy>() {
        @Override
        public Glossy createFromParcel(Parcel in) {
            return new Glossy(in);
        }

        @Override
        public Glossy[] newArray(int size) {
            return new Glossy[size];
        }
    };

    public int getRadius() {
        return radius;
    }

    public int getSampling() {
        return sampling;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(radius);
        dest.writeInt(sampling);
        dest.writeParcelable(color, flags);
    }
}
