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
        COVER, INTERSTITIAL, HALF_INTERSTITIAL, HEADER, FOOTER, SIDE_POP
    }

    public enum EntranceAnimation {
        SLIDE_IN_TOP, SLIDE_IN_DOWN, SLIDE_IN_LEFT, SLIDE_IN_RIGHT
    }

    public enum ExitAnimation {
        SLIDE_OUT_TOP, SLIDE_OUT_DOWN, SLIDE_OUT_LEFT, SLIDE_OUT_RIGHT
    }

    public boolean isShowAsPN() {
        return showAsPN;
    }

    public TriggerCloseBehaviour getCloseBehaviour() {
        return closeBehaviour;
    }

    public TriggerButton[] getButtons() {
        return buttons;
    }

    private String id;
    private Type type;
    private Fill fill;
    private long duration;
    private TriggerBehindBackground triggerBackground;
    private TriggerBackground background;
    private boolean showAsPN;
    private String imageUrl;
    private String layeredImageUrl;
    private String videoUrl;
    private EntranceAnimation entranceAnimation;
    private ExitAnimation exitAnimation;
    private TriggerCloseBehaviour closeBehaviour;
    private TriggerText title;
    private TriggerText message;
    private TriggerButton[] buttons;
    private boolean isCarousel;
    private SidePopSetting sidePopSetting;
    private CarouselData[] carouselData;
    private int imageShadow;
    private boolean showImageShadow;
    private String imageUrl1;
    private int carouselOffset = 1;

    public SidePopSetting getSidePopSetting() {
        return sidePopSetting;
    }

    public void setCarousel(boolean carousel) {
        isCarousel = carousel;
    }

    public CarouselData[] getCarouselData() {
        return carouselData;
    }

    public void setCarouselData(CarouselData[] carouselData) {
        this.carouselData = carouselData;
    }

    public boolean isCarousel() {
        return isCarousel;
    }

    public TriggerData() {
    }

    public TriggerData(Map<String, String> triggerData) {
    }

    public int getCarouselOffset() {
        return carouselOffset;
    }

    protected TriggerData(Parcel in) {
        id = in.readString();
        duration = in.readLong();
        triggerBackground = in.readParcelable(TriggerBehindBackground.class.getClassLoader());
        background = in.readParcelable(TriggerBackground.class.getClassLoader());
        showAsPN = in.readByte() != 0;
        imageUrl = in.readString();
        layeredImageUrl = in.readString();
        videoUrl = in.readString();
        closeBehaviour = in.readParcelable(TriggerCloseBehaviour.class.getClassLoader());
        title = in.readParcelable(TriggerText.class.getClassLoader());
        message = in.readParcelable(TriggerText.class.getClassLoader());
        entranceAnimation = EntranceAnimation.valueOf(in.readString());
        exitAnimation = ExitAnimation.valueOf(in.readString());
        type = Type.valueOf(in.readString());
        fill = Fill.valueOf(in.readString());
        buttons = in.createTypedArray(TriggerButton.CREATOR);
        isCarousel = in.readByte() != 0;
        carouselData = in.createTypedArray(CarouselData.CREATOR);
        sidePopSetting = in.readParcelable(SidePopSetting.class.getClassLoader());
        imageShadow = in.readInt();
        showImageShadow = in.readByte() != 0;
        imageUrl1 = in.readString();
        carouselOffset = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(duration);
        dest.writeParcelable(triggerBackground, flags);
        dest.writeParcelable(background, flags);
        dest.writeByte((byte) (showAsPN ? 1 : 0));
        dest.writeString(imageUrl);
        dest.writeString(layeredImageUrl);
        dest.writeString(videoUrl);
        dest.writeParcelable(closeBehaviour, flags);
        dest.writeParcelable(title, flags);
        dest.writeParcelable(message, flags);
        dest.writeString(entranceAnimation.name());
        dest.writeString(exitAnimation.name());
        dest.writeString(type.name());
        dest.writeString(fill.name());
        dest.writeTypedArray(buttons, flags);
        dest.writeByte((byte) (isCarousel ? 1 : 0));
        dest.writeTypedArray(carouselData, flags);
        dest.writeParcelable(sidePopSetting, flags);
        dest.writeInt(imageShadow);
        dest.writeByte((byte) (showImageShadow ? 1 : 0));
        dest.writeString(imageUrl1);
        dest.writeInt(carouselOffset);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TriggerData> CREATOR = new Creator<TriggerData>() {
        @Override
        public TriggerData createFromParcel(Parcel in) {
            return new TriggerData(in);
        }

        @Override
        public TriggerData[] newArray(int size) {
            return new TriggerData[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Fill getFill() {
        return fill;
    }

    public void setFill(Fill fill) {
        this.fill = fill;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
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

    public String getImageUrl1() {
        return imageUrl1;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLayeredImageUrl() {
        return layeredImageUrl;
    }

    public void setLayeredImageUrl(String layeredImageUrl) {
        this.layeredImageUrl = layeredImageUrl;
    }


    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public EntranceAnimation getEntranceAnimation() {
        return entranceAnimation;
    }

    public void setEntranceAnimation(EntranceAnimation entranceAnimation) {
        this.entranceAnimation = entranceAnimation;
    }

    public ExitAnimation getExitAnimation() {
        return exitAnimation;
    }

    public void setExitAnimation(ExitAnimation exitAnimation) {
        this.exitAnimation = exitAnimation;
    }

    public TriggerText getTitle() {
        return title;
    }

    public void setTitle(TriggerText title) {
        this.title = title;
    }

    public TriggerText getMessage() {
        return message;
    }

    public void setMessage(TriggerText message) {
        this.message = message;
    }

    public TriggerBehindBackground getTriggerBackground() {
        return triggerBackground;
    }

    public Integer getImageShadow() {
        return imageShadow;
    }

    public boolean isShowImageShadow() {
        return showImageShadow;
    }

    @Override
    public String toString() {
        return "TriggerData{" +
                "id=" + id +
                ", type=" + type +
                ", title=" + title +
                ", message=" + message +
                '}';
    }
}
