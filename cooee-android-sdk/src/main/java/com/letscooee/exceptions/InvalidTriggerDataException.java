package com.letscooee.exceptions;

import com.letscooee.models.trigger.TriggerData;

/**
 * Exception to be thrown when a raw trigger data is parsed to fail.
 *
 * @author Shashank Agrawal
 */
public class InvalidTriggerDataException extends Exception {

    String payload;
    String message;
    TriggerData triggerData;

    public InvalidTriggerDataException(String message) {
        super(message);
        this.message = message;
    }

    public InvalidTriggerDataException(String message, TriggerData triggerData) {
        super(message);
        this.message = message;
        this.triggerData = triggerData;
    }

    public InvalidTriggerDataException(Throwable cause, String payload) {
        super(cause);
        this.payload = payload;
    }

    public InvalidTriggerDataException(Throwable cause, TriggerData triggerData) {
        super(cause);
        this.triggerData = triggerData;
    }

    public InvalidTriggerDataException(String message, Throwable cause, TriggerData triggerData) {
        super(message, cause);
        this.message = message;
        this.triggerData = triggerData;
    }
}
