package com.letscooee.models.trigger.elements;

import android.os.Parcel;
import android.text.TextUtils;

import com.letscooee.exceptions.InvalidTriggerDataException;
import java.util.List;

public class ImageElement extends BaseElement {

    private final String src;

    protected ImageElement(Parcel in) {
        super(in);
        src = in.readString();
    }

    public static final Creator<ImageElement> CREATOR = new Creator<ImageElement>() {
        @Override
        public ImageElement createFromParcel(Parcel in) {
            return new ImageElement(in);
        }

        @Override
        public ImageElement[] newArray(int size) {
            return new ImageElement[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(src);
    }

    public String getSrc() {
        return src;
    }

    public List<String> getImageURLs() {
        List<String> urls = super.getImageURLs();
        if (!TextUtils.isEmpty(src)) {
            urls.add(src);
        }

        return urls;
    }

    /**
     * Checks if the image element has valid image resource.
     *
     * @return true if the image element has valid image resource.
     * @throws InvalidTriggerDataException if the image element has no/empty image resource.
     */
    @Override
    public boolean hasValidImageResource() throws InvalidTriggerDataException {
        if (TextUtils.isEmpty(src)) {
            throw new InvalidTriggerDataException("ImageElement has no/empty src");
        }

        return true;
    }
}
