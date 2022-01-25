package com.letscooee.models.trigger.inapp;

import android.os.Parcel;
import android.widget.RelativeLayout;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.trigger.Gravity;
import com.letscooee.models.trigger.blocks.Animation;
import com.letscooee.models.trigger.elements.BaseElement;

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

    // Returns actual view gravity
    public int getGravity(RelativeLayout.LayoutParams layoutParams) {
        Gravity g = Gravity.fromByte(gravity);

        switch (g) {
            case TOP_LEFT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                return android.view.Gravity.START;
            }
            case TOP_CENTER: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                return android.view.Gravity.CENTER_HORIZONTAL;
            }
            case TOP_RIGHT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                return android.view.Gravity.END;
            }
            case CENTER_LEFT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                return android.view.Gravity.START | android.view.Gravity.CENTER;
            }
            case CENTER_RIGHT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                return android.view.Gravity.END | android.view.Gravity.CENTER;
            }
            case BOTTOM_LEFT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                return android.view.Gravity.START | android.view.Gravity.BOTTOM;
            }
            case BOTTOM_CENTER: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                return android.view.Gravity.CENTER | android.view.Gravity.BOTTOM;
            }
            case BOTTOM_RIGHT: {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                return android.view.Gravity.END | android.view.Gravity.BOTTOM;
            }
            default: {
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                return android.view.Gravity.CENTER;
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(animation, flags);
        dest.writeByte(gravity);
    }
}
