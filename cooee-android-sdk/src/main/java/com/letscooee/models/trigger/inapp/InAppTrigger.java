package com.letscooee.models.trigger.inapp;

import android.os.Parcel;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.trigger.Gravity;
import com.letscooee.exceptions.InvalidTriggerDataException;
import com.letscooee.models.trigger.blocks.Animation;
import com.letscooee.models.trigger.blocks.Background;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.trigger.TriggerDataHelper;
import java.util.ArrayList;
import java.util.List;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;

public class InAppTrigger extends BaseElement {

    @SerializedName("cont")
    @Expose
    private final Container container;

    @SerializedName("elems")
    @Expose
    private final ArrayList<BaseElement> elements;

    @SerializedName("gvt")
    @Expose
    private byte gravity;

    @SerializedName("anim")
    @Expose
    private final Animation animation;

    protected InAppTrigger(Parcel in) {
        super(in);
        container = in.readParcelable(Container.class.getClassLoader());
        elements = in.readArrayList(getClass().getClassLoader());
        gravity = in.readByte();
        animation = in.readParcelable(Animation.class.getClassLoader());
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

    @NonNull
    public ArrayList<BaseElement> getElements() {
        return elements != null ? elements : new ArrayList<>();
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
        dest.writeParcelable(animation, flags);
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

    /**
     * Check for {@link InAppTrigger} <code>animation</code> and return it.
     *
     * @return Nullable instance of {@link Animation}
     */
    public Animation getAnimation() {
        return animation;
    }

    public List<String> getImageURLs() {
        // Collect and filter null/empty image URL from all elements
        List<String> imageList = StreamSupport.stream(elements)
                .map(BaseElement::getImageURL)
                .filter(s -> !TextUtils.isEmpty(s))
                .collect(Collectors.toList());

        // Background image URL of InAppTrigger.
        if (!TextUtils.isEmpty(getImageURL())) {
            imageList.add(getImageURL());
        }

        // Background image URL of Container.
        if (!TextUtils.isEmpty(container.getImageURL())) {
            imageList.add(container.getImageURL());
        }

        return imageList;
    }

    /**
     * Deserialize {@link InAppTrigger} from JSON
     *
     * @param jsonString JSON String
     * @return NonNull instance of {@link InAppTrigger}
     * @throws JsonSyntaxException If JSON is not valid
     */
    @NonNull
    public static InAppTrigger fromJson(@NonNull String jsonString) throws JsonSyntaxException {
        return TriggerDataHelper.getGson().fromJson(jsonString, InAppTrigger.class);
    }

    /**
     * Checks if InAppTrigger is valid container, elements and background image.
     *
     * @return True if InAppTrigger is valid
     * @throws InvalidTriggerDataException If InAppTrigger is not valid
     */
    @Override
    public boolean hasValidResource() throws InvalidTriggerDataException {
        return super.hasValidResource() && container != null && container.hasValidResource()
                && elements != null && !elements.isEmpty() && containsValidChildren();
    }

    /**
     * Checks if elements contains valid children
     *
     * @return True if elements contains valid children
     * @throws InvalidTriggerDataException If elements contains invalid children
     */
    private boolean containsValidChildren() throws InvalidTriggerDataException {
        for (BaseElement element : elements) {
            if (!element.hasValidResource()) {
                return false;
            }
        }

        return true;
    }
}
