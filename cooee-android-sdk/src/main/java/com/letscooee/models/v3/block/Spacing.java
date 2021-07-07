package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Spacing implements Parcelable {

    private Unit unit;
    private int padding;
    private int paddingLeft;
    private int paddingRight;
    private int paddingTop;
    private int paddingBottom;
    private int margin;
    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBottom;

    protected Spacing(Parcel in) {
        padding = in.readInt();
        paddingLeft = in.readInt();
        paddingRight = in.readInt();
        paddingTop = in.readInt();
        paddingBottom = in.readInt();
        margin = in.readInt();
        marginLeft = in.readInt();
        marginRight = in.readInt();
        marginTop = in.readInt();
        marginBottom = in.readInt();
        unit = Unit.valueOf(in.readString());
    }

    public static final Creator<Spacing> CREATOR = new Creator<Spacing>() {
        @Override
        public Spacing createFromParcel(Parcel in) {
            return new Spacing(in);
        }

        @Override
        public Spacing[] newArray(int size) {
            return new Spacing[size];
        }
    };

    public Unit getUnit() {
        return unit;
    }

    public int getPadding() {
        return padding;
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public int getMargin() {
        return margin;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public int getMarginRight() {
        return marginRight;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(padding);
        dest.writeInt(paddingLeft);
        dest.writeInt(paddingRight);
        dest.writeInt(paddingTop);
        dest.writeInt(paddingBottom);
        dest.writeInt(margin);
        dest.writeInt(marginLeft);
        dest.writeInt(marginRight);
        dest.writeInt(marginTop);
        dest.writeInt(marginBottom);
        dest.writeString(unit.name());
    }
}
