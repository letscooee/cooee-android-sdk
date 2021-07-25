package com.letscooee.models.trigger.elements;

import android.os.Parcel;
import android.os.Parcelable;
import com.letscooee.models.trigger.blocks.*;

public abstract class BaseElement implements Parcelable {

    protected Background bg;
    protected Border border;
    protected ClickAction click;
    protected Overflow overflow;
    protected Position position;
    protected Shadow shadow;
    protected Size size;
    protected Spacing spacing;
    protected Transform transform;

    protected BaseElement(Parcel in) {
        bg = in.readParcelable(Background.class.getClassLoader());
        border = in.readParcelable(Border.class.getClassLoader());
        click = in.readParcelable(ClickAction.class.getClassLoader());
        overflow = in.readParcelable(Overflow.class.getClassLoader());
        position = in.readParcelable(Position.class.getClassLoader());
        shadow = in.readParcelable(Shadow.class.getClassLoader());
        spacing = in.readParcelable(Spacing.class.getClassLoader());
        size = in.readParcelable(Size.class.getClassLoader());
        transform = in.readParcelable(Transform.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bg, flags);
        dest.writeParcelable(border, flags);
        dest.writeParcelable(click, flags);
        dest.writeParcelable(overflow, flags);
        dest.writeParcelable(position, flags);
        dest.writeParcelable(shadow, flags);
        dest.writeParcelable(spacing, flags);
        dest.writeParcelable(size, flags);
        dest.writeParcelable(transform, flags);
    }

    public ClickAction getAction() {
        return click;
    }

    public Position getPosition() {
        return position;
    }

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

    public Transform getTransform() {
        return transform;
    }
}
