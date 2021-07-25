package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

public class Alignment implements Parcelable {

    protected Alignment(Parcel in) {
        align = in.readString();
        direction = Direction.valueOf(in.readString());
    }

    public static final Creator<Alignment> CREATOR = new Creator<Alignment>() {
        @Override
        public Alignment createFromParcel(Parcel in) {
            return new Alignment(in);
        }

        @Override
        public Alignment[] newArray(int size) {
            return new Alignment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(align);
        if (direction == null)
            dest.writeString(Direction.ltr.name());
        else
            dest.writeString(direction.name());
    }

    public enum Align {LEFT, CENTER, RIGHT}

    public enum Direction {ltr, rtl}

    private String align;
    private Direction direction;

    public String getAlign() {
        return align;
    }

    public Direction getDirection() {
        return direction;
    }
}
