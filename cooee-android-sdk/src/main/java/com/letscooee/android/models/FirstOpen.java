package com.letscooee.android.models;

/**
 * The FirstOpen class will help in storing sdkToken received from the server when app is opened first time.
 */

public class FirstOpen {
    private String sdkToken;

    public String getSdkToken() {
        return sdkToken;
    }

    public void setSdkToken(String sdkToken) {
        this.sdkToken = sdkToken;
    }


}
