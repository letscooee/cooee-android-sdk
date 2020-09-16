package com.letscooee.models;

/**
 * @author Abhishek Taparia
 * The TriggerData class will store the data about user properties, devices for campaigns received from the server
 */
public class TriggerData {
    private String userProperties;
    private String devices;
    private String campaignLimits;

    public String getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(String userProperties) {
        this.userProperties = userProperties;
    }

    public String getDevices() {
        return devices;
    }

    public void setDevices(String devices) {
        this.devices = devices;
    }

    public String getCampaignLimits() {
        return campaignLimits;
    }

    public void setCampaignLimits(String campaignLimits) {
        this.campaignLimits = campaignLimits;
    }
}
