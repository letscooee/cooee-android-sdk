package com.letscooee.models.trigger.blocks;

import static com.letscooee.utils.ui.UnitUtils.getScaledPixel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Spacing implements Parcelable {

    @SerializedName("p")
    @Expose
    private final Float padding;

    @SerializedName("pl")
    @Expose
    private final Float paddingLeft;

    @SerializedName("pr")
    @Expose
    private final Float paddingRight;

    @SerializedName("pt")
    @Expose
    private final Float paddingTop;

    @SerializedName("pb")
    @Expose
    private final Float paddingBottom;

    private float calculatedPadding = 0;

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

    public void calculatedPadding() {
        calculatedPadding = getScaledPixel(padding);

    }

    public float getPaddingLeft() {
        float calculatedValue = getScaledPixel(paddingLeft);
        return calculatedValue == 0 ? calculatedPadding : calculatedValue;
    }

    public float getPaddingRight() {
        float calculatedValue = getScaledPixel(paddingRight);
        return calculatedValue == 0 ? calculatedPadding : calculatedValue;
    }

    public float getPaddingTop() {
        float calculatedValue = getScaledPixel(paddingTop);
        return calculatedValue == 0 ? calculatedPadding : calculatedValue;
    }

    public float getPaddingBottom() {
        float calculatedValue = getScaledPixel(paddingBottom);
        return calculatedValue == 0 ? calculatedPadding : calculatedValue;
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
