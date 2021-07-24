package com.letscooee.models.v3.element;

import android.os.Parcel;

import java.util.ArrayList;

public class GroupElement extends BaseChildElement {

    private final ArrayList<BaseElement> children;

    protected GroupElement(Parcel in) {
        super(in);
        children = in.readArrayList(null);
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
    }

    public ArrayList<BaseElement> getChildren() {
        return children;
    }
}
