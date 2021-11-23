package com.letscooee.models.trigger.inapp;

import android.os.Parcel;
import com.letscooee.models.trigger.blocks.Animation;
import com.letscooee.models.trigger.elements.BaseElement;

public class Container extends BaseElement {

    private final Animation animation;

    protected Container(Parcel in) {
        super(in);
        animation = in.readParcelable(getClass().getClassLoader());
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(animation, flags);
    }
}
