package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import com.letscooee.utils.ui.UnitUtils;

public class Font implements Parcelable {

    private String size;
    private String style;
    private String family;
    private String weight;
    private String lineHeight;

    protected Font(Parcel in) {
        size = in.readString();
        style = in.readString();
        family = in.readString();
        weight = in.readString();
        lineHeight = in.readString();
    }

    public static final Creator<Font> CREATOR = new Creator<Font>() {
        @Override
        public Font createFromParcel(Parcel in) {
            return new Font(in);
        }

        @Override
        public Font[] newArray(int size) {
            return new Font[size];
        }
    };

    public String getStyle() {
        return style;
    }

    public String getFamily() {
        return family;
    }

    public String getWeight() {
        return weight;
    }

    public String getLineHeight() {
        return lineHeight;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(size);
        dest.writeString(style);
        dest.writeString(family);
        dest.writeString(weight);
        dest.writeString(lineHeight);
    }

    public int getSize() {
        return UnitUtils.getCalculatedPixel(size);
    }
}
