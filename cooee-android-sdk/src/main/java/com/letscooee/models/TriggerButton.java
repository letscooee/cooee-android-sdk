package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Abhishek Taparia
 */
public class TriggerButton implements Parcelable {
    private int radius;
    private boolean showInPN;
    private boolean launchApp;

    private String text;
    private String notificationText;
    private String background;
    private String color;
    private TriggerButtonAction action;

    protected TriggerButton(Parcel in) {
        radius = in.readInt();
        showInPN = in.readByte() != 0;
        launchApp = in.readByte() != 0;
        text = in.readString();
        notificationText = in.readString();
        background = in.readString();
        color = in.readString();
        action = in.readParcelable(TriggerButtonAction.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(radius);
        dest.writeByte((byte) (showInPN ? 1 : 0));
        dest.writeByte((byte) (launchApp ? 1 : 0));
        dest.writeString(text);
        dest.writeString(notificationText);
        dest.writeString(background);
        dest.writeString(color);
        dest.writeParcelable(action, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TriggerButton> CREATOR = new Creator<TriggerButton>() {
        @Override
        public TriggerButton createFromParcel(Parcel in) {
            return new TriggerButton(in);
        }

        @Override
        public TriggerButton[] newArray(int size) {
            return new TriggerButton[size];
        }
    };

    public int getRadius() {
        return radius;
    }

    public boolean isShowInPN() {
        return showInPN;
    }

    public boolean isLaunchApp() {
        return launchApp;
    }

    public String getText() {
        return text;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public String getBackground() {
        return background;
    }

    public String getColor() {
        return color;
    }

    public TriggerButtonAction getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "TriggerButton{" +
                "radius=" + radius +
                ", showInPN=" + showInPN +
                ", launchApp=" + launchApp +
                ", text='" + text + '\'' +
                ", notificationText='" + notificationText + '\'' +
                ", background='" + background + '\'' +
                ", color='" + color + '\'' +
                ", action=" + action +
                '}';
    }
}
