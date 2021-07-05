package com.letscooee.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

public class DataBlock implements Parcelable {

    public enum DataType {TEXT, IMAGE, BUTTON, VIDEO}

    public enum TextStyle {BOLD, Italic, BOLD_ITALIC}

    public enum FillType {POSITIONED, FILL,COVER}

    private String url;
    private String textContent;
    private String color;
    private Double xPosition;
    private Double yPosition;
    private Double width;
    private Double height;
    private boolean keepVideoMute;
    private boolean canUserPauseVideo;
    private Integer textSize;
    private DataType type;
    private TextStyle textStyle;
    private FillType fillType;

    protected DataBlock(Parcel in) {
        url = in.readString();
        textContent = in.readString();
        color = in.readString();
        if (in.readByte() == 0) {
            xPosition = null;
        } else {
            xPosition = in.readDouble();
        }
        if (in.readByte() == 0) {
            yPosition = null;
        } else {
            yPosition = in.readDouble();
        }
        if (in.readByte() == 0) {
            width = null;
        } else {
            width = in.readDouble();
        }
        if (in.readByte() == 0) {
            height = null;
        } else {
            height = in.readDouble();
        }
        keepVideoMute = in.readByte() != 0;
        canUserPauseVideo = in.readByte() != 0;
        if (in.readByte() == 0) {
            textSize = null;
        } else {
            textSize = in.readInt();
        }
        textStyle = TextStyle.valueOf(in.readString());
        type = DataType.valueOf(in.readString());
        fillType=FillType.valueOf(in.readString());
    }

    public static final Creator<DataBlock> CREATOR = new Creator<DataBlock>() {
        @Override
        public DataBlock createFromParcel(Parcel in) {
            return new DataBlock(in);
        }

        @Override
        public DataBlock[] newArray(int size) {
            return new DataBlock[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(textContent);
        dest.writeString(color);
        if (xPosition == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(xPosition);
        }
        if (yPosition == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(yPosition);
        }
        if (width == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(width);
        }
        if (height == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(height);
        }
        dest.writeByte((byte) (keepVideoMute ? 1 : 0));
        dest.writeByte((byte) (canUserPauseVideo ? 1 : 0));
        if (textSize == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(textSize);
        }

        dest.writeString(textStyle.name());
        dest.writeString(type.name());
        dest.writeString(fillType.name());
    }

    public String getUrl() {
        return url;
    }

    public String getTextContent() {
        return textContent;
    }

    public String getColor() {
        return color;
    }

    public Double getxPosition() {
        return xPosition;
    }

    public Double getyPosition() {
        return yPosition;
    }

    public Double getWidth() {
        return width;
    }

    public Double getHeight() {
        return height;
    }

    public boolean isKeepVideoMute() {
        return keepVideoMute;
    }

    public boolean isCanUserPauseVideo() {
        return canUserPauseVideo;
    }

    public Integer getTextSize() {
        return textSize;
    }

    public DataType getType() {
        return type;
    }

    public TextStyle getTextStyle() {
        return textStyle;
    }

    public FillType getFillType() {
        return fillType;
    }
}