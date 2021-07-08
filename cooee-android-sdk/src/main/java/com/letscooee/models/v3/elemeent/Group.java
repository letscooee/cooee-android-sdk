package com.letscooee.models.v3.elemeent;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Group implements Parcelable {

    public enum Direction {COLUMN, ROW, COLUMN_REVERSE, ROW_REVERSE}

    private Direction direction;
    private String wrap;
    private String justifyContent;
    private String alignItems;
    private String alignContent;
    private ArrayList<Element> elements;

    protected Group(Parcel in) {
        wrap = in.readString();
        justifyContent = in.readString();
        alignItems = in.readString();
        alignContent = in.readString();
        direction = Direction.valueOf(in.readString());
        elements = in.readArrayList(null);
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(wrap);
        dest.writeString(justifyContent);
        dest.writeString(alignItems);
        dest.writeString(alignContent);
        dest.writeString(direction.name());
        dest.writeList(elements);
    }

    public Direction getDirection() {
        return direction;
    }

    public String getWrap() {
        return wrap;
    }

    public String getJustifyContent() {
        return justifyContent;
    }

    public String getAlignItems() {
        return alignItems;
    }

    public String getAlignContent() {
        return alignContent;
    }

    public ArrayList<Element> getElements() {
        return elements;
    }
}
