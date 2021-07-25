package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
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

    public int getPadding(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, padding);
    }

    public int getPaddingLeft(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, paddingLeft);
    }

    public int getPaddingRight(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, paddingRight);
    }

    public int getPaddingTop(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, paddingTop, true);
    }

    public int getPaddingBottom(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, paddingBottom, true);
    }

    public int getMargin(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, margin);
    }

    public int getMarginLeft(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, marginLeft);
    }

    public int getMarginRight(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, marginRight);
    }

    public int getMarginTop(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, marginTop, true);
    }

    public int getMarginBottom(int deviceWidth, int deviceHeight) {
        return getCalculatedValue(deviceWidth, deviceHeight, marginBottom, true);
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
