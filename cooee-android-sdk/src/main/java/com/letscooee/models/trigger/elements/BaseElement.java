package com.letscooee.models.trigger.elements;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.blocks.Border;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.models.trigger.blocks.Shadow;
import com.letscooee.models.trigger.blocks.Spacing;
import com.letscooee.models.trigger.blocks.Transform;

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
    protected final float width;

    @SerializedName("h")
    @Expose
    protected final float height;
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

        width = in.readFloat();
        height = in.readFloat();
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

        dest.writeFloat(width);
        dest.writeFloat(height);
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

    public void setBg(Background bg) {
        this.bg = bg;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public String getBgImage() {
        if (this.bg == null || this.bg.getImage() == null || TextUtils.isEmpty(this.bg.getImage().getSrc())) {
            return null;
        }

        return this.bg.getImage().getSrc();
    }

    public String getImageURL() {
        return this.getBgImage();
    }

    /**
     * Checks if Element has valid background image resource.
     *
     * @return true if background image resource is valid, otherwise throws error with details.
     * @throws InvalidTriggerDataException if background image resource is invalid.
     */
    public boolean hasValidImageResource() throws InvalidTriggerDataException {
        if (bg == null || bg.getImage() == null || !TextUtils.isEmpty(getBgImage())) {
            return true;
        }

        throw new InvalidTriggerDataException("Found Empty background image url");
    }

}
