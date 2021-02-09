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

    protected TriggerCloseBehaviour(Parcel in) {
        auto = in.readByte() != 0;
        timeToClose = in.readInt();
        position = Position.valueOf(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (auto ? 1 : 0));
        dest.writeInt(timeToClose);
        dest.writeString(position.name());
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

    @Override
    public String toString() {
        return "TriggerCloseBehaviour{" +
                "auto=" + auto +
                ", position=" + position +
                '}';
    }
}
