package com.wizpanda.cooee.models;

/**
 * The Campaign class will store the data about the campaigns received from the server
 */


public class Campaign {
    private String subtitle;
    private String title;
    private String description;

    /* mediaURL will have the s3 presigned url for the media*/
    private String mediaURL;
    private String mediaData;

    /* mediaType id the type of media available in mediaURL such as jpeg, png, mp4, etc. */
    private String mediaType;

    /* engagementMode stores the type of engagement like image, splash, video, push notification and poll*/
    private String engagementMode;

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public String getMediaData() {
        return mediaData;
    }

    public void setMediaData(String mediaData) {
        this.mediaData = mediaData;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getEngagementMode() {
        return engagementMode;
    }

    public void setEngagementMode(String engagementMode) {
        this.engagementMode = engagementMode;
    }
}
