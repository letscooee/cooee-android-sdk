package com.letscooee.utils;

import java.util.HashMap;

/**
 * @author Abhishek Taparia
 */
public interface InAppNotificationClickListener {

    /**
     * Callback to return a Key Value payload associated with inApp widget click.
     */
    void onInAppButtonClick(HashMap<String, String> payload);
}
