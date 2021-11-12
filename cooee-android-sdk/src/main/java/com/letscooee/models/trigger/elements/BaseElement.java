package com.letscooee.models.trigger.elements;

import static com.letscooee.utils.ui.UnitUtils.getScaledPixel;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.trigger.PositionType;
import com.letscooee.models.trigger.blocks.*;
import com.letscooee.utils.ui.UnitUtils;

public abstract class BaseElement implements Parcelable {

    protected Background bg;

    @SerializedName("br")
    @Expose
    protected Border border;

    @SerializedName("clc")
    @Expose
    protected ClickAction click;

    @SerializedName("shd")
    @Expose
    protected Shadow shadow;

    @SerializedName("spc")
    @Expose
    protected Spacing spacing;

    @SerializedName("trf")
    @Expose
    protected Transform transform;

    private final Integer flexGrow;
    private final Integer flexShrink;
    private final Integer flexOrder;
    private final PositionType mode;

    @SerializedName("w")
    @Expose
    private final String width;

    @SerializedName("h")
    @Expose
    private final String height;
    private final float x;
    private final float y;
    private final Integer z;

    protected BaseElement(Parcel in) {
        bg = in.readParcelable(Background.class.getClassLoader());
        border = in.readParcelable(Border.class.getClassLoader());
        click = in.readParcelable(ClickAction.class.getClassLoader());
        shadow = in.readParcelable(Shadow.class.getClassLoader());
        spacing = in.readParcelable(Spacing.class.getClassLoader());
        transform = in.readParcelable(Transform.class.getClassLoader());

        flexGrow = (Integer) in.readSerializable();
        flexShrink = (Integer) in.readSerializable();
        flexOrder = (Integer) in.readSerializable();

        mode = (PositionType) in.readSerializable();
        width = in.readString();
        height = in.readString();
        x = in.readFloat();
        y = in.readFloat();
        z = (Integer) in.readSerializable();
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
        dest.writeParcelable(shadow, flags);
        dest.writeParcelable(spacing, flags);
        dest.writeParcelable(transform, flags);

        dest.writeSerializable(flexGrow);
        dest.writeSerializable(flexShrink);
        dest.writeSerializable(flexOrder);

        dest.writeSerializable(mode);
        dest.writeString(width);
        dest.writeString(height);
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeSerializable(z);
    }

    public ClickAction getClickAction() {
        return click;
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

    public Float getCalculatedHeight() {
        return TextUtils.isEmpty(height) ? null : getScaledPixel(Float.parseFloat(height));
    }

    public Float getCalculatedWidth() {
        return TextUtils.isEmpty(width) ? null : getScaledPixel(Float.parseFloat(width));
    }

    public float getY() {
        return UnitUtils.getScaledPixel(y);
    }

    public float getX() {
        return UnitUtils.getScaledPixel(x);
    }

    public Integer getZ() {
        return z;
    }

    public boolean isAbsolute() {
        return mode != null && this.mode == PositionType.FREE_FLOATING;
    }

}
