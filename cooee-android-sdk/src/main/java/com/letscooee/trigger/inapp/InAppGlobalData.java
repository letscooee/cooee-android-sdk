package com.letscooee.trigger.inapp;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple data holder class shared across different renderers.
 *
 * @author Shashank Agrawal
 * @since 1.0.0
 */
public class InAppGlobalData {

    private final Map<String, Object> closedEventProps = new HashMap<>();

    public Map<String, Object> getClosedEventProps() {
        return closedEventProps;
    }
}
