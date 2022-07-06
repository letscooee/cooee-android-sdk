package com.letscooee.models.trigger.elements;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class TextElement extends BaseTextElement {

    @SerializedName("prs")
    @Expose
    private ArrayList<PartElement> parts;

    protected TextElement(Parcel in) {
        super(in);
        parts = in.readArrayList(getClass().getClassLoader());
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

    public ArrayList<PartElement> getParts() {
        return parts;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(parts);
    }

    @Override
    public boolean hasValidData() {
        return super.hasValidData() && parts != null && !parts.isEmpty();
    }
}
