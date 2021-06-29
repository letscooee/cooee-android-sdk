package com.letscooee.models.ar;

import android.os.Parcel;
import android.os.Parcelable;

public class ARData implements Parcelable {

    private String triggerName;
    private String customerName;
    private String greeting;
    private String offer;
    private String action;


    protected ARData(Parcel in) {
        triggerName = in.readString();
        customerName = in.readString();
        greeting = in.readString();
        offer = in.readString();
        action = in.readString();
    }

    public static final Creator<ARData> CREATOR = new Creator<ARData>() {
        @Override
        public ARData createFromParcel(Parcel in) {
            return new ARData(in);
        }

        @Override
        public ARData[] newArray(int size) {
            return new ARData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(triggerName);
        dest.writeString(customerName);
        dest.writeString(greeting);
        dest.writeString(offer);
        dest.writeString(action);
    }

    public String getTriggerName() {
        return triggerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getGreeting() {
        return greeting;
    }

    public String getOffer() {
        return offer;
    }

    public String getAction() {
        return action;
    }

    public static Creator<ARData> getCREATOR() {
        return CREATOR;
    }
}
