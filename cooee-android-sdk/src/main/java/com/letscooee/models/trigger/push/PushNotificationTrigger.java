package com.letscooee.models.trigger.push;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.trigger.PushNotificationImportance;
import com.letscooee.models.trigger.blocks.ClickAction;
import com.letscooee.models.trigger.elements.ButtonElement;
import com.letscooee.models.trigger.elements.TextElement;

import java.util.ArrayList;

public class PushNotificationTrigger implements Parcelable {

    @SerializedName("t")
    @Expose
    private final TextElement title;

    @SerializedName("b")
    @Expose
    private final TextElement body;

    @SerializedName("si")
    @Expose
    private final String smallImage;

    @SerializedName("li")
    @Expose
    private final String largeImage;

    @SerializedName("btns")
    @Expose
    private final ArrayList<ButtonElement> buttons;

    @SerializedName("clc")
    @Expose
    private final ClickAction clickAction;

    @SerializedName("pt")
    @Expose
    private final int pushType;

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
        smallImage = in.readString();
        largeImage = in.readString();
        buttons = in.createTypedArrayList(ButtonElement.CREATOR);
        clickAction = in.readParcelable(ClickAction.class.getClassLoader());
        pushType = in.readInt();
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

    public ClickAction getClickAction() {
        return clickAction;
    }

    public int getPushType() {
        return pushType == 0 ? 1 : pushType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(title, flags);
        dest.writeParcelable(body, flags);
        dest.writeString(smallImage);
        dest.writeString(largeImage);
        dest.writeTypedList(buttons);
        dest.writeParcelable(clickAction, flags);
        dest.writeInt(pushType == 0 ? 1 : pushType);
    }
}
