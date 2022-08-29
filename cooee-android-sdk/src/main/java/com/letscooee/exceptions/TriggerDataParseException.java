package com.letscooee.exceptions;

/**
 * Exception to be thrown when a raw trigger data is parsed to fail.
 *
 * @author Shashank Agrawal
 */
public class TriggerDataParseException extends InvalidTriggerDataException {

    public TriggerDataParseException(Throwable cause, String payload) {
        super(cause, payload);
    }

}
