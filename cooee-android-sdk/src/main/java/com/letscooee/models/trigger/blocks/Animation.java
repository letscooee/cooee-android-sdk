package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Animation implements Parcelable {

    public enum EntranceAnimation {
        NONE(1),
        SLIDE_IN_TOP(2),
        SLIDE_IN_DOWN(3),
        SLIDE_IN_LEFT(4),
        SLIDE_IN_RIGHT(5),
        SLIDE_IN_TOP_LEFT(6),
        SLIDE_IN_TOP_RIGHT(7),
        SLIDE_IN_BOTTOM_LEFT(8),
        SLIDE_IN_BOTTOM_RIGHT(9);

        private final int id;

        EntranceAnimation(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum ExitAnimation {
        NONE(1),
        SLIDE_OUT_TOP(2),
        SLIDE_OUT_DOWN(3),
        SLIDE_OUT_LEFT(4),
        SLIDE_OUT_RIGHT(5),
        SLIDE_OUT_TOP_LEFT(6),
        SLIDE_OUT_TOP_RIGHT(7),
        SLIDE_OUT_BOTTOM_LEFT(8),
        SLIDE_OUT_BOTTOM_RIGHT(9);

        private final int id;

        ExitAnimation(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    @SerializedName("en")
    @Expose
    private final EntranceAnimation enter;

    @SerializedName("ex")
    @Expose
    private final ExitAnimation exit;

    protected Animation(Parcel in) {
        enter = (EntranceAnimation) in.readSerializable();
        exit = (ExitAnimation) in.readSerializable();
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
        dest.writeSerializable(enter);
        dest.writeSerializable(exit);
    }

    public EntranceAnimation getEnter() {
        return enter;
    }

    public ExitAnimation getExit() {
        return exit;
    }

}
