package com.letscooee.models.trigger.elements;

import android.os.Build;
import android.os.Parcel;
import android.view.Gravity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.models.trigger.blocks.*;
import com.letscooee.utils.Constants;

public abstract class BaseTextElement extends BaseElement {

    @SerializedName("alg")
    @Expose
    protected int alignment;

    @SerializedName("c")
    @Expose
    protected Colour color;

    @SerializedName("f")
    @Expose
    protected Font font;
    protected String text;

    @SerializedName("txtShd")
    @Expose
    protected TextShadow textShadow;

    protected BaseTextElement(Parcel in) {
        super(in);
        text = in.readString();
        alignment = in.readInt();
        font = in.readParcelable(Font.class.getClassLoader());
        color = in.readParcelable(Colour.class.getClassLoader());
        textShadow = in.readParcelable(TextShadow.class.getClassLoader());
    }

    public int getAlignment() {
        switch (alignment) {
            case 1:
                return Gravity.CENTER;
            case 2:
                return Gravity.END;
            case 3:
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? Constants.JUSTIFY_TEXT_ALIGNMENT : Gravity.START;
            default:
                return Gravity.START;
        }
    }

    public Colour getColor() {
        return color;
    }

    public Font getFont() {
        return font;
    }

    public String getText() {
        return text;
    }

    public TextShadow getTextShadow() {
        return textShadow;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeInt(alignment);
        dest.writeParcelable(font, flags);
        dest.writeParcelable(color, flags);
        dest.writeParcelable(textShadow, flags);
    }
}
