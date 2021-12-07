package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.letscooee.utils.ui.UnitUtils;

public class Position implements Parcelable {

    public enum PositionType {STATIC, ABSOLUTE, FIXED}

    private final PositionType type;
    private final String top;
    private final String left;
    private final String bottom;
    private final String right;

    private final Integer zIndex;

    public Position() {
        this.type = PositionType.STATIC;
        this.top = null;
        this.bottom = null;
        this.left = null;
        this.right = null;
        this.zIndex = null;
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

    protected Position(Parcel in) {
        top = in.readString();
        left = in.readString();
        bottom = in.readString();
        right = in.readString();
        type = (PositionType) in.readSerializable();
        zIndex = (Integer) in.readSerializable();
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
        dest.writeSerializable(type);
        dest.writeSerializable(zIndex);
    }

    public PositionType getType() {
        return type;
    }

    public boolean isNonStatic() {
        return this.type == PositionType.ABSOLUTE || this.type == PositionType.FIXED;
    }

    public int getTop(View parent) {
        Integer calculatedValue = UnitUtils.getCalculatedValue(parent, top, true);
        return calculatedValue != null ? calculatedValue : 0;
    }

    public int getLeft(View parent) {
        Integer calculatedValue = UnitUtils.getCalculatedValue(parent, left);
        return calculatedValue != null ? calculatedValue : 0;
    }

    public int getBottom(View parent) {
        Integer calculatedValue = UnitUtils.getCalculatedValue(parent, bottom, true);
        return calculatedValue != null ? calculatedValue : 0;
    }

    public int getRight(View parent) {
        Integer calculatedValue = UnitUtils.getCalculatedValue(parent, right);
        return calculatedValue != null ? calculatedValue : 0;
    }

    public Integer getzIndex() {
        return zIndex;
    }

    public boolean isAbsolute() {
        return this.type == Position.PositionType.ABSOLUTE;
    }
}