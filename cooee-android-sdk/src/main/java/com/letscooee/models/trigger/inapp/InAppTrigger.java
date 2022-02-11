package com.letscooee.models.trigger.inapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.trigger.Gravity;
import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.inapp.renderer.utils.GravityUtil;

import java.util.ArrayList;

public class InAppTrigger implements Parcelable {

    @SerializedName("cont")
    @Expose
    private Container container;

    @SerializedName("elems")
    @Expose
    private ArrayList<BaseElement> elements;

    @SerializedName("o")
    @Expose
    private byte gravity;

    @SerializedName("w")
    @Expose
    private double width;

    @SerializedName("h")
    @Expose
    private double height;

    @SerializedName("clc")
    @Expose
    private ClickAction clickAction;

    @SerializedName("bg")
    @Expose
    private Background background;

    protected InAppTrigger(Parcel in) {
        container = in.readParcelable(Container.class.getClassLoader());
        elements = in.readArrayList(getClass().getClassLoader());
        gravity = in.readByte();
        width = in.readDouble();
        height = in.readDouble();
        background = in.readParcelable(Background.class.getClassLoader());
        clickAction = in.readParcelable(ClickAction.class.getClassLoader());
    }

    public static final Creator<InAppTrigger> CREATOR = new Creator<InAppTrigger>() {
        @Override
        public InAppTrigger createFromParcel(Parcel in) {
            return new InAppTrigger(in);
        }

        @Override
        public InAppTrigger[] newArray(int size) {
            return new InAppTrigger[size];
        }
    };

    public Container getContainer() {
        return container;
    }

    public ArrayList<BaseElement> getElements() {
        return elements;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(container, flags);
        dest.writeList(elements);
        dest.writeByte(gravity);
        dest.writeDouble(width);
        dest.writeDouble(height);
        dest.writeParcelable(background, flags);
        dest.writeParcelable(clickAction, flags);
    }

    /**
     * Process In-App gravity on the screen
     *
     * @param layoutParams {@link RelativeLayout.LayoutParams} of the view
     * @return Return Nullable {@link android.view.Gravity} as {@link Integer} value
     */
    @Nullable
    public Integer getGravity(@NonNull RelativeLayout.LayoutParams layoutParams) {
        if (gravity == 0) {
            return null;
        }

        return GravityUtil.processGravity(Gravity.fromByte(gravity), layoutParams);
    }

    public int getWidth() {
        if (width <= 0) {
            return 1080; // default width
        }

        return (int) Math.round(width);
    }

    public int getHeight() {
        if (height <= 0) {
            return 1920; // / default height
        }

        return (int) Math.round(height);
    }

    /**
     * Checks and returns {@link ClickAction} for {@link InAppTrigger}. If <code>clickAction</code>
     * is <code>null</code> then creates new {@link ClickAction} and assign it to variable
     *
     * @return Returns non-null {@link ClickAction}
     */
    @NonNull
    public ClickAction getClickAction() {
        if (clickAction == null) {
            clickAction = new ClickAction(true);
        }

        return clickAction;
    }

    @Nullable
    public Background getBackground() {
        if (background == null){
            background = container.getBg();
        }

        return background;
    }
}
