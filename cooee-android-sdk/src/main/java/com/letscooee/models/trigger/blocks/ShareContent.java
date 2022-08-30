package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holds data for share content.
 *
 * @author Ashish Gaikwad 06/07/22
 * @since 1.3.12
 */
public class ShareContent implements Parcelable {

    @SerializedName("text")
    @Expose
    private final String content;

    protected ShareContent(Parcel in) {
        content = in.readString();
    }

    public static final Creator<ShareContent> CREATOR = new Creator<ShareContent>() {
        @Override
        public ShareContent createFromParcel(Parcel in) {
            return new ShareContent(in);
        }

        @Override
        public ShareContent[] newArray(int size) {
            return new ShareContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
    }

    public String getContent() {
        return content;
    }
}
