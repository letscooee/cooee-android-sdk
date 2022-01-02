package com.letscooee.models.trigger.elements;

import android.os.Parcel;


public class ShapeElement extends BaseElement{
    protected ShapeElement(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator<ShapeElement> CREATOR = new Creator<ShapeElement>() {
        @Override
        public ShapeElement createFromParcel(Parcel in) {
            return new ShapeElement(in);
        }

        @Override
        public ShapeElement[] newArray(int size) {
            return new ShapeElement[size];
        }
    };
}
