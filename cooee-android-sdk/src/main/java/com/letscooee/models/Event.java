package com.letscooee.models;

import java.util.Map;

/**
 * Event class is sent as body to server when a user event needs to be tracked.
 *
 * @author Abhishek Taparia
 */
public class Event {

    private String name;
    private Map<String, String> properties;
    private String sessionID;
    private int sessionNumber;
    private String screenName;

    public Event(String name, Map<String, String> properties, String sessionID, int sessionNumber, String screenName) {
        this.name = name;
        this.properties = properties;
        this.sessionID = sessionID;
        this.sessionNumber = sessionNumber;
        this.screenName = screenName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }
}
