package com.letscooee.models;

/**
 * @author Abhishek Taparia
 * The Button class will store the data about the buttons to be added in campaigns received from the server
 */
public class Button {
    private Button text;
    private Button color;
    private Button action;

    public Button getText() {
        return text;
    }

    public void setText(Button text) {
        this.text = text;
    }

    public Button getColor() {
        return color;
    }

    public void setColor(Button color) {
        this.color = color;
    }

    public Button getAction() {
        return action;
    }

    public void setAction(Button action) {
        this.action = action;
    }
}
