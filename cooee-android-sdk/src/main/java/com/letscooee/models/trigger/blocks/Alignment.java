package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

import com.letscooee.enums.trigger.TextAlignment;

public class Alignment implements Parcelable {

    private TextAlignment align;
    private Direction direction;

    protected Alignment(Parcel in) {
        align = (TextAlignment) in.readSerializable();
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

    public enum Direction {LTR, RTL}

    public int getAlign() {
        return align == null ? TextAlignment.CENTER.getValue() : align.getValue();
    }

    public Direction getDirection() {
        return direction == null ? Direction.LTR : direction;
    }
}
