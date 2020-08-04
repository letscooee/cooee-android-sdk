package com.letscooee.models;

/**
 * @author Abhishek Taparia
 * The Content class will store the data about contents of campaigns received from the server
 */
public class Content {
    private String mediaType;
    private String mediaData;
    private String mediaUrl;
    private String description;
    private String title;
    private Layout layout;

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaData() {
        return mediaData;
    }

    public void setMediaData(String mediaData) {
        this.mediaData = mediaData;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }
}
