package com.letscooee.models.v3.element;

import android.os.Parcel;

public class VideoElement extends BaseChildElement {

    private final String url;

    protected VideoElement(Parcel in) {
        super(in);
        url = in.readString();
    }

    public static final Creator<VideoElement> CREATOR = new Creator<VideoElement>() {
        @Override
        public VideoElement createFromParcel(Parcel in) {
            return new VideoElement(in);
        }

        @Override
        public VideoElement[] newArray(int size) {
            return new VideoElement[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(url);
    }

    public String getUrl() {
        return url;
    }
}
