package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.letscooee.utils.ValueUtil;

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
        if (gravity == null)
            dest.writeString(Gravity.CENTER.name());
        else
            dest.writeString(gravity.name());
    }

    public enum PositionType {STATIC, ABSOLUTE, FIXED}

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

    public int getTop(int deviceWidth, int deviceHeight) {

        return !TextUtils.isEmpty(top) ?
                ValueUtil.getCalculatedValue(deviceWidth, deviceHeight, top.toLowerCase(), true)
                : 0;
    }

    public int getLeft(int deviceWidth, int deviceHeight) {
        return !TextUtils.isEmpty(left) ?
                ValueUtil.getCalculatedValue(deviceWidth, deviceHeight, left.toLowerCase()) : 0;
    }

    public int getBottom(int deviceWidth, int deviceHeight) {
        return !TextUtils.isEmpty(bottom) ?
                ValueUtil.getCalculatedValue(deviceWidth, deviceHeight, bottom.toLowerCase(), true)
                : 0;
    }

    public int getRight(int deviceWidth, int deviceHeight) {
        return !TextUtils.isEmpty(right) ?
                ValueUtil.getCalculatedValue(deviceWidth, deviceHeight, right.toLowerCase(), true)
                : 0;
    }
}
