package com.letscooee.utils;

/**
 * The CooeeSDKConstants class contains all the constants required by SDK
 *
 * @author Abhishek Taparia
 * @version 0.0.2
 */
public class Constants {

    private Constants() {
    }

    public static final int PERMISSION_REQUEST_CODE = 1;

    // region All Shared Preference related keys
    public static final String STORAGE_FIRST_TIME_LAUNCH = "is_first_launch";
    public static final String STORAGE_SDK_TOKEN = "sdk_token";
    public static final String STORAGE_SESSION_NUMBER = "session_number";
    public static final String STORAGE_USER_ID = "user_id";
    public static final String STORAGE_ACTIVE_TRIGGERS = "active_triggers";
    public static final String STORAGE_ACTIVATED_TRIGGERS = "activated_triggers";
    public static final String STORAGE_ACTIVE_TRIGGER = "active_trigger";
    public static final String STORAGE_LAST_TOKEN_ATTEMPT = "last_token_check_attempt";
    public static final String STORAGE_LAST_FONT_ATTEMPT = "last_font_check_attempt";
    public static final String STORAGE_CACHED_FONTS = "cached_fonts";
    public static final String STORAGE_DEVICE_ID = "cooee_device_id";
    public static final String STORAGE_DEVICE_UUID = "cooee_device_uuid";
    public static final String STORAGE_SCREENSHOT_SYNC_TIME = "screenshot_sync_time";
    public static final String STORAGE_LAST_SESSION_USE_TIME = "cooee_last_session_use_time";
    public static final String STORAGE_ACTIVE_SESSION = "cooee_active_session";
    public static final String STORAGE_RAW_IN_APP_TRIGGER_KEY = "cooee_raw_in_app_trigger";
    // endregion

    public static final String TAG = "CooeeSDK";
    public static final int IDLE_TIME_IN_SECONDS = 30 * 60;
    public static final int KEEP_ALIVE_TIME_IN_MS = 5 * 60 * 1000;
    public static final int SCREENSHOT_SEND_INTERVAL_HOURS = 6;

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

    // region Intent extras key
    public static final String INTENT_BUNDLE_KEY = "cooeeIntentBundle";
    public static final String INTENT_TRIGGER_DATA_KEY = "cooeeTriggerData";
    public static final String INTENT_SDK_VERSION_CODE_KEY = "cooeeDdkVersionCode";
    // endregion

    public static final int PENDING_TASK_JOB_ID = 2663;
    public static final String IN_APP_FULLSCREEN_FLAG_KEY = "make_in_app_fullscreen";

    // region Units
    public static final String UNIT_PIXEL = "px";
    // endregion

    public static final int FONT_REFRESH_INTERVAL_DAYS = 7;
    public static final String FONTS_DIRECTORY = "cooeeFonts";
    public static final String AR_PROCESS_NAME = "CooeeARProcess";
    public static final int INCREMENT_PASSWORD = 10;
    public static final String PLATFORM = "ANDROID";
    public static final String PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=";
    public static final int DEFAULT_CONTAINER_WIDTH = 1080;
    public static final int DEFAULT_CONTAINER_HEIGHT = 1920;
    public static final int JUSTIFY_TEXT_ALIGNMENT = -1;

    // region AR
    public static final String AR_INTENT = "com.letscooee.launchCooeeAR";
    public static final String AR_BROADCAST_CLASS = "com.unity3d.player.broadcast.CooeeLaunchAR";
    public static final String AR_INTENT_TYPE = "intentType";
    public static final String AR_LAUNCH_INTENT = "CooeeARLaunch";
    public static final String AR_DATA = "arData";
    public static final String AR_PACKAGE_NAME = "appPackage";
    // endregion

    // region Date Formats
    public static final String DATE_FORMAT_DEBUG = "dd-MMM-yyyy hh:mm a";
    public static final String DATE_FORMAT_UTC = "EEE MMM dd HH:mm:ss zzz yyyy";
    // endregion
}
