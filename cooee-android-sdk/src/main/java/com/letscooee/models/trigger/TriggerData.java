package com.letscooee.models.trigger;

import android.os.Parcel;
import android.os.Parcelable;

import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.models.trigger.push.PushNotificationTrigger;

import java.util.HashMap;
import java.util.Map;

public class TriggerData implements Parcelable {

    private final String id;
    private final double version;
    private final long duration;
    private final InAppTrigger ian;
    private final PushNotificationTrigger pn;
    private final String engagementID;
    private final boolean internal;
    private final Map<String, Object> config;

    public TriggerData(String id, InAppTrigger ian) {
        this.id = id;
        this.ian = ian;
        this.version = 3;
        this.duration = 0;
        this.pn = null;
        this.engagementID = null;
        this.internal = false;
        this.config = null;
    }

    protected TriggerData(Parcel in) {
        id = in.readString();
        version = in.readDouble();
        duration = in.readLong();
        ian = in.readParcelable(InAppTrigger.class.getClassLoader());
        pn = in.readParcelable(PushNotificationTrigger.class.getClassLoader());
        engagementID = in.readString();
        internal = in.readByte() != 0;
        config = new HashMap<>();
        in.readMap(config, Object.class.getClassLoader());
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

    public InAppTrigger getInAppTrigger() {
        return ian;
    }

    public long getDuration() {
        return duration;
    }

    public String getEngagementID() {
        return engagementID;
    }

    public Boolean getInternal() {
        return internal;
    }

    public Map<String, Object> getConfig() {
        return config;
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
        dest.writeString(engagementID);
        dest.writeByte((byte) (internal ? 1 : 0));
        dest.writeMap(config);
    }
}
