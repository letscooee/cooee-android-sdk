package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

/**
 * Model class for storing engagement data from server
 *
 * @author Abhishek Taparia
 */
public class TriggerData implements Parcelable {
    public enum Type {
        IMAGE, VIDEO
    }

    public enum Fill {
        COVER, INTERSTITIAL, HALF_INTERSTITIAL, HEADER, FOOTER
    }

    public enum EntranceAnimation {
        SLIDE_IN_TOP, SLIDE_IN_DOWN, SLIDE_IN_LEFT, SLIDE_IN_RIGHT
    }

    public enum ExitAnimation {
        SLIDE_OUT_TOP, SLIDE_OUT_DOWN, SLIDE_OUT_LEFT, SLIDE_OUT_RIGHT
    }

    public enum CloseButtonPosition {
        TOP_RIGHT, TOP_LEFT, DOWN_RIGHT, DOWN_LEFT
    }

    public enum TextPosition {
        TOP, BOTTOM, LEFT, RIGHT
    }

    private int id;
    private com.letscooee.models.TriggerData.Type type;
    private com.letscooee.models.TriggerData.Fill fill;
    private TriggerBackground background;
    private String imageUrl;
    private String videoUrl;
    private com.letscooee.models.TriggerData.EntranceAnimation entranceAnimation;
    private com.letscooee.models.TriggerData.ExitAnimation exitAnimation;
    private Object autoClose;
    private com.letscooee.models.TriggerData.CloseButtonPosition closeButtonPosition;
    private TriggerText text;
    private TriggerText message;
    private com.letscooee.models.TriggerData.TextPosition textPosition;
    private boolean isAutoClose;

    private String inappActionButtonText;
    private String inappActionButtonColor;

    public String getInappActionButtonText() {
        return inappActionButtonText;
    }

    public void setInappActionButtonText(String inappActionButtonText) {
        this.inappActionButtonText = inappActionButtonText;
    }

    public String getInappActionButtonColor() {
        return inappActionButtonColor;
    }

    public void setInappActionButtonColor(String inappActionButtonColor) {
        this.inappActionButtonColor = inappActionButtonColor;
    }

    public static final Creator<com.letscooee.models.TriggerData> CREATOR = new Creator<com.letscooee.models.TriggerData>() {
        @Override
        public com.letscooee.models.TriggerData createFromParcel(Parcel in) {
            return new com.letscooee.models.TriggerData(in);
        }

        @Override
        public com.letscooee.models.TriggerData[] newArray(int size) {
            return new com.letscooee.models.TriggerData[size];
        }
    };

    public TriggerData() {
    }

    protected TriggerData(Parcel in) {
        id = in.readInt();
        background = in.readParcelable(TriggerBackground.class.getClassLoader());
        imageUrl = in.readString();
        videoUrl = in.readString();
        text = in.readParcelable(TriggerText.class.getClassLoader());
        message = in.readParcelable(TriggerText.class.getClassLoader());
        entranceAnimation = com.letscooee.models.TriggerData.EntranceAnimation.valueOf(in.readString());
        exitAnimation = com.letscooee.models.TriggerData.ExitAnimation.valueOf(in.readString());
        type = com.letscooee.models.TriggerData.Type.valueOf(in.readString());
        fill = com.letscooee.models.TriggerData.Fill.valueOf(in.readString());
        closeButtonPosition = com.letscooee.models.TriggerData.CloseButtonPosition.valueOf(in.readString());
        textPosition = com.letscooee.models.TriggerData.TextPosition.valueOf(in.readString());
        inappActionButtonColor = in.readString();
        inappActionButtonText = in.readString();
        autoClose = in.readInt();
        if (Integer.parseInt(autoClose.toString()) > 0) {
            isAutoClose = true;
        } else {
            isAutoClose = false;
        }
    }

