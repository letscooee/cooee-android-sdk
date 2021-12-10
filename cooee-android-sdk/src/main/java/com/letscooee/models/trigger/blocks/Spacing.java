package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static com.letscooee.utils.ui.UnitUtils.getCalculatedValue;

public class Spacing implements Parcelable {

    @SerializedName("p")
    @Expose
    private final String padding;

    @SerializedName("pl")
    @Expose
    private final String paddingLeft;

    @SerializedName("pr")
    @Expose
    private final String paddingRight;

    @SerializedName("pt")
    @Expose
    private final String paddingTop;

    @SerializedName("pb")
    @Expose
    private final String paddingBottom;

    private int calculatedPadding = 0;

    protected Spacing(Parcel in) {
        padding = in.readString();
        paddingLeft = in.readString();
        paddingRight = in.readString();
        paddingTop = in.readString();
        paddingBottom = in.readString();
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

    public void calculatedPaddingAndMargin(View parent) {
        if (!TextUtils.isEmpty(padding)) {
            calculatedPadding = getCalculatedValue(parent, padding);
        }
    }

    public int getPaddingLeft(View parent) {
        Integer calculatedValue = getCalculatedValue(parent, paddingLeft);
        return calculatedValue != null ? calculatedValue : calculatedPadding;
    }

    public int getPaddingRight(View parent) {
        Integer calculatedValue = getCalculatedValue(parent, paddingRight);
        return calculatedValue != null ? calculatedValue : calculatedPadding;
    }

    public int getPaddingTop(View parent) {
        Integer calculatedValue = getCalculatedValue(parent, paddingTop, true);
        return calculatedValue != null ? calculatedValue : calculatedPadding;
    }

    public int getPaddingBottom(View parent) {
        Integer calculatedValue = getCalculatedValue(parent, paddingBottom, true);
        return calculatedValue != null ? calculatedValue : calculatedPadding;
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
    }
}
