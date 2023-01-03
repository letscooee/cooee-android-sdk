package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.letscooee.enums.trigger.FontStyle;

/**
 * Class that holds font url with its {@link FontStyle}
 *
 * @author Ashish Gaikwad
 * @since 1.4.2
 */
public class SDKFont implements Parcelable {

    public static final Creator<SDKFont> CREATOR = new Creator<SDKFont>() {
        @Override
        public SDKFont createFromParcel(Parcel in) {
            return new SDKFont(in);
        }

        @Override
        public SDKFont[] newArray(int size) {
            return new SDKFont[size];
        }
    };

    private final FontStyle style;
    private final String url;

    protected SDKFont(Parcel in) {
        url = in.readString();
        style = (FontStyle) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeSerializable(style);
    }

    public FontStyle getStyle() {
        return style;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @NonNull
    @Override
    public String toString() {
        return "SDKFont{" +
                "style=" + style +
                ", url='" + url + '\'' +
                '}';
    }
}
