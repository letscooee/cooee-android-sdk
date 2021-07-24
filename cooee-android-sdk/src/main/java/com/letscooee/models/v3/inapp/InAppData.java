package com.letscooee.models.v3.inapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class InAppData implements Parcelable {

    private Container container;
    private ArrayList<Layer> layers;

    protected InAppData(Parcel in) {
        container = in.readParcelable(Container.class.getClassLoader());
        layers = in.createTypedArrayList(Layer.CREATOR);
    }

    public static final Creator<InAppData> CREATOR = new Creator<InAppData>() {
        @Override
        public InAppData createFromParcel(Parcel in) {
            return new InAppData(in);
        }

        @Override
        public InAppData[] newArray(int size) {
            return new InAppData[size];
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
