package com.letscooee.models.v3.elemeent.property;

import android.os.Parcel;
import android.os.Parcelable;

public class Alignment implements Parcelable {

    protected Alignment(Parcel in) {
        align = Align.valueOf(in.readString());
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
        if (align == null)
            dest.writeString(Align.CENTER.name());
        else
            dest.writeString(align.name());
        if (direction == null)
            dest.writeString(Direction.ltr.name());
        else
            dest.writeString(direction.name());
    }

    public enum Align {LEFT, CENTER, RIGHT}

    public enum Direction {ltr, rtl, ttb, btt}

    private Align align;
    private Direction direction;

    public Align getAlign() {
        return align;
    }

    public Direction getDirection() {
        return direction;
    }
}
