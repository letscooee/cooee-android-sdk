package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Position implements Parcelable {

    protected Position(Parcel in) {
        top = in.readInt();
        left = in.readInt();
        bottom = in.readInt();
        right = in.readInt();
        type = PositionType.valueOf(in.readString());
        gravity = Gravity.valueOf(in.readString());
    }

    public static final Creator<Position> CREATOR = new Creator<Position>() {
        @Override
        public Position createFromParcel(Parcel in) {
            return new Position(in);
        }

        @Override
        public Position[] newArray(int size) {
            return new Position[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(top);
        dest.writeInt(left);
        dest.writeInt(bottom);
        dest.writeInt(right);
        dest.writeString(type.name());
        dest.writeString(gravity.name());
    }

    public enum PositionType {STATIC, RELATIVE, ABSOLUTE, FIXED}

    public enum Gravity {CENTER, TOP, BOTTOM}

    private PositionType type;
    private Gravity gravity;
    private int top;
    private int left;
    private int bottom;
    private int right;

    public PositionType getType() {
        return type;
    }

    public Gravity getGravity() {
        return gravity;
    }

    public int getTop() {
        return top;
    }

    public int getLeft() {
        return left;
    }

    public int getBottom() {
        return bottom;
    }

    public int getRight() {
        return right;
    }
}
