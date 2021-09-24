package com.letscooee.models.trigger.inapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class InAppTrigger implements Parcelable {

    private Container container;
    private ArrayList<Layer> layers;

    protected InAppTrigger(Parcel in) {
        container = in.readParcelable(Container.class.getClassLoader());
        layers = in.createTypedArrayList(Layer.CREATOR);
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

    public ArrayList<Layer> getLayers() {
        return layers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(container, flags);
        dest.writeTypedList(layers);
    }
}
