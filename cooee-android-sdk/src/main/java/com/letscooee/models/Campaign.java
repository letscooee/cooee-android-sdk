package com.letscooee.models;

import com.google.gson.annotations.SerializedName;

/**
 * @author Abhishek Taparia
 * The Campaign class will store the data about the campaigns received from the server
 */
public class Campaign {
    private String type;
    private String name;
    private String startData;
    private String endDate;
    private String eventName;
    private int delay;
    private TriggerData triggerData;
    private Content content;

    @SerializedName("notify_by_push")
    private String notifyByPush;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartData() {
        return startData;
    }

    public void setStartData(String startData) {
        this.startData = startData;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public TriggerData getTriggerData() {
        return triggerData;
    }

    public void setTriggerData(TriggerData triggerData) {
        this.triggerData = triggerData;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public String getNotifyByPush() {
        return notifyByPush;
    }

    public void setNotifyByPush(String notifyByPush) {
        this.notifyByPush = notifyByPush;
    }
}

