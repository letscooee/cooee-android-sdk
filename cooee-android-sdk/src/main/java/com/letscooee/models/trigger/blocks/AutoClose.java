package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holds auto close properties of InApp
 *
 * @author Ashish Gaikwad
 * @since 1.4.2
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AutoClose implements Parcelable {

    public static final Creator<AutoClose> CREATOR = new Creator<AutoClose>() {
        @Override
        public AutoClose createFromParcel(Parcel in) {
            return new AutoClose(in);
        }

        @Override
        public AutoClose[] newArray(int size) {
            return new AutoClose[size];
        }
    };

    @SerializedName("c")
    @Expose
    private final String progressBarColour;

    @SerializedName("sec")
    @Expose
    private final int seconds;

    @SerializedName("v")
    @Expose
    private final boolean hideProgress;

    protected AutoClose(Parcel in) {
        seconds = in.readInt();
        progressBarColour = in.readString();
        hideProgress = in.readByte() == 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seconds);
        dest.writeString(progressBarColour);
        dest.writeByte((byte) (hideProgress ? 0 : 1));
    }

    @Nullable
    public String getProgressBarColour() {
        return progressBarColour;
    }

    public int getSeconds() {
        return seconds;
    }

    public boolean isHideProgress() {
        return hideProgress;
    }

}
