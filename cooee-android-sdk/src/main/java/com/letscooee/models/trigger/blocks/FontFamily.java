package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java9.util.stream.StreamSupport;

/**
 * Font-family which holds all fonts in the family
 *
 * @author Ashish Gaikwad
 * @since 1.4.2
 */
public class FontFamily implements Parcelable {

    public static final Creator<FontFamily> CREATOR = new Creator<FontFamily>() {
        @Override
        public FontFamily createFromParcel(Parcel in) {
            return new FontFamily(in);
        }

        @Override
        public FontFamily[] newArray(int size) {
            return new FontFamily[size];
        }
    };

    private final List<SDKFont> fonts;
    private final String name;

    protected FontFamily(Parcel in) {
        name = in.readString();
        fonts = in.createTypedArrayList(SDKFont.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(fonts);
    }

    public List<String> getFontURLs() {
        if (fonts == null || fonts.isEmpty()) {
            return new ArrayList<>();
        }

        return StreamSupport.stream(fonts)
                .map(SDKFont::getUrl)
                .filter(u -> !TextUtils.isEmpty(u))
                .toList();
    }

    @SuppressWarnings("unused")
    public List<SDKFont> getFonts() {
        return fonts;
    }

    public String getName() {
        return name;
    }
}
