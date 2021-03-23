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
    private int opacity;
    private int radius;
    private TriggerButtonAction action;

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
        opacity = in.readInt();
        type = TriggerType.valueOf(in.readString());
        radius = in.readInt();
        action=in.readParcelable(TriggerButtonAction.class.getClassLoader());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(color);
        dest.writeString(image);
        dest.writeInt(opacity);
        dest.writeString(type.name());
        dest.writeInt(radius);
        dest.writeParcelable(action,flags);
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

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public int getRadius() {
        return radius;
    }

    public TriggerButtonAction getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "TriggerBackground{" +
                "type=" + type +
                ", color='" + color + '\'' +
                ", image='" + image + '\'' +
                ", opacity=" + opacity +
                ", radius=" + radius +
                '}';
    }
}
