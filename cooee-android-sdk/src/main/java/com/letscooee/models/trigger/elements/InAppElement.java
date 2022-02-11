package com.letscooee.models.trigger.elements;

import android.os.Parcel;

import androidx.annotation.*;

import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.blocks.ClickAction;

/**
 * Provides {@link BaseElement} instance to process InAppTrigger's background and clickAction
 */
public class InAppElement extends BaseElement {

    protected InAppElement(Parcel in) {
        super(in);
    }

    public InAppElement(@Nullable Background background, @NonNull ClickAction clickAction) {
        super(background, clickAction);
    }

    public static final Creator<InAppElement> CREATOR = new Creator<InAppElement>() {
        @Override
        public InAppElement createFromParcel(Parcel in) {
            return new InAppElement(in);
        }

        @Override
        public InAppElement[] newArray(int size) {
            return new InAppElement[size];
        }
    };
}
