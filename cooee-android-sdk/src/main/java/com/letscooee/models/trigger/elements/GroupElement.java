package com.letscooee.models.trigger.elements;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.models.trigger.blocks.Flex;
import com.letscooee.models.trigger.blocks.Overflow;

import java.util.ArrayList;

public class GroupElement extends BaseElement {

    private final ArrayList<BaseElement> children;
    protected Overflow.Type overflow;

    @SerializedName("flex")
    @Expose
    private final Flex flexProperties;

    protected GroupElement(Parcel in) {
        super(in);
        children = in.readArrayList(getClass().getClassLoader());
        overflow = (Overflow.Type) in.readSerializable();
        flexProperties = in.readParcelable(Flex.class.getClassLoader());
    }

    public static final Creator<GroupElement> CREATOR = new Creator<GroupElement>() {
        @Override
        public GroupElement createFromParcel(Parcel in) {
            return new GroupElement(in);
        }

        @Override
        public GroupElement[] newArray(int size) {
            return new GroupElement[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(children);
        dest.writeSerializable(overflow);
        dest.writeParcelable(flexProperties, flags);
    }

    public ArrayList<BaseElement> getChildren() {
        return children;
    }

    public boolean isOverflowHidden() {
        return overflow == null || overflow == Overflow.Type.HIDDEN;
    }

    public Flex getFlexProperties() {
        return flexProperties;
    }
}