    public TriggerData(Map<String, String> triggerData) {
        id = Integer.parseInt(triggerData.get("id"));
        background = new TriggerBackground(triggerData.get("backgroundType"), triggerData.get("backgroundColor"), triggerData.get("backgroundImage"), triggerData.get("backgroundBlur"));
        imageUrl = triggerData.get("imageUrl");
        videoUrl = triggerData.get("videoUrl");
        text = new TriggerText(triggerData.get("textData"), triggerData.get("textColor"), triggerData.get("textSize"));
        message = new TriggerText(triggerData.get("messageData"), triggerData.get("messageColor"), triggerData.get("messageSize"));
        entranceAnimation = com.letscooee.models.TriggerData.EntranceAnimation.valueOf(triggerData.get("entranceAnimation"));
        exitAnimation = com.letscooee.models.TriggerData.ExitAnimation.valueOf(triggerData.get("exitAnimation"));
        type = com.letscooee.models.TriggerData.Type.valueOf(triggerData.get("type"));
        fill = com.letscooee.models.TriggerData.Fill.valueOf(triggerData.get("fill"));
        closeButtonPosition = com.letscooee.models.TriggerData.CloseButtonPosition.valueOf(triggerData.get("closeButtonPosition"));
        textPosition = com.letscooee.models.TriggerData.TextPosition.valueOf(triggerData.get("textPosition"));
        inappActionButtonText = triggerData.get("inappActionButtonText");
        inappActionButtonColor = triggerData.get("inappActionButtonColor");
        try {
            autoClose = Integer.parseInt(triggerData.get("autoClose"));
        } catch (Exception ignored) {
            autoClose = Boolean.parseBoolean(triggerData.get("autoClose"));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(background, flags);
        dest.writeString(imageUrl);
        dest.writeString(videoUrl);
        dest.writeParcelable(text, flags);
        dest.writeParcelable(message, flags);
        dest.writeString(entranceAnimation.name());
        dest.writeString(exitAnimation.name());
        dest.writeString(type.name());
        dest.writeString(fill.name());
        dest.writeString(closeButtonPosition.name());
        dest.writeString(textPosition.name());
        dest.writeString(inappActionButtonColor);
        dest.writeString(inappActionButtonText);
        try {
            dest.writeInt((Integer) autoClose);
        } catch (ClassCastException ignored) {
        }
    }

    public boolean isAutoClose() {
        return isAutoClose;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public com.letscooee.models.TriggerData.Type getType() {
        return type;
    }

    public void setType(com.letscooee.models.TriggerData.Type type) {
        this.type = type;
    }

    public com.letscooee.models.TriggerData.Fill getFill() {
        return fill;
    }

    public void setFill(com.letscooee.models.TriggerData.Fill fill) {
        this.fill = fill;
    }

    public TriggerBackground getBackground() {
        return background;
    }

    public void setBackground(TriggerBackground background) {
        this.background = background;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public com.letscooee.models.TriggerData.EntranceAnimation getEntranceAnimation() {
        return entranceAnimation;
    }

    public void setEntranceAnimation(com.letscooee.models.TriggerData.EntranceAnimation entranceAnimation) {
        this.entranceAnimation = entranceAnimation;
    }

    public com.letscooee.models.TriggerData.ExitAnimation getExitAnimation() {
        return exitAnimation;
    }

    public void setExitAnimation(com.letscooee.models.TriggerData.ExitAnimation exitAnimation) {
        this.exitAnimation = exitAnimation;
    }

    public Object getAutoClose() {
        return autoClose;
    }

    public void setAutoClose(Object autoClose) {
        this.autoClose = autoClose;
    }

    public com.letscooee.models.TriggerData.CloseButtonPosition getCloseButtonPosition() {
        return closeButtonPosition;
    }

    public void setCloseButtonPosition(com.letscooee.models.TriggerData.CloseButtonPosition closeButtonPosition) {
        this.closeButtonPosition = closeButtonPosition;
    }

    public TriggerText getText() {
        return text;
    }

    public void setText(TriggerText text) {
        this.text = text;
    }

    public TriggerText getMessage() {
        return message;
    }

    public void setMessage(TriggerText message) {
        this.message = message;
    }

    public com.letscooee.models.TriggerData.TextPosition getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(com.letscooee.models.TriggerData.TextPosition textPosition) {
        this.textPosition = textPosition;
    }
}

