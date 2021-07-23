package com.letscooee.models.v3;

import android.os.Parcel;
import android.os.Parcelable;

import com.letscooee.models.v3.inapp.InAppData;

public class CoreTriggerData implements Parcelable {

    private String id;
    private double version;
    private long duration;
    private InAppData ian;
    private PushNotificationData pn;


    protected CoreTriggerData(Parcel in) {
        id = in.readString();
        version = in.readDouble();
        duration = in.readLong();
        ian = in.readParcelable(InAppData.class.getClassLoader());
        pn = in.readParcelable(PushNotificationData.class.getClassLoader());
    }

    public static final Creator<CoreTriggerData> CREATOR = new Creator<CoreTriggerData>() {
        @Override
        public CoreTriggerData createFromParcel(Parcel in) {
            return new CoreTriggerData(in);
        }

        @Override
        public CoreTriggerData[] newArray(int size) {
            return new CoreTriggerData[size];
        }
    };

    public PushNotificationData getPn() {
        return pn;
    }

    public String getId() {
        return id;
    }

    public double getVersion() {
        return version;
    }

    public InAppData getIan() {
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
