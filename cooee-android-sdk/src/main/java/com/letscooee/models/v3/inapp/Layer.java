package com.letscooee.models.v3.inapp;

import android.os.Parcel;
import com.letscooee.models.v3.element.GroupElement;

public class Layer extends GroupElement {

    protected Layer(Parcel in) {
        super(in);
    }

    public static final Creator<Layer> CREATOR = new Creator<Layer>() {
        @Override
        public Layer createFromParcel(Parcel in) {
            return new Layer(in);
        }

        @Override
        public Layer[] newArray(int size) {
            return new Layer[size];
        }
    };
}
