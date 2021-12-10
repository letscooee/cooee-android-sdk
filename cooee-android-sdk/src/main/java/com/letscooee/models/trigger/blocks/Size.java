package com.letscooee.models.trigger.blocks;

import static com.letscooee.utils.ui.UnitUtils.getCalculatedValue;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Size implements Parcelable {

    @SerializedName("w")
    @Expose
    private final String width;

    @SerializedName("h")
    @Expose
    private final String height;

    public Size() {
        width = null;
        height = null;
    }

    protected Size(Parcel in) {
        width = in.readString();
        height = in.readString();
    }

    public static final Creator<Size> CREATOR = new Creator<Size>() {
        @Override
        public Size createFromParcel(Parcel in) {
            return new Size(in);
        }

        @Override
        public Size[] newArray(int size) {
            return new Size[size];
        }
    };

    public Integer getCalculatedHeight(View parent) {
        return getCalculatedValue(parent, height, true);
    }

    public Integer getCalculatedWidth(View parent) {
        return getCalculatedValue(parent, width);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(width);
        dest.writeString(height);
    }
}
