package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SidePopSetting implements Parcelable {

    protected SidePopSetting(Parcel in) {
        imageUrl = in.readString();
        text = in.readString();
        textColor = in.readString();
        textSize = in.readDouble();
        backgroundColor = in.readString();
        type = Type.valueOf(in.readString());
        position = Position.valueOf(in.readString());
        action = in.readParcelable(TriggerButtonAction.class.getClassLoader());
    }

    public static final Creator<SidePopSetting> CREATOR = new Creator<SidePopSetting>() {
        @Override
        public SidePopSetting createFromParcel(Parcel in) {
            return new SidePopSetting(in);
        }

        @Override
        public SidePopSetting[] newArray(int size) {
            return new SidePopSetting[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imageUrl);
        parcel.writeString(text);
        parcel.writeString(textColor);
        parcel.writeDouble(textSize);
        parcel.writeString(backgroundColor);
        parcel.writeString(type.name());
        parcel.writeString(position.name());
        parcel.writeParcelable(action, i);
    }

    public enum Type {
        TEXT, IMAGE
    }

    public enum Position {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER, LEFT_CENTER, RIGHT_CENTER, BOTTOM_CENTER, TOP_CENTER
    }

    private Type type;
    private String imageUrl;
    private String text;
    private String textColor;
    private double textSize;
    private String backgroundColor;
    private Position position;
    private TriggerButtonAction action;


    public Type getType() {
        return type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }

    public String getTextColor() {
        return textColor;
    }

    public double getTextSize() {
        return textSize;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public Position getPosition() {
        return position;
    }

    public TriggerButtonAction getAction() {
        return action;
    }
}
