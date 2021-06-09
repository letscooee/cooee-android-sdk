package com.letscooee.utils;

/**
 * @author Ashish Gaikwad
 *
 * Will send trigger to Flutter data
 */
public interface OnInAppPopListener {

    /**
     * Callback to send InApp is triggered to flutter plugin.
     * @param blur int value for glassmorphism
     */
    void onInAppTriggered(int blur);

}