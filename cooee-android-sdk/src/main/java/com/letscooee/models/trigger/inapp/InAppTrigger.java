package com.letscooee.models.trigger.inapp;

import android.content.pm.ActivityInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.models.trigger.elements.BaseElement;

import java.util.ArrayList;

public class InAppTrigger implements Parcelable {

    @SerializedName("cont")
    @Expose
    private Container container;

    @SerializedName("elems")
    @Expose
    private ArrayList<BaseElement> elements;

    @SerializedName("o")
    @Expose
    private int orientation;

    protected InAppTrigger(Parcel in) {
        container = in.readParcelable(Container.class.getClassLoader());
        elements = in.readArrayList(getClass().getClassLoader());
        orientation = in.readInt();
    }

    public static final Creator<InAppTrigger> CREATOR = new Creator<InAppTrigger>() {
        @Override
        public InAppTrigger createFromParcel(Parcel in) {
            return new InAppTrigger(in);
        }

        @Override
        public InAppTrigger[] newArray(int size) {
            return new InAppTrigger[size];
        }
    };

    public Container getContainer() {
        return container;
    }

    public ArrayList<BaseElement> getElements() {
        return elements;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(container, flags);
        dest.writeList(elements);
        dest.writeInt(orientation);
    }

    public int getOrientation() {

        if (orientation == 2) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }
}
