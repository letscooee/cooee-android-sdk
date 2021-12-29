package com.letscooee.models.trigger.elements;

import android.graphics.Color;
import android.os.Parcel;
import android.text.TextUtils;
import android.view.Gravity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.models.trigger.blocks.*;

public abstract class BaseTextElement extends BaseElement {

    protected String text;

    @SerializedName("alg")
    @Expose
    protected int alignment;

    @SerializedName("f")
    @Expose
    protected Font font;

    @SerializedName("c")
    @Expose
    protected Colour color;

    protected BaseTextElement(Parcel in) {
        super(in);
        text = in.readString();
        alignment = in.readInt();
        font = in.readParcelable(Font.class.getClassLoader());
        color = in.readParcelable(Colour.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeInt(alignment);
        dest.writeParcelable(font, flags);
        dest.writeParcelable(color, flags);
    }

    public String getText() {
        return text;
    }

    public int getAlignment() {
        switch (alignment) {
            case 0:
                return Gravity.START;
            case 1:
                return Gravity.CENTER;
            case 3:
                return Gravity.END;
        }
        return Gravity.START;
    }

    public Font getFont() {
        return font;
    }

    public Colour getColor() {
        return color;
    }
}
