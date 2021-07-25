package com.letscooee.models.trigger.elements;

import android.os.Parcel;

import java.util.ArrayList;

public class GroupElement extends BaseChildElement {

    private final ArrayList<BaseChildElement> children;

    protected GroupElement(Parcel in) {
        super(in);
        children = in.readArrayList(getClass().getClassLoader());
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

    public ArrayList<BaseChildElement> getChildren() {
        return children;
    }
}
