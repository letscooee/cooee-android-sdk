package com.letscooee.models.trigger;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.models.trigger.inapp.InAppTrigger;
import com.letscooee.models.trigger.push.PushNotificationTrigger;
import com.letscooee.utils.Constants;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TriggerData implements Parcelable {

    private final String id;

    @SerializedName("v")
    @Expose
    private final double version;
    private InAppTrigger ian;
    private final PushNotificationTrigger pn;
    private final String engagementID;
    private final boolean internal;
    private final Map<String, Object> config = new HashMap<>();
    private final long expireAt;

    @SerializedName("sentAt")
    @Expose
    private final Date sentAt;

    @SerializedName("ar")
    @Expose
    private Map<String, Object> selfARData = new HashMap<>();

    @SerializedName("features")
    @Expose
    private final ArrayList<Integer> features;

    /**
     * No longer used and is replaced by {@link #expireAt}. This was used to append time after notification was
     * received.
     * But {@link #expireAt} would be used to sent a definite time of expiry.
     */
    @Deprecated
    private final long duration;

    private final long pushNotificationID = new Date().getTime();

    protected TriggerData(Parcel in) {
        id = in.readString();
        version = in.readDouble();
        duration = in.readLong();
        ian = in.readParcelable(InAppTrigger.class.getClassLoader());
        pn = in.readParcelable(PushNotificationTrigger.class.getClassLoader());
        engagementID = in.readString();
        internal = in.readByte() != 0;

        // TODO: 04/03/22 readMap(Map, ClassLoader) is deprecated in android API Tiramisu.
        //  And readMap(Map, ClassLoader, Class<Key>, Class<Value>) is added.
        //  New Method Ref: https://developer.android.com/reference/android/os/Parcel#readMap(java.util.Map%3C?%20super%20K,%20?%20super%20V%3E,%20java.lang.ClassLoader,%20java.lang.Class%3CK%3E,%20java.lang.Class%3CV%3E)
        //  Old Method Ref: https://developer.android.com/reference/android/os/Parcel#readMap(java.util.Map,%20java.lang.ClassLoader)
        in.readMap(config, Object.class.getClassLoader());
        expireAt = in.readLong();
        in.readMap(selfARData, Object.class.getClassLoader());
        sentAt = (Date) in.readSerializable();
        features = in.readArrayList(Integer.class.getClassLoader());
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

    public long getNotificationID() {
        return this.pushNotificationID;
    }

    public double getVersion() {
        return version;
    }

    public InAppTrigger getInAppTrigger() {
        return ian;
    }

    public void setInAppTrigger(InAppTrigger ian) {
        this.ian = ian;
    }

    @Deprecated
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

    public long getExpireAt() {
        return expireAt;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public Map<String, Object> getARData() {
        return selfARData;
    }

    @NonNull
    public ArrayList<Integer> getFeatures() {
        return features == null ? new ArrayList<>() : features;
    }

    public void setSelfARData(Map<String, Object> selfARData) {
        this.selfARData = selfARData;
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
        dest.writeLong(expireAt);
        dest.writeMap(selfARData);
        dest.writeSerializable(sentAt);
        dest.writeList(features);
    }

    @NonNull
    @Override
    public String toString() {
        return "Trigger{" +
                "id='" + id + '\'' +
                '}';
    }

    public boolean isCurrentlySupported() {
        return version >= 4 && version < 5;
    }

    /**
     * Checks if IN_APP or SELF_AR features are present or not.
     * <ul>
     *     <li>If its null, will allow to load InApp from server</li>
     *     <li>If its empty, will allow to load InApp from server</li>
     *     <li>If its present, Will loop and check if there is any feature except PN is present or not.</li>
     *     <ol>
     *         <li>If present, Will allow loading data from server</li>
     *     </ol>
     * </ul>
     *
     * @return true if InApp/AR is present, false otherwise.
     */
    public boolean shouldLazyLoad() {
        if (getFeatures().isEmpty()) {
            return true;
        }

        if (getFeatures().contains(Constants.FEATURE_IN_APP) && getInAppTrigger() == null) {
            return true;
        }
        // TODO add check for self AR object
        return getFeatures().contains(Constants.FEATURE_SELF_AR);
    }

    public boolean isContainValidData() {
        return !TextUtils.isEmpty(id) && getInAppTrigger() != null && getInAppTrigger().isContainValidData();
    }
}
