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

    @SerializedName("m")
    @Expose
    private final String margin;

    @SerializedName("ml")
    @Expose
    private final String marginLeft;

    @SerializedName("mr")
    @Expose
    private final String marginRight;

    @SerializedName("mt")
    @Expose
    private final String marginTop;

    @SerializedName("mb")
    @Expose
    private final String marginBottom;

    private int calculatedMargin = 0;
    private int calculatedPadding = 0;

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

    public void calculatedPaddingAndMargin(View parent) {
        if (!TextUtils.isEmpty(margin)) {
            calculatedMargin = getCalculatedValue(parent, margin);
        }
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

    public int getMarginLeft(View parent) {
        Integer calculatedValue = getCalculatedValue(parent, marginLeft);
        return calculatedValue != null ? calculatedValue : calculatedMargin;
    }

    public int getMarginRight(View parent) {
        Integer calculatedValue = getCalculatedValue(parent, marginRight);
        return calculatedValue != null ? calculatedValue : calculatedMargin;
    }

    public int getMarginTop(View parent) {
        Integer calculatedValue = getCalculatedValue(parent, marginTop, true);
        return calculatedValue != null ? calculatedValue : calculatedMargin;
    }

    public int getMarginBottom(View parent) {
        Integer calculatedValue = getCalculatedValue(parent, marginBottom, true);
        return calculatedValue != null ? calculatedValue : calculatedMargin;
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
