package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Background implements Parcelable {

    @SerializedName("s")
    @Expose
    private Colour solid;

    @SerializedName("g")
    @Expose
    private Glassmorphism glassmorphism;

    @SerializedName("i")
    @Expose
    private Image image;

    protected Background(Parcel in) {
        solid = in.readParcelable(Colour.class.getClassLoader());
        glassmorphism = in.readParcelable(Glassmorphism.class.getClassLoader());
        image = in.readParcelable(Image.class.getClassLoader());
    }

    public static final Creator<Background> CREATOR = new Creator<Background>() {
        @Override
        public Background createFromParcel(Parcel in) {
            return new Background(in);
        }

        @Override
        public Background[] newArray(int size) {
            return new Background[size];
        }
    };

    public Colour getSolid() {
        return solid;
    }

    public Glassmorphism getGlassmorphism() {
        return glassmorphism;
    }

    public Image getImage() {
        return image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(solid, flags);
        dest.writeParcelable(glassmorphism, flags);
        dest.writeParcelable(image, flags);
    }
}
