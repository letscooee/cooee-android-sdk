package com.letscooee.models;

import androidx.annotation.RestrictTo;

import com.letscooee.utils.Constants;

import java.util.Map;

/**
 * AuthenticationRequestBody class is used in sending request body for the first time to get sdkToken from server
 *
 * @author Abhishek Taparia
 * @version 0.0.1
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AuthenticationRequestBody {
    private final String appID;
    private final String appSecret;
    private final String uuid;
    private final String sdk = Constants.PLATFORM;
    private final Map<String, Object> props;

    public AuthenticationRequestBody(String appID, String appSecret, String uuid, Map<String, Object> props) {
        this.appID = appID;
        this.appSecret = appSecret;
        this.uuid = uuid;
        this.props = props;
    }

    public String getAppID() {
        return appID;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public Map<String, Object> getDeviceData() {
        return props;
    }

    public String getUuid() {
        return uuid;
    }

    public String getSdk() {
        return sdk;
    }

    public Map<String, Object> getProps() {
        return props;
    }
}

