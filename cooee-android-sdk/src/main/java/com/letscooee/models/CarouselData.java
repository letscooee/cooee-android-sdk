package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CarouselData implements Parcelable {
    private String productName;
    private String productId;
    private String imageUrl;
    private boolean showButton;
    private boolean showBanner;
    private String backgroundColor;
    private String textColor;
    private String text;


    protected CarouselData(Parcel in) {
        productName = in.readString();
        productId = in.readString();
        imageUrl = in.readString();
        showButton = in.readByte() != 0;
        showBanner = in.readByte() != 0;
        backgroundColor = in.readString();
        textColor = in.readString();
        text = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productName);
        dest.writeString(productId);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (showButton ? 1 : 0));
        dest.writeByte((byte) (showBanner ? 1 : 0));
        dest.writeString(backgroundColor);
        dest.writeString(textColor);
        dest.writeString(text);
    }

    public static final Creator<CarouselData> CREATOR = new Creator<CarouselData>() {
        @Override
        public CarouselData createFromParcel(Parcel in) {
            return new CarouselData(in);
        }

        @Override
        public CarouselData[] newArray(int size) {
            return new CarouselData[size];
        }
    };

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isShowButton() {
        return showButton;
    }

    public void setShowButton(boolean showButton) {
        this.showButton = showButton;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public boolean isShowBanner() {
        return showBanner;
    }

    public void setShowBanner(boolean showBanner) {
        this.showBanner = showBanner;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
