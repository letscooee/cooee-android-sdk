package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.letscooee.enums.trigger.PositionType;
import com.letscooee.utils.ui.UnitUtils;

public class Position implements Parcelable {



    private final PositionType type;
    private final String y;
    private final String x;

    private final Integer z;

    public Position() {
        this.type = PositionType.FOLLOW_PARENT;
        this.y = null;
        this.x = null;
        this.z = null;
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
        y = in.readString();
        x = in.readString();
        type = (PositionType) in.readSerializable();
        z = (Integer) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(y);
        dest.writeString(x);
        dest.writeSerializable(type);
        dest.writeSerializable(z);
    }

    public PositionType getType() {
        return type;
    }

    public boolean isNonStatic() {
        return this.type == PositionType.FREE_FLOATING;
    }

    public int getY(View parent) {
        Integer calculatedValue = UnitUtils.getCalculatedValue(parent, y, true);
        return calculatedValue != null ? calculatedValue : 0;
    }

    public int getX(View parent) {
        Integer calculatedValue = UnitUtils.getCalculatedValue(parent, x);
        return calculatedValue != null ? calculatedValue : 0;
    }

    public Integer getZ() {
        return z;
    }

    public boolean isAbsolute() {
        return this.type == PositionType.FREE_FLOATING;
    }
}
