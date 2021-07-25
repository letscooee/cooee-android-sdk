package com.letscooee.models.trigger.push;

import android.os.Parcel;
import android.os.Parcelable;
import com.letscooee.enums.trigger.PushNotificationImportance;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.models.trigger.elements.ButtonElement;
import com.letscooee.models.trigger.elements.TextElement;

import java.util.ArrayList;

public class PushNotificationTrigger implements Parcelable {

    private final TextElement title;
    private final TextElement body;
    private final String smallImage;
    private final String largeImage;
    private final ArrayList<ButtonElement> buttons;

    private PushNotificationImportance importance;
    public final boolean vibrate = true;
    public final boolean sound = true;
    public final boolean lights = true;

    public PushNotificationImportance getImportance() {
        if (importance == null) {
            return PushNotificationImportance.HIGH;
        }
        return importance;
    }

    protected PushNotificationTrigger(Parcel in) {
        title = in.readParcelable(TextElement.class.getClassLoader());
        body = in.readParcelable(TextElement.class.getClassLoader());
        click = in.readParcelable(getClass().getClassLoader());
        smallImage = in.readString();
        largeImage = in.readString();
        buttons = in.createTypedArrayList(ButtonElement.CREATOR);
    }

    public static final Creator<PushNotificationTrigger> CREATOR = new Creator<PushNotificationTrigger>() {
        @Override
        public PushNotificationTrigger createFromParcel(Parcel in) {
            return new PushNotificationTrigger(in);
        }

        @Override
        public PushNotificationTrigger[] newArray(int size) {
            return new PushNotificationTrigger[size];
        }
    };

    public TextElement getTitle() {
        return title;
    }

    public TextElement getBody() {
        return body;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public String getLargeImage() {
        return largeImage;
    }

    public ArrayList<ButtonElement> getButtons() {
        return buttons;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(title, flags);
        dest.writeParcelable(body, flags);
        dest.writeParcelable(click, flags);
        dest.writeString(smallImage);
        dest.writeString(largeImage);
        dest.writeTypedList(buttons);
    }
}
