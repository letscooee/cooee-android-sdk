package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Abhishek Taparia
 */
public class TriggerButtonAction implements Parcelable {
    private HashMap<String, Object> userProperty;
    private HashMap<String, Object> kv;

    protected TriggerButtonAction(Parcel in) {
        this.userProperty = (HashMap<String, Object>) in.readSerializable();
        this.kv = (HashMap<String, Object>) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.userProperty);
        dest.writeSerializable(this.kv);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TriggerButtonAction> CREATOR = new Creator<TriggerButtonAction>() {
        @Override
        public TriggerButtonAction createFromParcel(Parcel in) {
            return new TriggerButtonAction(in);
        }

        @Override
        public TriggerButtonAction[] newArray(int size) {
            return new TriggerButtonAction[size];
        }
    };

    public HashMap<String, Object> getUserProperty() {
        return userProperty;
    }

    public HashMap<String, Object> getKv() {
        return kv;
    }

    @Override
    public String toString() {
        return "TriggerButtonAction{" +
                "userProperty=" + userProperty +
                ", kv=" + kv +
                '}';
    }
}
