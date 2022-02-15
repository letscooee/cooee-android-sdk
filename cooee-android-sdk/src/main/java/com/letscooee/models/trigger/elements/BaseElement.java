package com.letscooee.models.trigger.elements;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.models.trigger.blocks.*;

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

    @SerializedName("w")
    @Expose
    protected final double width;

    @SerializedName("h")
    @Expose
    protected final double height;
    private final float x;
    private final float y;
    private final Integer z;

    /**
     * Main purpose of this constructor is to initialize BaseElement for InAppBodyRenderer with
     * InAppTrigger's custom background and clickAction
     *
     * @param background  Nullable instance of {@link Background}
     * @param clickAction NonNull instance of {@link ClickAction}
     */
    protected BaseElement(@Nullable Background background, @NonNull ClickAction clickAction) {
        this.bg = background;
        this.click = clickAction;
        this.width = 0;
        this.height = 0;
        this.x = 0;
        this.y = 0;
        this.z = null;
    }

    protected BaseElement(Parcel in) {
        bg = in.readParcelable(Background.class.getClassLoader());
        border = in.readParcelable(Border.class.getClassLoader());
        click = in.readParcelable(ClickAction.class.getClassLoader());
        shadow = in.readParcelable(Shadow.class.getClassLoader());
        spacing = in.readParcelable(Spacing.class.getClassLoader());
        transform = in.readParcelable(Transform.class.getClassLoader());

        width = in.readDouble();
        height = in.readDouble();
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

        dest.writeDouble(width);
        dest.writeDouble(height);
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

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    public Integer getZ() {
        return z;
    }

    public void setClick(ClickAction click) {
        this.click = click;
    }

    public void setBg(Background bg) {
        this.bg = bg;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
