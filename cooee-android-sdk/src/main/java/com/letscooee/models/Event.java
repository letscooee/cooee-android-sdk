package com.letscooee.models;

import com.letscooee.models.v3.CoreTriggerData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Event class is sent as body to server when a user event needs to be tracked.
 *
 * @author Abhishek Taparia
 */
public class Event {

    private String name;
    private Map<String, Object> properties;
    private String sessionID;
    private int sessionNumber;
    private String screenName;
    private ArrayList<HashMap<String, String>> activeTriggers;
    private Date occurred;

    public Event(String name) {
        this(name, new HashMap<>());
    }

    // TODO: 16/07/21 Remove old constructor
    public Event(String name, TriggerData triggerData) {
        this(name);
        this.withTrigger(triggerData);
    }

    public Event(String name, CoreTriggerData triggerData) {
        this(name);
        this.withTrigger(triggerData);
    }

    public Event(String name, Map<String, Object> properties) {
        this.name = name;
        this.properties = properties;
        this.occurred = new Date();
    }

    // TODO: 16/07/21 remove old method
    public void withTrigger(TriggerData triggerData) {
        properties.put("triggerID", triggerData.getId().trim());
    }

    public void withTrigger(CoreTriggerData triggerData) {
        properties.put("triggerID", triggerData.getId().trim());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
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

    public ArrayList<HashMap<String, String>> getActiveTriggers() {
        return activeTriggers;
    }

    public void setActiveTriggers(ArrayList<HashMap<String, String>> activeTriggers) {
        this.activeTriggers = activeTriggers;
    }

    @Override
    public String toString() {
        return "Event{name=" + name + "}";
    }
}
