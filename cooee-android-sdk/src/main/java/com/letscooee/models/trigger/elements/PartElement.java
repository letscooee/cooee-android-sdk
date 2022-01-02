package com.letscooee.models.trigger.elements;

import android.graphics.Color;
import android.os.Parcel;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PartElement extends BaseElement {

    @SerializedName("txt")
    @Expose
    protected String text;

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

    protected PartElement(Parcel in) {
        super(in);
        text = in.readString();
        textColour = in.readString();
        bold = in.readByte() == 0;
        italic = in.readByte() == 0;
        underline = in.readByte() == 0;
        strikeTrough = in.readByte() == 0;
    }

    public static final Creator<PartElement> CREATOR = new Creator<PartElement>() {
        @Override
        public PartElement createFromParcel(Parcel in) {
            return new PartElement(in);
        }

        @Override
        public PartElement[] newArray(int size) {
            return new PartElement[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeString(textColour);
        dest.writeByte((byte) (bold ? 0 : 1));
        dest.writeByte((byte) (italic ? 0 : 1));
        dest.writeByte((byte) (underline ? 0 : 1));
        dest.writeByte((byte) (strikeTrough ? 0 : 1));

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

    public String getText() {
        return text;
    }
}
