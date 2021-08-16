package com.letscooee.models.trigger.blocks;

import static com.letscooee.utils.Constants.UNIT_PIXEL;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.letscooee.enums.trigger.Style;
import com.letscooee.utils.ui.UnitUtils;

public class Font implements Parcelable {

    private static final int DEFAULT_SIZE = 15;

    private final String size;
    private final Style style;
    private final String name;
    private final String lineHeight;

    protected Font(Parcel in) {
        size = in.readString();
        style = (Style) in.readSerializable();
        name = in.readString();
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

    public int getTypefaceStyle() {
        return (style == null ? Style.NORMAL : style).typeface;
    }

    /**
     * Get the font family name i.e. Typeface file name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    public Float getLineHeight(float fontSize) {
        if (TextUtils.isEmpty(lineHeight)) return null;

        if (lineHeight.contains(UNIT_PIXEL)) {
            return UnitUtils.parseToFloat(lineHeight, UNIT_PIXEL);
        }

        float value = Float.parseFloat(lineHeight);

        return fontSize * value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(size);
        dest.writeSerializable(style);
        dest.writeString(name);
        dest.writeString(lineHeight);
    }

    public int getSize() {
        if (TextUtils.isEmpty(size)) return DEFAULT_SIZE;
        return UnitUtils.getCalculatedPixel(size);
    }
}
