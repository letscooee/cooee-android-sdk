package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

public class Alignment implements Parcelable {

    protected Alignment(Parcel in) {
        align = (Align) in.readSerializable();
        direction = (Direction) in.readSerializable();
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
        dest.writeSerializable(align);
        dest.writeSerializable(direction);
    }

    public enum Align {LEFT, CENTER, RIGHT}

    public enum Direction {LTR, RTL}

    private Align align;
    private Direction direction;

    public Align getAlign() {
        return align == null ? Align.LEFT : align;
    }

    public Direction getDirection() {
        return direction == null ? Direction.LTR : direction;
    }
}
