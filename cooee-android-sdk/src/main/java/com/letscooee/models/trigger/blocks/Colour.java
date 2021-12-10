package com.letscooee.models.trigger.blocks;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Colour implements Parcelable {

    @SerializedName("h")
    @Expose
    private final String hex;

    @SerializedName("a")
    @Expose
    private final int alpha;

    @SerializedName("g")
    @Expose
    private final Gradient grad;

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

    public Colour() {
        hex = "#000000";
        grad = null;
        alpha = 100;
    }

    protected Colour(Parcel in) {
        hex = in.readString();
        alpha = in.readInt();
        grad = in.readParcelable(Gradient.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hex);
        dest.writeInt(alpha);
        dest.writeParcelable(grad, flags);
    }

    public int getHexColor() {
        if (TextUtils.isEmpty(hex)) return Color.TRANSPARENT;

        String aRBG = "#" + percentToHex() + hex.replace("#", "");
        return Color.parseColor(aRBG);
    }

    public void updateDrawable(GradientDrawable drawable) {
        if (grad == null) {
            drawable.setColor(getHexColor());
        } else {
            grad.updateDrawable(drawable);
        }
    }

    @NonNull
    private String percentToHex() {
        int decimal = alpha * 255 / 100;
        String stringAlpha = Integer.toHexString(decimal);

        return stringAlpha.length() < 2 ? "0" + stringAlpha : stringAlpha;
    }
}
