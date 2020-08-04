package com.letscooee.models;

/**
 * @author Abhishek Taparia
 * The CloseBehaviour class will store the data about the closing behaviour of campaigns received from the server
 */
public class CloseBehaviour {
    private String closeButton;
    private int autoCloseTime;

    public String getCloseButton() {
        return closeButton;
    }

    public void setCloseButton(String closeButton) {
        this.closeButton = closeButton;
    }

    public int getAutoCloseTime() {
        return autoCloseTime;
    }

    public void setAutoCloseTime(int autoCloseTime) {
        this.autoCloseTime = autoCloseTime;
    }
}
