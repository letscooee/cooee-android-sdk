package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.utils.PermissionType;

import java.util.HashMap;
import java.util.Map;

public class ClickAction implements Parcelable {

    private BrowserContent iab;

    @SerializedName("ext")
    @Expose
    private BrowserContent external;

    @SerializedName("updt")
    @Expose
    private BrowserContent updateApp;

    @SerializedName("pmpt")
    @Expose
    private PermissionType prompt;
    private HashMap<String, Object> up;
    private HashMap<String, Object> kv;
    private HashMap<String, Object> share;
    private boolean close;

    @SerializedName("ntvAR")
    @Expose
    private AppAR appAR;

    @SerializedName("open")
    private int launchFeature;

    /**
     * Initialize the {@link ClickAction} with close variable
     * Main purpose of this constructor is to initialise click listener for InApp's main background
     * click event
     *
     * @param close <code>boolean</code> value
     */
    public ClickAction(boolean close) {
        this.close = close;
    }

    protected ClickAction(Parcel in) {
        iab = in.readParcelable(BrowserContent.class.getClassLoader());
        external = in.readParcelable(BrowserContent.class.getClassLoader());
        updateApp = in.readParcelable(BrowserContent.class.getClassLoader());
        prompt = (PermissionType) in.readSerializable();
        close = in.readByte() != 0;

        // TODO: 04/03/22 readMap(Map, ClassLoader) is deprecated in android API Tiramisu.
        //  And readMap(Map, ClassLoader, Class<Key>, Class<Value>) is added.
        //  New Method Ref: https://developer.android.com/reference/android/os/Parcel#readMap(java.util.Map%3C?%20super%20K,%20?%20super%20V%3E,%20java.lang.ClassLoader,%20java.lang.Class%3CK%3E,%20java.lang.Class%3CV%3E)
        //  Old Method Ref: https://developer.android.com/reference/android/os/Parcel#readMap(java.util.Map,%20java.lang.ClassLoader)
        up = new HashMap<>();
        in.readMap(up, Object.class.getClassLoader());
        kv = new HashMap<>();
        in.readMap(kv, Object.class.getClassLoader());
        share = new HashMap<>();
        in.readMap(share, Object.class.getClassLoader());
        appAR = in.readParcelable(AppAR.class.getClassLoader());
        launchFeature = in.readInt();
    }

    public static final Creator<ClickAction> CREATOR = new Creator<ClickAction>() {
        @Override
        public ClickAction createFromParcel(Parcel in) {
            return new ClickAction(in);
        }

        @Override
        public ClickAction[] newArray(int size) {
            return new ClickAction[size];
        }
    };

    public BrowserContent getIab() {
        return iab;
    }

    public BrowserContent getExternal() {
        return external;
    }

    public BrowserContent getUpdateApp() {
        return updateApp;
    }

    public PermissionType getPrompt() {
        return prompt;
    }

    public Map<String, Object> getUserPropertiesToUpdate() {
        return up;
    }

    public Map<String, Object> getKeyValue() {
        return kv;
    }

    public Map<String, Object> getShare() {
        return share;
    }

    public AppAR getAR() {
        return appAR;
    }

    public boolean isClose() {
        return close;
    }

    /**
     * Tells if CTA is only for closing In-App
     *
     * @return <code>true</code> if all other CTA's are <code>null</code>; Otherwise <code>false</code>
     */
    public boolean isOnlyCloseCTA() {
        return iab == null && external == null && updateApp == null && prompt == null
                && (up == null || up.isEmpty()) && (kv == null || kv.isEmpty()) && (share == null || share.isEmpty()) && launchFeature == 0;
    }

    public int getLaunchFeature() {
        return launchFeature;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(iab, flags);
        dest.writeParcelable(external, flags);
        dest.writeParcelable(updateApp, flags);
        dest.writeSerializable(prompt);
        dest.writeByte((byte) (close ? 1 : 0));
        dest.writeMap(up);
        dest.writeMap(kv);
        dest.writeMap(share);
        dest.writeParcelable(appAR, flags);
        dest.writeInt(launchFeature);
    }
}
