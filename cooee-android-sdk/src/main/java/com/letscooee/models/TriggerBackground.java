package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for storing engagement data from server
 *
 * @author Abhishek Taparia
 */
public class TriggerBackground implements Parcelable {
    public enum TriggerType {
        SOLID_COLOR, IMAGE, BLURRED
    }

    private TriggerType type;
    private String color;
    private String image;
    private int blur;

    public static final Creator<TriggerBackground> CREATOR = new Creator<TriggerBackground>() {
        @Override
        public TriggerBackground createFromParcel(Parcel in) {
            return new TriggerBackground(in);
        }

        @Override
        public TriggerBackground[] newArray(int size) {
            return new TriggerBackground[size];
        }
    };

    public TriggerBackground() {
    }

    protected TriggerBackground(Parcel in) {
        color = in.readString();
        image = in.readString();
        blur = in.readInt();
        type = TriggerType.valueOf(in.readString());
    }

    public TriggerBackground(String type, String color, String image, String blur) {
        this.color = color;
        this.image = image;
        this.blur = Integer.parseInt(blur);
        this.type = TriggerType.valueOf(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(color);
        dest.writeString(image);
        dest.writeInt(blur);
        dest.writeString(type.name());
    }

    public TriggerType getType() {
        return type;
    }

    public void setType(TriggerType type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getBlur() {
        return blur;
    }

    public void setBlur(int blur) {
        this.blur = blur;
    }
}
