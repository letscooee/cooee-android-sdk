package com.letscooee.models.trigger.elements;

import android.os.Parcel;

public class ButtonElement extends TextElement {

    protected ButtonElement(Parcel in) {
        super(in);
    }

    public static final Creator<ButtonElement> CREATOR = new Creator<ButtonElement>() {
        @Override
        public ButtonElement createFromParcel(Parcel in) {
            return new ButtonElement(in);
        }

        @Override
        public ButtonElement[] newArray(int size) {
            return new ButtonElement[size];
        }
    };
}
