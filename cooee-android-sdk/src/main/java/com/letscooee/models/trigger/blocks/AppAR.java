package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;

public class AppAR implements Parcelable {

    private final String name;
    private final HashMap<String, Object> data;

    protected AppAR(Parcel in) {
        name = in.readString();
        data = (HashMap<String, Object>) in.readSerializable();
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeSerializable(data);
    }

    public String getName() {
        return name;
    }

    public HashMap<String, Object> getData() {
        return data;
    }
}