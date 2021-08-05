package com.letscooee.utils;

/**
 * The CooeeSDKConstants class contains all the constants required by SDK
 *
 * @author Abhishek Taparia
 * @version 0.0.2
 */
public class Constants {

    public static final int PERMISSION_REQUEST_CODE = 1;

    // region All Shared Preference related keys
    public static final String STORAGE_FIRST_TIME_LAUNCH = "is_first_launch";
    public static final String STORAGE_SDK_TOKEN = "sdk_token";
    public static final String STORAGE_SESSION_NUMBER = "session_number";
    public static final String STORAGE_USER_ID = "user_id";
    public static final String STORAGE_ACTIVE_TRIGGERS = "active_triggers";
    public static final String STORAGE_LAST_TOKEN_ATTEMPT = "last_token_check_attempt";
    public static final String STORAGE_LAST_FONT_ATTEMPT = "last_font_check_attempt";
    public static final String STORAGE_CACHED_FONTS = "cached_fonts";
    // endregion

    public static final String TAG = "CooeeSDK";
    public static final int IDLE_TIME_IN_SECONDS = 30 * 60;
    public static final int KEEP_ALIVE_TIME_IN_MS = 5 * 60 * 1000;

    // region Push notification channel keys
    public static final String DEFAULT_CHANNEL_ID = "COOEE_DEFAULT_CHANNEL";
    public static final String DEFAULT_CHANNEL_NAME = "Default";
    public static final String HIGH_CHANNEL_ID = "COOEE_HIGH_CHANNEL";
    public static final String HIGH_CHANNEL_NAME = "Important";
    // endregion

    // region Push notification
    public static final String ACTION_PUSH_BUTTON_CLICK = "pnButtonClick";
    public static final String ACTION_DELETE_NOTIFICATION = "pnDelete";
    // endregion

    public static final String INTENT_BUNDLE_KEY = "cooeeIntentBundle";
    public static final String INTENT_TRIGGER_DATA_KEY = "cooeeTriggerData";

    public static final int PENDING_TASK_JOB_ID = 2663;

    public static final String UNIT_PIXEL = "px";
    public static final String UNIT_PERCENT = "%";
    public static final String UNIT_VIEWPORT_HEIGHT = "vh";
    public static final String UNIT_VIEWPORT_WIDTH = "vw";
    public static final int INTERVAL_DAYS = 7;
    public static final String DIRECTORY_NAME = "Cooee";
}
