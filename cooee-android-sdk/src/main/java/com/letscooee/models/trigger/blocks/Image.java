package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {

    private String url;
    private int alpha;

    protected Image(Parcel in) {
        url = in.readString();
        alpha = in.readInt();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public int getAlpha() {
        return alpha;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeInt(alpha);
    }
}
