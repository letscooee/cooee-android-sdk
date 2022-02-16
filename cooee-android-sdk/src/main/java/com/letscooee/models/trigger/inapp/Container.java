package com.letscooee.models.trigger.inapp;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.models.trigger.blocks.Animation;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.utils.Constants;

public class Container extends BaseElement {

    private final Animation animation;

    // TODO: 16/02/22 This would be removed in future when client-portal sends gravity in InAppTrigger class instead of Container class.
    // o -> origin
    @SerializedName("o")
    @Expose
    private final byte gravity;

    protected Container(Parcel in) {
        super(in);
        animation = in.readParcelable(getClass().getClassLoader());
        gravity = in.readByte();
    }

    public static final Creator<Container> CREATOR = new Creator<Container>() {
        @Override
        public Container createFromParcel(Parcel in) {
            return new Container(in);
        }

        @Override
        public Container[] newArray(int size) {
            return new Container[size];
        }
    };

    public Animation getAnimation() {
        return animation;
    }

    /**
     * Provides containers gravity
     *
     * @return Return Nullable {@link android.view.Gravity} as {@link Integer} value
     */
    public byte getGravity() {
        return gravity;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(animation, flags);
        dest.writeByte(gravity);
    }

    @Override
    public double getWidth() {
        return width <= 0 ? Constants.DEFAULT_CONTAINER_WIDTH : width;
    }

    @Override
    public double getHeight() {
        return height <= 0 ? Constants.DEFAULT_CONTAINER_HEIGHT : height;
    }
}
