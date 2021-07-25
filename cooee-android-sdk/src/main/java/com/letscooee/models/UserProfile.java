package com.letscooee.models;

import java.util.Map;

/**
 * UserProfile class will store user data from server
 *
 * @author Abhishek Taparia
 */
public class UserProfile {

    Map<String, String> deviceData;
    Map<String, String> userData;
    Map<String, String> userProperties;

    public UserProfile(Map<String, String> deviceData, Map<String, String> userData, Map<String, String> userProperties) {
        this.deviceData = deviceData;
        this.userData = userData;
        this.userProperties = userProperties;
    }

    public Map<String, String> getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(Map<String, String> deviceData) {
        this.deviceData = deviceData;
    }

    public Map<String, String> getUserData() {
        return userData;
    }

    public void setUserData(Map<String, String> userData) {
        this.userData = userData;
    }

    public Map<String, String> getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(Map<String, String> userProperties) {
        this.userProperties = userProperties;
    }
}
