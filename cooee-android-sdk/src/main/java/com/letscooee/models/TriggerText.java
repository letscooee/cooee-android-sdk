package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for storing engagement data from server
 *
 * @author Abhishek Taparia
 */
public class TriggerText implements Parcelable {
    public enum Position {
        TOP, BOTTOM, LEFT, RIGHT
    }
    private String text;
    private String notificationText;
    private String color;
    private int size;
    private Position position;

    protected TriggerText(Parcel in) {
        text = in.readString();
        notificationText = in.readString();
        color = in.readString();
        size = in.readInt();
        position = Position.valueOf(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(notificationText);
        dest.writeString(color);
        dest.writeInt(size);
        dest.writeString(position.name());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TriggerText> CREATOR = new Creator<TriggerText>() {
        @Override
        public TriggerText createFromParcel(Parcel in) {
            return new TriggerText(in);
        }

        @Override
        public TriggerText[] newArray(int size) {
            return new TriggerText[size];
        }
    };

    public String getText() {
        return text;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public String getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "TriggerText{" +
                "text='" + text + '\'' +
                ", notificationText='" + notificationText + '\'' +
                ", color='" + color + '\'' +
                ", size=" + size +
                ", position='" + position + '\'' +
                '}';
    }
}
