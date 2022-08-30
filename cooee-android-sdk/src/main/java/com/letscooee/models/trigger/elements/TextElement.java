package com.letscooee.models.trigger.elements;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.exceptions.InvalidTriggerDataException;

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

    /**
     * Checks if the text element has valid text.
     *
     * @return true if the text element has valid text.
     * @throws InvalidTriggerDataException if the text element has no/empty text.
     */
    @Override
    public boolean hasValidResource() throws InvalidTriggerDataException {
        if (parts == null || parts.isEmpty()) {
            throw new InvalidTriggerDataException("TextElement has no/empty parts");
        }

        return true;
    }
}
