package com.letscooee.utils;

/**
 * The CooeeSDKConstants class contains all the constants required by SDK
 *
 * @author Abhishek Taparia
 */
public class CooeeSDKConstants {

    public static final int REQUEST_LOCATION = 1;

    public static final String STORAGE_FIRST_TIME_LAUNCH = "is_first_launch";
    public static final String STORAGE_SDK_TOKEN = "sdk_token";
    public static final String STORAGE_SESSION_NUMBER = "session_number";
    public static final String STORAGE_USER_ID = "user_id";
    public static final String STORAGE_ACTIVE_TRIGGERS = "active_triggers";

    public static final String LOG_PREFIX = "CooeeSDK";
    public static final int IDLE_TIME_IN_MS = 30 * 60 * 1000;
    public static final int KEEP_ALIVE_TIME_IN_MS = 5 * 60 * 1000;

    public static final String NOTIFICATION_CHANNEL_ID = "COOEE_DEFAULT_CHANNEL";
    public static final String NOTIFICATION_CHANNEL_NAME = "DEFAULT";
    public static final String TOUCH_MAP = "TOUCHMAP";

    public static final String INTENT_BUNDLE_KEY = "cooeeIntentBundle";
    public static final String INTENT_TRIGGER_DATA_KEY = "cooeeTriggerData";

    public static final String POST_METHOD = "post";
    public static final String PUT_METHOD = "put";

    public static final String SAVE_USER_PATH = "/v1/user/save";
    public static final String EVENT_PATH = "/v1/event/track";
    public static final String USER_PROFILE_PATH = "/v1/user/update";
    public static final String SESSION_CONCLUDED_PATH = "/v1/session/conclude";
    public static final String KEEP_ALIVE_PATH = "/v1/session/keepAlive";
    public static final String FIREBASE_TOKEN_PATH = "/v1/user/setFirebaseToken";
}
