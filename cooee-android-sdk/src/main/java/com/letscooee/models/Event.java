package com.letscooee.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.models.trigger.EmbeddedTrigger;
import com.letscooee.models.trigger.TriggerData;

import org.bson.types.ObjectId;
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

    @SuppressWarnings({"unused", "FieldCanBeLocal", "FieldMayBeFinal"})
    private String id;
    private String name;
    private Map<String, Object> properties;
    private String sessionID;
    private int sessionNumber;
    private String screenName;
    private ArrayList<EmbeddedTrigger> activeTriggers;
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "unused"})
    private Date occurred;
    private Map<String, Object> deviceProps;

    @SerializedName("trigger")
    private EmbeddedTrigger activeTrigger;

    public Event(String name) {
        this(name, new HashMap<>());
    }

    public Event(String name, TriggerData triggerData) {
        this(name);
        this.withTrigger(triggerData);
    }

    public Event(String name, Map<String, Object> properties) {
        this.id = new ObjectId().toHexString();
        this.name = name;
        this.properties = properties;
        this.occurred = new Date();
    }

    public void withTrigger(TriggerData triggerData) {
        this.activeTrigger = new EmbeddedTrigger(triggerData);
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

    @SuppressWarnings("unused")
    public ArrayList<EmbeddedTrigger> getActiveTriggers() {
        return activeTriggers;
    }

    public EmbeddedTrigger getActiveTrigger() {
        return activeTrigger;
    }

    public void setActiveTriggers(ArrayList<EmbeddedTrigger> activeTriggers) {
        this.activeTriggers = activeTriggers;
    }

    public Map<String, Object> getDeviceProps() {
        return deviceProps;
    }

    public void setDeviceProps(Map<String, Object> deviceProps) {
        this.deviceProps = deviceProps;
    }

    public void setActiveTrigger(EmbeddedTrigger activeTrigger) {
        if (this.activeTrigger != null) {
            return;
        }

        this.activeTrigger = activeTrigger;
    }

    @Override
    public String toString() {
        return "Event{name=" + name + "}";
    }

}
