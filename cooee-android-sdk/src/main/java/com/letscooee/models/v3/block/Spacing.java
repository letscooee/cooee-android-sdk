package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Spacing implements Parcelable {


    private String padding;
    private String paddingLeft;
    private String paddingRight;
    private String paddingTop;
    private String paddingBottom;
    private String margin;
    private String marginLeft;
    private String marginRight;
    private String marginTop;
    private String marginBottom;

    protected Spacing(Parcel in) {
        padding = in.readString();
        paddingLeft = in.readString();
        paddingRight = in.readString();
        paddingTop = in.readString();
        paddingBottom = in.readString();
        margin = in.readString();
        marginLeft = in.readString();
        marginRight = in.readString();
        marginTop = in.readString();
        marginBottom = in.readString();
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

    public String getPadding() {
        return padding;
    }

    public String getPaddingLeft() {
        return paddingLeft;
    }

    public String getPaddingRight() {
        return paddingRight;
    }

    public String getPaddingTop() {
        return paddingTop;
    }

    public String getPaddingBottom() {
        return paddingBottom;
    }

    public String getMargin() {
        return margin;
    }

    public String getMarginLeft() {
        return marginLeft;
    }

    public String getMarginRight() {
        return marginRight;
    }

    public String getMarginTop() {
        return marginTop;
    }

    public String getMarginBottom() {
        return marginBottom;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(padding);
        dest.writeString(paddingLeft);
        dest.writeString(paddingRight);
        dest.writeString(paddingTop);
        dest.writeString(paddingBottom);
        dest.writeString(margin);
        dest.writeString(marginLeft);
        dest.writeString(marginRight);
        dest.writeString(marginTop);
        dest.writeString(marginBottom);
    }


}
