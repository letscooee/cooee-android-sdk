package com.letscooee.models.trigger;

import android.os.Parcel;
import android.os.Parcelable;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.models.trigger.push.PushNotificationTrigger;

public class TriggerData implements Parcelable {

    private final String id;
    private final double version;
    private final long duration;
    private final InAppTrigger ian;
    private final PushNotificationTrigger pn;

    protected TriggerData(Parcel in) {
        id = in.readString();
        version = in.readDouble();
        duration = in.readLong();
        ian = in.readParcelable(InAppTrigger.class.getClassLoader());
        pn = in.readParcelable(PushNotificationTrigger.class.getClassLoader());
    }

    public static final Creator<TriggerData> CREATOR = new Creator<TriggerData>() {
        @Override
        public TriggerData createFromParcel(Parcel in) {
            return new TriggerData(in);
        }

        @Override
        public TriggerData[] newArray(int size) {
            return new TriggerData[size];
        }
    };

    public PushNotificationTrigger getPn() {
        return pn;
    }

    public String getId() {
        return id;
    }

    public double getVersion() {
        return version;
    }

    public InAppTrigger getIan() {
        return ian;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(version);
        dest.writeLong(duration);
        dest.writeParcelable(ian, flags);
        dest.writeParcelable(pn, flags);
    }
}
