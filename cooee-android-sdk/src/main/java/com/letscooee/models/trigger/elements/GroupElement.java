package com.letscooee.models.trigger.elements;

import android.os.Parcel;

import com.letscooee.models.trigger.blocks.Overflow;

import java.util.ArrayList;

public class GroupElement extends BaseElement {

    private final ArrayList<BaseElement> children;
    protected Overflow.Type overflow;

    protected GroupElement(Parcel in) {
        super(in);
        children = in.readArrayList(getClass().getClassLoader());
        overflow = (Overflow.Type) in.readSerializable();
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
    }

    public ArrayList<BaseElement> getChildren() {
        return children;
    }

    public Overflow.Type getOverflow() {
        return overflow;
    }
}
