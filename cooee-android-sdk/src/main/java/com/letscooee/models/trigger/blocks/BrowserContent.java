package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BrowserContent implements Parcelable {

    @SerializedName("u")
    @Expose
    private String url;

    protected BrowserContent(Parcel in) {
        url = in.readString();
    }

    public static final Creator<BrowserContent> CREATOR = new Creator<BrowserContent>() {
        @Override
        public BrowserContent createFromParcel(Parcel in) {
            return new BrowserContent(in);
        }

        @Override
        public BrowserContent[] newArray(int size) {
            return new BrowserContent[size];
        }
    };

    public String getUrl() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
    }
}
