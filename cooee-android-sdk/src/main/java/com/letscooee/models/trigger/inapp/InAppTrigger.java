package com.letscooee.models.trigger.inapp;

import static com.letscooee.utils.Constants.TAG;

import android.os.Parcel;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.trigger.Gravity;
import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.models.trigger.elements.BaseElement;

import java.util.ArrayList;

public class InAppTrigger extends BaseElement {

    @SerializedName("cont")
    @Expose
    private Container container;

    @SerializedName("elems")
    @Expose
    private ArrayList<BaseElement> elements;

    @SerializedName("o")
    @Expose
    private byte gravity;

    protected InAppTrigger(Parcel in) {
        super(in);
        container = in.readParcelable(Container.class.getClassLoader());
        elements = in.readArrayList(getClass().getClassLoader());
        gravity = in.readByte();
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
        super.writeToParcel(dest, flags);
        dest.writeParcelable(container, flags);
        dest.writeList(elements);
        dest.writeByte(gravity);
    }

    /**
     * Process In-App gravity on the screen
     *
     * @return Return Nullable {@link android.view.Gravity} as {@link Integer} value
     */
    public Gravity getGravity() {
        if (gravity == 0) {
            gravity = container.getGravity();
        }

        return Gravity.fromByte(gravity);
    }

    @Override
    public double getWidth() {
        return width <= 0 ? 1080 : width;
    }

    @Override
    public double getHeight() {
        return height <= 0 ? 1920 : height;
    }

    /**
     * Checks and returns {@link ClickAction} for {@link InAppTrigger}. If <code>clickAction</code>
     * is <code>null</code> then creates new {@link ClickAction} and assign it to variable
     *
     * @return Returns non-null {@link ClickAction}
     */
    @NonNull
    @Override
    public ClickAction getClickAction() {
        return super.getClickAction() == null ? new ClickAction(true) : super.getClickAction();
    }

    /**
     * Check for {@link InAppTrigger} <code>background</code> and return it.
     * If <code>background</code> is <code>null</code> method will pick <code>background</code> from
     * {@link Container} and will add to InApp's <code>background</code> and then replace Container's
     * background with <code>null</code>
     *
     * @return NonNull instance of {@link Background}
     */
    @Nullable
    @Override
    public Background getBg() {
        if (bg == null) {
            bg = container.getBg();
            container.setBg(null);

        }
        return bg;
    }
}
