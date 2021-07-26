package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.letscooee.utils.ui.UnitUtils;

public class Position implements Parcelable {

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

    private final PositionType type;
    private final String top;
    private final String left;
    private final String bottom;
    private final String right;

    protected Position(Parcel in) {
        top = in.readString();
        left = in.readString();
        bottom = in.readString();
        right = in.readString();
        type = PositionType.valueOf(in.readString());
    }

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
    }

    public PositionType getType() {
        return type;
    }

    public boolean isNonStatic() {
        return this.type == PositionType.ABSOLUTE || this.type == PositionType.FIXED;
    }

    public int getTop(int deviceWidth, int deviceHeight) {
        return !TextUtils.isEmpty(top) ?
                UnitUtils.getCalculatedValue(deviceWidth, deviceHeight, top.toLowerCase(), true)
                : 0;
    }

    public int getLeft(int deviceWidth, int deviceHeight) {
        return !TextUtils.isEmpty(left) ?
                UnitUtils.getCalculatedValue(deviceWidth, deviceHeight, left.toLowerCase()) : 0;
    }

    public int getBottom(int deviceWidth, int deviceHeight) {
        return !TextUtils.isEmpty(bottom) ?
                UnitUtils.getCalculatedValue(deviceWidth, deviceHeight, bottom.toLowerCase(), true)
                : 0;
    }

    public int getRight(int deviceWidth, int deviceHeight) {
        return !TextUtils.isEmpty(right) ?
                UnitUtils.getCalculatedValue(deviceWidth, deviceHeight, right.toLowerCase())
                : 0;
    }

    public enum PositionType {STATIC, ABSOLUTE, FIXED}
}
