package com.letscooee.models.v3.inapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.letscooee.models.v3.block.Animation;
import com.letscooee.models.v3.block.Background;
import com.letscooee.models.v3.block.Border;
import com.letscooee.models.v3.block.ClickAction;
import com.letscooee.models.v3.block.Position;
import com.letscooee.models.v3.block.Size;
import com.letscooee.models.v3.block.Spacing;

public class Container  implements Parcelable {

    private Size size;
    private Background bg;
    private Border border;
    private Spacing spacing;
    private Position position;
    private Animation animation;
    private ClickAction action;

    protected Container(Parcel in) {
        size = in.readParcelable(Size.class.getClassLoader());
        bg = in.readParcelable(Background.class.getClassLoader());
        border = in.readParcelable(Border.class.getClassLoader());
        spacing = in.readParcelable(Spacing.class.getClassLoader());
        position = in.readParcelable(Position.class.getClassLoader());
        animation = in.readParcelable(Animation.class.getClassLoader());
        action = in.readParcelable(ClickAction.class.getClassLoader());
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

    public Position getPosition() {
        return position;
    }

    public Animation getAnimation() {
        return animation;
    }

    public ClickAction getAction() {
        return action;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(size, flags);
        dest.writeParcelable(bg, flags);
        dest.writeParcelable(border, flags);
        dest.writeParcelable(spacing, flags);
        dest.writeParcelable(position, flags);
        dest.writeParcelable(animation, flags);
        dest.writeParcelable(action, flags);
    }


}
