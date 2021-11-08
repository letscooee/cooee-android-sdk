package com.letscooee.models.trigger.blocks;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.trigger.FlexProperty;

/**
 * @author Ashish Gaikwad 08/11/21
 * @since 1.1.0
 */
public class Flex implements Parcelable {

    @SerializedName("jc")
    @Expose
    private FlexProperty.JustifyContent justifyContent;

    @SerializedName("ai")
    @Expose
    private FlexProperty.AlignItems alignItems;

    @SerializedName("w")
    @Expose
    private FlexProperty.Wrap wrap;

    @SerializedName("ac")
    @Expose
    private FlexProperty.AlignContent alignContent;

    @SerializedName("d")
    @Expose
    private FlexProperty.Direction direction;

    protected Flex(Parcel in) {
        justifyContent = (FlexProperty.JustifyContent) in.readSerializable();
        alignItems = (FlexProperty.AlignItems) in.readSerializable();
        wrap = (FlexProperty.Wrap) in.readSerializable();
        alignContent = (FlexProperty.AlignContent) in.readSerializable();
        direction = (FlexProperty.Direction) in.readSerializable();
    }

    public static final Creator<Flex> CREATOR = new Creator<Flex>() {
        @Override
        public Flex createFromParcel(Parcel in) {
            return new Flex(in);
        }

        @Override
        public Flex[] newArray(int size) {
            return new Flex[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(justifyContent);
        dest.writeSerializable(alignItems);
        dest.writeSerializable(wrap);
        dest.writeSerializable(alignContent);
        dest.writeSerializable(direction);
    }

    public int getDirection() {
        return direction == null ? FlexProperty.Direction.ROW.getValue() : direction.getValue();
    }

    public int getWrap() {
        return wrap == null ? FlexProperty.Wrap.NOWRAP.getValue() : wrap.getValue();
    }

    public int getAlignContent() {
        return alignContent == null ?
                FlexProperty.AlignContent.STRETCH.getValue() : alignContent.getValue();
    }

    public int getJustifyContent() {
        return justifyContent == null ?
                FlexProperty.JustifyContent.FLEX_START.getValue() : justifyContent.getValue();
    }

    public int getAlignItems() {
        return alignItems == null ? FlexProperty.AlignItems.STRETCH.getValue() : alignItems.getValue();
    }

}
