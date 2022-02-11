package com.letscooee.models.trigger.inapp;

import android.os.Parcel;
import android.widget.RelativeLayout;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.trigger.Gravity;
import com.letscooee.models.trigger.blocks.Animation;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.inapp.renderer.utils.GravityUtil;

public class Container extends BaseElement {

    private final Animation animation;

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
     * Process In-App gravity on the screen
     *
     * @param layoutParams {@link RelativeLayout.LayoutParams} of the view
     * @return Return Nullable {@link android.view.Gravity} as {@link Integer} value
     */
    public int getGravity(RelativeLayout.LayoutParams layoutParams) {
        Gravity inAppGravity = Gravity.fromByte(gravity);

        return GravityUtil.processGravity(inAppGravity, layoutParams);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(animation, flags);
        dest.writeByte(gravity);
    }
}
