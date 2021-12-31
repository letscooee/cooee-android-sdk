package com.letscooee.models.trigger.blocks;

import static com.letscooee.utils.Constants.UNIT_PIXEL;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.trigger.Style;
import com.letscooee.utils.ui.UnitUtils;

public class Font implements Parcelable {

    private static final int DEFAULT_SIZE = 15;

    @SerializedName("s")
    @Expose
    private final Float size;
    private final Style style;

    @SerializedName("tf")
    @Expose
    private final String name;

    @SerializedName("ff")
    @Expose
    private final String fontFamily;

    @SerializedName("lh")
    @Expose
    private final String lineHeight;

    protected Font(Parcel in) {
        size = in.readFloat();
        style = (Style) in.readSerializable();
        name = in.readString();
        lineHeight = in.readString();
        fontFamily = in.readString();
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

    public Float getLineHeight() {
        if (TextUtils.isEmpty(lineHeight)) return null;

        if (lineHeight.contains(UNIT_PIXEL)) {
            return UnitUtils.parseToFloat(lineHeight, UNIT_PIXEL);
        }

        return Float.parseFloat(lineHeight);
    }

    public boolean hasUnit() {
        return lineHeight.contains(UNIT_PIXEL);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(size);
        dest.writeSerializable(style);
        dest.writeString(name);
        dest.writeString(lineHeight);
        dest.writeString(fontFamily);
    }

    public float getSize() {
        if (size == null) return DEFAULT_SIZE;
        return UnitUtils.getScaledPixel(size);
    }

    public String getFontFamily() {
        return fontFamily;
    }
}
