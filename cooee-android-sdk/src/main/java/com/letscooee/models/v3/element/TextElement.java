package com.letscooee.models.v3.element;

import android.os.Parcel;

public class TextElement extends BaseTextElement {

    protected TextElement(Parcel in) {
        super(in);
    }

    public static final Creator<TextElement> CREATOR = new Creator<TextElement>() {
        @Override
        public TextElement createFromParcel(Parcel in) {
            return new TextElement(in);
        }

        @Override
        public TextElement[] newArray(int size) {
            return new TextElement[size];
        }
    };
}
