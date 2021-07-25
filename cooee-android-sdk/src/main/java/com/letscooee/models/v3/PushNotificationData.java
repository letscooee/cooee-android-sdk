package com.letscooee.models.v3;

import android.os.Parcel;
import android.os.Parcelable;
import com.letscooee.models.trigger.PushNotification;
import com.letscooee.models.trigger.PushNotificationImportance;
import com.letscooee.models.v3.element.ButtonElement;
import com.letscooee.models.v3.element.TextElement;

import java.util.ArrayList;

public class PushNotificationData implements Parcelable {

    private TextElement title;
    private TextElement body;
    private String smallImage;
    private String largeImage;
    private ArrayList<ButtonElement> buttons;
    public PushNotification pn;

    public PushNotificationImportance getImportance() {
        if (pn == null || pn.importance == null) {
            return PushNotificationImportance.HIGH;
        }
        return pn.importance;
    }


    protected PushNotificationData(Parcel in) {
        title = in.readParcelable(TextElement.class.getClassLoader());
        body = in.readParcelable(TextElement.class.getClassLoader());
        smallImage = in.readString();
        largeImage = in.readString();
        buttons = in.createTypedArrayList(ButtonElement.CREATOR);
        pn = in.readParcelable(PushNotificationImportance.class.getClassLoader());
    }

    public static final Creator<PushNotificationData> CREATOR = new Creator<PushNotificationData>() {
        @Override
        public PushNotificationData createFromParcel(Parcel in) {
            return new PushNotificationData(in);
        }

        @Override
        public PushNotificationData[] newArray(int size) {
            return new PushNotificationData[size];
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
        dest.writeString(smallImage);
        dest.writeString(largeImage);
        dest.writeTypedList(buttons);
        dest.writeParcelable(pn, flags);
    }
}
