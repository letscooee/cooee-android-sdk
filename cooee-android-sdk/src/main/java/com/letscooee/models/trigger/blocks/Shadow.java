package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

public class Shadow implements Parcelable {

    // TODO: 07/07/21 Discus for transitionZ
    private int elevation;
    private Color color;

    protected Shadow(Parcel in) {
        elevation = in.readInt();
        color = in.readParcelable(Color.class.getClassLoader());
    }

    public static final Creator<Shadow> CREATOR = new Creator<Shadow>() {
        @Override
        public Shadow createFromParcel(Parcel in) {
            return new Shadow(in);
        }

        @Override
        public Shadow[] newArray(int size) {
            return new Shadow[size];
        }
    };

    public int getElevation() {
        return elevation;
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
        dest.writeInt(elevation);
        dest.writeParcelable(color, flags);
    }
}
