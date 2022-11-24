package com.letscooee.models.trigger.blocks;

import android.os.Parcel;

/**
 * Holds Text show properties like position etc.
 *
 * @author Ashish Gaikwwad
 * @since 1.4.2
 */
public class TextShadow extends Shadow {

    public static final Creator<TextShadow> CREATOR = new Creator<TextShadow>() {
        @Override
        public TextShadow createFromParcel(Parcel in) {
            return new TextShadow(in);
        }

        @Override
        public TextShadow[] newArray(int size) {
            return new TextShadow[size];
        }
    };
    final private int x;
    final private int y;

    protected TextShadow(Parcel in) {
        super(in);
        x = in.readInt();
        y = in.readInt();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(x);
        dest.writeInt(y);
    }
}
