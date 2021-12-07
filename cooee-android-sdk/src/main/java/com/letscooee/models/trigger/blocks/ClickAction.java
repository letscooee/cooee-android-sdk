package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class ClickAction implements Parcelable {

    private BrowserContent iab;
    private BrowserContent external;
    private BrowserContent updateApp;
    private String[] prompts;
    private HashMap<String, Object> up;
    private HashMap<String, Object> kv;
    private HashMap<String, Object> share;
    private boolean close;
    private AppAR appAR;

    protected ClickAction(Parcel in) {
        iab = in.readParcelable(BrowserContent.class.getClassLoader());
        external = in.readParcelable(BrowserContent.class.getClassLoader());
        updateApp = in.readParcelable(BrowserContent.class.getClassLoader());
        prompts = in.createStringArray();
        close = in.readByte() != 0;
        up = (HashMap<String, Object>) in.readSerializable();
        kv = (HashMap<String, Object>) in.readSerializable();
        share = (HashMap<String, Object>) in.readSerializable();
        appAR = in.readParcelable(AppAR.class.getClassLoader());
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

    public String[] getPrompts() {
        return prompts;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(iab, flags);
        dest.writeParcelable(external, flags);
        dest.writeParcelable(updateApp, flags);
        dest.writeStringArray(prompts);
        dest.writeByte((byte) (close ? 1 : 0));
        dest.writeSerializable(up);
        dest.writeSerializable(kv);
        dest.writeSerializable(share);
        dest.writeParcelable(appAR, flags);
    }
}