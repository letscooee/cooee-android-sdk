package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Position implements Parcelable {

    protected Position(Parcel in) {
        top = in.readString();
        left = in.readString();
        bottom = in.readString();
        right = in.readString();
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
        dest.writeString(top);
        dest.writeString(left);
        dest.writeString(bottom);
        dest.writeString(right);
        dest.writeString(type.name());
        dest.writeString(gravity.name());
    }

    public enum PositionType {STATIC, RELATIVE, ABSOLUTE, FIXED}

    public enum Gravity {CENTER, TOP, BOTTOM}

    private PositionType type;
    private Gravity gravity;
    private String top;
    private String left;
    private String bottom;
    private String right;

    public PositionType getType() {
        return type;
    }

    public Gravity getGravity() {
        return gravity;
    }

    public String getTop() {
        return top;
    }

    public String getLeft() {
        return left;
    }

    public String getBottom() {
        return bottom;
    }

    public String getRight() {
        return right;
    }
}
