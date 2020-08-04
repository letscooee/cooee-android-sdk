package com.letscooee.models;

/**
 * @author Abhishek Taparia
 * The Layout class will store the data about the layout properties of campaigns received from the server
 */
public class Layout {
    private String type;
    private String background;
    private String direction;
    private String startAs;
    private CloseBehaviour closeBehaviour;
    private Button button;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getStartAs() {
        return startAs;
    }

    public void setStartAs(String startAs) {
        this.startAs = startAs;
    }

    public CloseBehaviour getCloseBehaviour() {
        return closeBehaviour;
    }

    public void setCloseBehaviour(CloseBehaviour closeBehaviour) {
        this.closeBehaviour = closeBehaviour;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

}
