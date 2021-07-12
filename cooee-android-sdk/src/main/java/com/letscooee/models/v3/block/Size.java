package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

import com.letscooee.utils.Constants;

import static com.letscooee.utils.Constants.PERCENT;
import static com.letscooee.utils.Constants.PIXEL;
import static com.letscooee.utils.Constants.VIEWPORT_HEIGHT;
import static com.letscooee.utils.Constants.VIEWPORT_WIDTH;
import static com.letscooee.utils.ValueUtil.getCalculatedValue;

public class Size implements Parcelable {

    protected Size(Parcel in) {
        width = in.readString();
        height = in.readString();
        maxWidth = in.readString();
        maxHeight = in.readString();
        display = Display.valueOf(in.readString());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(width);
        dest.writeString(height);
        dest.writeString(maxWidth);
        dest.writeString(maxHeight);
        dest.writeString(display.name());
    }

    public int getCalculatedHeight(int deviceWidth, int deviceHeight) {

        return getCalculatedValue(deviceWidth, deviceHeight, getHeight());
    }


    public int getCalculatedWidth(int deviceWidth, int deviceHeight) {

        return getCalculatedValue(deviceWidth, deviceHeight, getWidth());
    }

    public enum Display {BLOCK, INLINE_BLOCK}

    private String width;
    private String height;
    private String maxWidth;
    private String maxHeight;
    private Display display;

    public Display getDisplay() {
        return display;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height.toLowerCase();
    }

    public String getMaxWidth() {
        return maxWidth;
    }

    public String getMaxHeight() {
        return maxHeight;
    }
}
