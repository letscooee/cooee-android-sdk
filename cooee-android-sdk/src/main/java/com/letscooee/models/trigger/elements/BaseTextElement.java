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
    protected Font font;

    @SerializedName("clr")
    @Expose
    protected Colour color;

    @SerializedName("b")
    @Expose
    protected boolean bold;

    @SerializedName("i")
    @Expose
    protected boolean italic;

    @SerializedName("u")
    @Expose
    protected boolean underline;

    @SerializedName("c")
    @Expose
    protected String textColour;

    @SerializedName("st")
    @Expose
    protected boolean strikeTrough;

    protected BaseTextElement(Parcel in) {
        super(in);
        text = in.readString();
        alignment = in.readInt();
        font = in.readParcelable(Font.class.getClassLoader());
        color = in.readParcelable(Colour.class.getClassLoader());
        bold = in.readInt() == 0;
        italic = in.readInt() == 0;
        underline = in.readInt() == 0;
        textColour = in.readString();
        strikeTrough = in.readInt() == 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeInt(alignment);
        dest.writeParcelable(font, flags);
        dest.writeParcelable(color, flags);
        dest.writeInt(bold ? 0 : 1);
        dest.writeInt(italic ? 0 : 1);
        dest.writeInt(underline ? 0 : 1);
        dest.writeString(textColour);
        dest.writeInt(strikeTrough ? 0 : 1);
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

    public boolean isBold() {
        return bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isUnderline() {
        return underline;
    }

    public Integer getPartTextColour() {
        return TextUtils.isEmpty(textColour) ? null : Color.parseColor(textColour);
    }

    public boolean isStrikeTrough() {
        return strikeTrough;
    }
}
