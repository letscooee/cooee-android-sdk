package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Spacing implements Parcelable {

    @SerializedName("p")
    @Expose
    private final float padding;

    @SerializedName("pl")
    @Expose
    private final float paddingLeft;

    @SerializedName("pr")
    @Expose
    private final float paddingRight;

    @SerializedName("pt")
    @Expose
    private final float paddingTop;

    @SerializedName("pb")
    @Expose
    private final float paddingBottom;

    protected Spacing(Parcel in) {
        padding = in.readFloat();
        paddingLeft = in.readFloat();
        paddingRight = in.readFloat();
        paddingTop = in.readFloat();
        paddingBottom = in.readFloat();
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

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public float getPaddingTop() {
        return paddingTop;
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(padding);
        dest.writeFloat(paddingLeft);
        dest.writeFloat(paddingRight);
        dest.writeFloat(paddingTop);
        dest.writeFloat(paddingBottom);
    }
}
