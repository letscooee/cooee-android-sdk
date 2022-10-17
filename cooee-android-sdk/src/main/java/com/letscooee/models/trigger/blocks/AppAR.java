package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;

public class AppAR implements Parcelable {

    public static final Creator<AppAR> CREATOR = new Creator<AppAR>() {
        @Override
        public AppAR createFromParcel(Parcel in) {
            return new AppAR(in);
        }

        @Override
        public AppAR[] newArray(int size) {
            return new AppAR[size];
        }
    };
    private final HashMap<String, Object> data = new HashMap<>();
    private final String name;
    private final HashMap<String, Object> splash = new HashMap<>();

    protected AppAR(Parcel in) {
        name = in.readString();
        in.readMap(data, Object.class.getClassLoader());
        in.readMap(splash, Object.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeMap(data);
        dest.writeMap(splash);
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public HashMap<String, Object> getSplash() {
        return splash;
    }
}