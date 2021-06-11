package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Abhishek Taparia
 */
public class TriggerCloseBehaviour implements Parcelable {

    public enum Position {
        TOP_RIGHT, TOP_LEFT, DOWN_RIGHT, DOWN_LEFT
    }

    private boolean auto;
    private Position position;
    private int timeToClose;
    private String closeButtonColor;
    private String countDownTextColor;
    private String progressBarColor;
    private boolean show;

    protected TriggerCloseBehaviour(Parcel in) {
        auto = in.readByte() != 0;
        timeToClose = in.readInt();
        closeButtonColor = in.readString();
        countDownTextColor = in.readString();
        progressBarColor = in.readString();
        position = Position.valueOf(in.readString());
        show = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (auto ? 1 : 0));
        dest.writeInt(timeToClose);
        dest.writeString(closeButtonColor);
        dest.writeString(countDownTextColor);
        dest.writeString(progressBarColor);
        dest.writeString(position.name());
        dest.writeByte((byte) (show ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TriggerCloseBehaviour> CREATOR = new Creator<TriggerCloseBehaviour>() {
        @Override
        public TriggerCloseBehaviour createFromParcel(Parcel in) {
            return new TriggerCloseBehaviour(in);
        }

        @Override
        public TriggerCloseBehaviour[] newArray(int size) {
            return new TriggerCloseBehaviour[size];
        }
    };

    public boolean isAuto() {
        return auto;
    }

    public Position getPosition() {
        return position;
    }

    public int getTimeToClose() {
        return timeToClose;
    }

    public String getCloseButtonColor() {
        return closeButtonColor;
    }

    public String getCountDownTextColor() {
        return countDownTextColor;
    }

    public String getProgressBarColor() {
        return progressBarColor;
    }

    public boolean shouldShowButton() {
        return show;
    }

    @Override
    public String toString() {
        return "TriggerCloseBehaviour{" +
                "auto=" + auto +
                ", position=" + position +
                '}';
    }
}
