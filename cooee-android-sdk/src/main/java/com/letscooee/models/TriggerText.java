package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for storing engagement data from server
 *
 * @author Abhishek Taparia
 */
public class TriggerText implements Parcelable {
    private String data;
    private String color;
    private int fontSize;

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

    public TriggerText() {
    }

    protected TriggerText(Parcel in) {
        data = in.readString();
        color = in.readString();
        fontSize = in.readInt();
    }

    public TriggerText(String data, String color, String size) {
        this.data = data;
        this.color = color;
        this.fontSize = Integer.parseInt(size);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data);
        dest.writeString(color);
        dest.writeInt(fontSize);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
}
