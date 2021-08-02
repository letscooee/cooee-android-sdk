package com.letscooee.models.trigger.elements;

import android.os.Parcel;

import java.util.ArrayList;

public class TextElement extends BaseTextElement {

    private ArrayList<TextElement> parts;

    protected TextElement(Parcel in) {
        super(in);
        parts = new ArrayList<>();
        parts = in.readArrayList(TextElement.class.getClassLoader());
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

    public ArrayList<TextElement> getParts() {
        return parts;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(parts);
    }
}
