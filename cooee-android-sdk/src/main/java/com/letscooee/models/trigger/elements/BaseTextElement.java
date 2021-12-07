package com.letscooee.models.trigger.elements;

import android.os.Parcel;
import com.letscooee.models.trigger.blocks.*;

public abstract class BaseTextElement extends BaseElement {

    protected String text;

    protected Alignment alignment;
    protected Font font;
    protected Colour colour;

    protected BaseTextElement(Parcel in) {
        super(in);
        text = in.readString();
        alignment = in.readParcelable(Alignment.class.getClassLoader());
        font = in.readParcelable(Font.class.getClassLoader());
        colour = in.readParcelable(Colour.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeParcelable(alignment, flags);
        dest.writeParcelable(font, flags);
        dest.writeParcelable(colour, flags);
    }

    public String getText() {
        return text;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public Font getFont() {
        return font;
    }

    public Colour getColor() {
        return colour;
    }
}