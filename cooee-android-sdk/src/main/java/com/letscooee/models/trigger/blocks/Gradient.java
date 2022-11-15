package com.letscooee.models.trigger.blocks;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class Gradient implements Parcelable {

    private static final int[] AVAILABLE_ANGLES = {0, 45, 90, 135, 180, 225, 270, 315, 360};

    public enum Type {
        LINEAR(0), RADIAL(1), SWEEP(2);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private final Type type;

    @SerializedName("c1")
    @Expose
    private final String start;

    @SerializedName("c2")
    @Expose
    private final String center;

    @SerializedName("c3")
    @Expose
    private final String end;

    @SerializedName("ang")
    @Expose
    private final int angle;

    protected Gradient(Parcel in) {
        start = in.readString();
        end = in.readString();
        angle = in.readInt();
        type = (Type) in.readSerializable();
        center = in.readString();
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
        dest.writeInt(angle);
        dest.writeSerializable(type);
        dest.writeString(center);
    }

    public Type getType() {
        return type == null ? Type.LINEAR : type;
    }

    public int getStartColor() {
        return Color.parseColor(start);
    }

    public int getEndColor() {
        return Color.parseColor(end);
    }

    public int getCenterColor() {
        return Color.parseColor(center);
    }

    /**
     * Get the actual {@link GradientDrawable.Orientation} from given {@link #angle}
     *
     * @return {@link GradientDrawable.Orientation}
     */
    public GradientDrawable.Orientation getGradiantAngle() {
        Orientation fixedAngle = Orientation.valueOf(getAngleAsEnumString(angle));
        return fixedAngle.getValue();
    }

    /**
     * Finds the nearest angle from given {@link #AVAILABLE_ANGLES} for the given {@link #angle}
     *
     * @param angle int value from 0 to 360
     * @return String equivalent to {@link Orientation#name()}
     */
    private String getAngleAsEnumString(int angle) {
        int distance = Math.abs(AVAILABLE_ANGLES[0] - angle);
        int index = 0;

        for (int tempIndex = 1; tempIndex < AVAILABLE_ANGLES.length; tempIndex++) {
            int tempDistance = Math.abs(AVAILABLE_ANGLES[tempIndex] - angle);
            if (tempDistance < distance) {
                index = tempIndex;
                distance = tempDistance;
            }
        }

        return "DEGREE" + (AVAILABLE_ANGLES[index] + 90);
    }

    // Gradient requires two colours minimum.
    private int[] getColours() {
        ArrayList<Integer> integers = new ArrayList<>();

        if (!TextUtils.isEmpty(start)) {
            integers.add(getStartColor());
        }

        if (!TextUtils.isEmpty(center)) {
            integers.add(getCenterColor());
        }

        if (!TextUtils.isEmpty(end)) {
            integers.add(getEndColor());
        }

        int[] array = new int[integers.size()];

        for (int i = 0; i < integers.size(); i++) {
            array[i] = integers.get(i);
        }

        return array;
    }

    public void updateDrawable(GradientDrawable drawable) {
        drawable.setGradientType(getType().value);
        drawable.setOrientation(getGradiantAngle());
        if (getType() == Type.RADIAL) {
            drawable.setGradientRadius(angle);
        }
        drawable.setColors(getColours());
    }

}
