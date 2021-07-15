package com.letscooee.models.v3.inapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.letscooee.models.v3.block.Background;
import com.letscooee.models.v3.block.Border;
import com.letscooee.models.v3.block.ClickAction;
import com.letscooee.models.v3.block.Position;
import com.letscooee.models.v3.block.Size;
import com.letscooee.models.v3.block.Spacing;
import com.letscooee.models.v3.elemeent.Children;

import java.util.ArrayList;

public class Layers implements Parcelable {

    private Size size;
    private Background bg;
    private Border border;
    private Spacing spacing;
    private Position position;
    private ClickAction click;
    private ArrayList<Children> children;

    protected Layers(Parcel in) {
        size = in.readParcelable(Size.class.getClassLoader());
        bg = in.readParcelable(Background.class.getClassLoader());
        border = in.readParcelable(Border.class.getClassLoader());
        spacing = in.readParcelable(Spacing.class.getClassLoader());
        position = in.readParcelable(Position.class.getClassLoader());
        click = in.readParcelable(ClickAction.class.getClassLoader());
        children = in.createTypedArrayList(Children.CREATOR);
    }

    public static final Creator<Layers> CREATOR = new Creator<Layers>() {
        @Override
        public Layers createFromParcel(Parcel in) {
            return new Layers(in);
        }

        @Override
        public Layers[] newArray(int size) {
            return new Layers[size];
        }
    };

    public Size getSize() {
        return size;
    }

    public Background getBg() {
        return bg;
    }

    public Border getBorder() {
        return border;
    }

    public Spacing getSpacing() {
        return spacing;
    }

    public Position getPosition() {
        return position;
    }

    public ClickAction getClick() {
        return click;
    }

    public ArrayList<Children> getElements() {
        return children;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(size, flags);
        dest.writeParcelable(bg, flags);
        dest.writeParcelable(border, flags);
        dest.writeParcelable(spacing, flags);
        dest.writeParcelable(position, flags);
        dest.writeParcelable(click, flags);
        dest.writeTypedList(children);
    }
}
