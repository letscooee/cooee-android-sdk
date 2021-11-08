package com.letscooee.models.trigger.elements;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.models.trigger.blocks.*;

public abstract class BaseElement implements Parcelable {

    protected Background bg;
    protected Border border;
    protected ClickAction click;

    @SerializedName("pos")
    @Expose
    protected Position position;
    protected Shadow shadow;
    protected Size size;
    protected Spacing spacing;
    protected Transform transform;

    private final Integer flexGrow;
    private final Integer flexShrink;
    private final Integer flexOrder;

    protected BaseElement(Parcel in) {
        bg = in.readParcelable(Background.class.getClassLoader());
        border = in.readParcelable(Border.class.getClassLoader());
        click = in.readParcelable(ClickAction.class.getClassLoader());
        position = in.readParcelable(Position.class.getClassLoader());
        shadow = in.readParcelable(Shadow.class.getClassLoader());
        spacing = in.readParcelable(Spacing.class.getClassLoader());
        size = in.readParcelable(Size.class.getClassLoader());
        transform = in.readParcelable(Transform.class.getClassLoader());

        flexGrow = (Integer) in.readSerializable();
        flexShrink = (Integer) in.readSerializable();
        flexOrder = (Integer) in.readSerializable();
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
        dest.writeParcelable(position, flags);
        dest.writeParcelable(shadow, flags);
        dest.writeParcelable(spacing, flags);
        dest.writeParcelable(size, flags);
        dest.writeParcelable(transform, flags);

        dest.writeSerializable(flexGrow);
        dest.writeSerializable(flexShrink);
        dest.writeSerializable(flexOrder);
    }

    public ClickAction getClickAction() {
        return click;
    }

    public Position getPosition() {
        if (position == null) this.position = new Position();
        return position;
    }

    public Size getSize() {
        if (size == null) this.size = new Size();
        return size;
    }

    public Background getBg() {
        return bg;
    }

    public Border getBorder() {
        return border;
    }

    public Shadow getShadow() {
        return shadow;
    }

    public Spacing getSpacing() {
        return spacing;
    }

    public Transform getTransform() {
        return transform;
    }

    public Integer getFlexGrow() {
        return flexGrow;
    }

    public Integer getFlexShrink() {
        return flexShrink;
    }

    public Integer getFlexOrder() {
        return flexOrder;
    }
}
