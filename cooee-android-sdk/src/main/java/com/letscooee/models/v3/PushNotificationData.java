package com.letscooee.models.v3;

import android.os.Parcel;
import android.os.Parcelable;

import com.letscooee.models.v3.elemeent.Children;

public class PushNotificationData extends CoreTriggerData implements Parcelable {

    private Children title;
    private Children body;
    private String smallImage;
    private String largeImage;

    protected PushNotificationData(Parcel in) {
        super(in);
        title = in.readParcelable(Children.class.getClassLoader());
        body = in.readParcelable(Children.class.getClassLoader());
        smallImage = in.readString();
        largeImage = in.readString();
    }

    public static final Creator<PushNotificationData> CREATOR = new Creator<PushNotificationData>() {
        @Override
        public PushNotificationData createFromParcel(Parcel in) {
            return new PushNotificationData(in);
        }

        @Override
        public PushNotificationData[] newArray(int size) {
            return new PushNotificationData[size];
        }
    };

    public Children getTitle() {
        return title;
    }

    public Children getBody() {
        return body;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public String getLargeImage() {
        return largeImage;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(title, flags);
        dest.writeParcelable(body, flags);
        dest.writeString(smallImage);
        dest.writeString(largeImage);
    }
}
