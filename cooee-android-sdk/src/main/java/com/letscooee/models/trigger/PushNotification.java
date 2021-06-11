package com.letscooee.models.trigger;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model which represents "pn" (push notification) related data in the incoming payload
 * of engagement trigger.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class PushNotification implements Parcelable {

    public PushNotificationImportance importance = PushNotificationImportance.HIGH;
    public boolean vibrate = true;
    public boolean sound = true;
    public boolean lights = true;

    protected PushNotification(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PushNotification> CREATOR = new Creator<PushNotification>() {
        @Override
        public PushNotification createFromParcel(Parcel in) {
            return new PushNotification(in);
        }

        @Override
        public PushNotification[] newArray(int size) {
            return new PushNotification[size];
        }
    };
}
