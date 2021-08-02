package com.letscooee.models.trigger.elements;

import android.os.Parcel;

public class ImageElement extends BaseChildElement {

    private final String url;

    protected ImageElement(Parcel in) {
        super(in);
        url = in.readString();
    }

    public static final Creator<ImageElement> CREATOR = new Creator<ImageElement>() {
        @Override
        public ImageElement createFromParcel(Parcel in) {
            return new ImageElement(in);
        }

        @Override
        public ImageElement[] newArray(int size) {
            return new ImageElement[size];
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
