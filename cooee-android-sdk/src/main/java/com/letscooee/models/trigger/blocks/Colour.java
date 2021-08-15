package com.letscooee.models.trigger.blocks;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Colour implements Parcelable {

    public static final Creator<Colour> CREATOR = new Creator<Colour>() {
        @Override
        public Colour createFromParcel(Parcel in) {
            return new Colour(in);
        }

        @Override
        public Colour[] newArray(int size) {
            return new Colour[size];
        }
    };

    private final String hex;
    private final Gradient grad;

    protected Colour(Parcel in) {
        hex = in.readString();
        grad = in.readParcelable(Gradient.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hex);
        dest.writeParcelable(grad, flags);
    }

    public int getSolidColor() {
        if (TextUtils.isEmpty(hex)) return Color.TRANSPARENT;
        return Color.parseColor(hex);
    }

    public void updateDrawable(GradientDrawable drawable) {
        if (grad == null) {
            drawable.setColor(getSolidColor());
        } else {
            grad.updateDrawable(drawable);
        }
    }
}
