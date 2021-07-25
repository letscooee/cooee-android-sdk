package com.letscooee.models.trigger.blocks;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;

public class Gradient implements Parcelable {

    // TODO: 07/07/21 Discus for Gradient type, angle
    protected Gradient(Parcel in) {
        start = in.readString();
        end = in.readString();
        direction = in.readInt();
        type = Type.valueOf(in.readString());
    }

    public static final Creator<Gradient> CREATOR = new Creator<Gradient>() {
        @Override
        public Gradient createFromParcel(Parcel in) {
            return new Gradient(in);
        }

        @Override
        public Gradient[] newArray(int size) {
            return new Gradient[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(start);
        dest.writeString(end);
        dest.writeInt(direction);
        dest.writeString(type.name());
    }

    public enum Type {LINEAR, RADIAL, SWEEP}

    private Type type;
    private String start;
    private String end;
    private int direction;

    public Type getType() {
        return type;
    }

    public int getDirection() {
        return direction;
    }

    public int getStartColor() {
        return Color.parseColor(start);
    }

    public int getEndColor() {
        return Color.parseColor(end);
    }

    public GradientDrawable.Orientation getGradiantAngle() {
        switch (direction) {
            case 45:
                return GradientDrawable.Orientation.TR_BL;
            case 90:
                return GradientDrawable.Orientation.TOP_BOTTOM;
            case 135:
                return GradientDrawable.Orientation.TL_BR;
            case 180:
                return GradientDrawable.Orientation.LEFT_RIGHT;
            case 225:
                return GradientDrawable.Orientation.BL_TR;
            case 270:
                return GradientDrawable.Orientation.BOTTOM_TOP;
            case 315:
                return GradientDrawable.Orientation.BR_TL;
            default:
                return GradientDrawable.Orientation.RIGHT_LEFT;

        }
    }

    public int getGradiantType() {
        switch (type) {
            case RADIAL:
                return GradientDrawable.RADIAL_GRADIENT;
            case SWEEP:
                return GradientDrawable.SWEEP_GRADIENT;
            default:
                return GradientDrawable.LINEAR_GRADIENT;
        }
    }

    public GradientDrawable getGradient() {
        GradientDrawable gradientDrawable = new GradientDrawable(getGradiantAngle(),
                new int[]{getStartColor(), getEndColor()});
        gradientDrawable.setGradientType(getGradiantType());
        return gradientDrawable;
    }
}
