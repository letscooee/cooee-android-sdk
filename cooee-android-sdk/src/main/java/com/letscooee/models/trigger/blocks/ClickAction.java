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

    // Main purpose of this constructor is to initialise click listener for InApp's main background
    // click event
    public ClickAction(boolean close) {
        this.close = close;
    }

    protected ClickAction(Parcel in) {
        iab = in.readParcelable(BrowserContent.class.getClassLoader());
        external = in.readParcelable(BrowserContent.class.getClassLoader());
        updateApp = in.readParcelable(BrowserContent.class.getClassLoader());
        prompt = (PermissionType) in.readSerializable();
        close = in.readByte() != 0;
        up = (HashMap<String, Object>) in.readSerializable();
        kv = (HashMap<String, Object>) in.readSerializable();
        share = (HashMap<String, Object>) in.readSerializable();
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
                && up == null && kv == null && share == null && launchFeature == 0;
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
        dest.writeSerializable(up);
        dest.writeSerializable(kv);
        dest.writeSerializable(share);
        dest.writeParcelable(appAR, flags);
        dest.writeInt(launchFeature);
    }
}
