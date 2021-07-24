package com.letscooee.models.v3.inapp;

import android.os.Parcel;
import com.letscooee.models.v3.block.Animation;
import com.letscooee.models.v3.element.BaseElement;

public class Container extends BaseElement {

    private Animation animation;

    protected Container(Parcel in) {
        super(in);
        animation = in.readParcelable(Animation.class.getClassLoader());
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
        dest.writeParcelable(animation, flags);
    }
}
