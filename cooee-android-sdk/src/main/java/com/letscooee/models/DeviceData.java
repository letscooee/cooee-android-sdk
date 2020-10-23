package com.letscooee.models;

/**
 * DeviceData class is used in AuthenticationRequestBody class as member variable.
 *
 * @author Abhishek Taparia
 */
public class DeviceData {

    private String os;
    private String cooeeSdkVersion;
    private String appVersion;
    private String osVersion;

    public DeviceData(String os, String cooeeSdkVersion, String appVersion, String osVersion) {
        this.os = os;
        this.cooeeSdkVersion = cooeeSdkVersion;
        this.appVersion = appVersion;
        this.osVersion = osVersion;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getCooeeSdkVersion() {
        return cooeeSdkVersion;
    }

    public void setCooeeSdkVersion(String cooeeSdkVersion) {
        this.cooeeSdkVersion = cooeeSdkVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
}
