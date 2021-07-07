package com.letscooee.models.v3.block;

import android.os.Parcel;
import android.os.Parcelable;

public class Animation implements Parcelable {

    protected Animation(Parcel in) {
        enter = EntranceAnimation.valueOf(in.readString());
        exit = ExitAnimation.valueOf(in.readString());
    }

    public static final Creator<Animation> CREATOR = new Creator<Animation>() {
        @Override
        public Animation createFromParcel(Parcel in) {
            return new Animation(in);
        }

        @Override
        public Animation[] newArray(int size) {
            return new Animation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(enter.name());
        dest.writeString(exit.name());
    }

    public enum EntranceAnimation {
        SLIDE_IN_TOP, SLIDE_IN_DOWN, SLIDE_IN_LEFT, SLIDE_IN_RIGHT
    }

    public enum ExitAnimation {
        SLIDE_OUT_TOP, SLIDE_OUT_DOWN, SLIDE_OUT_LEFT, SLIDE_OUT_RIGHT
    }

    private EntranceAnimation enter;
    private ExitAnimation exit;

    public EntranceAnimation getEnter() {
        return enter;
    }

    public ExitAnimation getExit() {
        return exit;
    }

}
