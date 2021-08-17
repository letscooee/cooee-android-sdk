package com.letscooee.utils;

import java.util.HashMap;

/**
 * @author Abhishek Taparia
 */
public interface CooeeCTAListener {

    /**
     * Callback to return a Key Value payload associated with inApp widget click.
     */
    void onResponse(HashMap<String, Object> payload);
}
