package com.letscooee.models;

/**
 * This class will help in storing sdkToken received from the server when app is opened first time.
 *
 * @author Abhishek Taparia
 */
public class UserAuthResponse {

    private String sdkToken;
    private String sessionID;
    private String id;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
