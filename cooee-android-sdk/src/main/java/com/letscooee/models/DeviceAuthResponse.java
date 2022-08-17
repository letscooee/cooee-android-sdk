package com.letscooee.models;

/**
 * This class will help in storing sdkToken received from the server when app is opened first time.
 *
 * @author Abhishek Taparia
 */
public class DeviceAuthResponse {

    private String sdkToken;
    private String sessionID;
    private String deviceID;
    private String userID;

    public String getSdkToken() {
        return sdkToken;
    }

    public void setSdkToken(String sdkToken) {
        this.sdkToken = sdkToken;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
