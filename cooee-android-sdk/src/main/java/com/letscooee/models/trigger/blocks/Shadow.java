package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Shadow implements Parcelable {

    @SerializedName("spr") // Spread from composer
    @Expose
    private final int elevation;

    @SerializedName("clr")
    @Expose
    private final Colour colour;

    protected Shadow(Parcel in) {
        elevation = in.readInt();
        colour = in.readParcelable(Colour.class.getClassLoader());
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

    public Colour getColor() {
        return colour;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(elevation);
        dest.writeParcelable(colour, flags);
    }

}
