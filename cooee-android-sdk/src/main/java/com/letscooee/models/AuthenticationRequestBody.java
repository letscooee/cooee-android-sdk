package com.letscooee.models;

/**
 * AuthenticationRequestBody class is used in sending request body for the first time to get sdkToken from server
 *
 * @author Abhishek Taparia
 * @version 0.0.1
 */
public class AuthenticationRequestBody {
    private String appID;
    private String appSecret;
    private DeviceData deviceData;

    public AuthenticationRequestBody(String appID, String appSecret, DeviceData deviceData) {
        this.appID = appID;
        this.appSecret = appSecret;
        this.deviceData = deviceData;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public DeviceData getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(DeviceData deviceData) {
        this.deviceData = deviceData;
    }
}

