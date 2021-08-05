package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

public class ARData implements Parcelable {

    private String triggerName;
    private String customerName;
    private String greeting;
    private String offer;
    private ClickAction action;

    protected ARData(Parcel in) {
        triggerName = in.readString();
        customerName = in.readString();
        greeting = in.readString();
        offer = in.readString();
        action = in.readParcelable(ClickAction.class.getClassLoader());
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

    public ClickAction getAction() {
        return action;
    }

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
        dest.writeParcelable(action, flags);
    }
}