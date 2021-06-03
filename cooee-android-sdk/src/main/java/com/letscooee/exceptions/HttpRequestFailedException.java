package com.letscooee.exceptions;

/**
 * A generic exception to be thrown when any HTTP request fails in a synchronous execution.
 *
 * @author Shashank Agrawal
 * @since 0.3.0
 */
public class HttpRequestFailedException extends Exception {

    Integer responseCode;
    Object responseBody;

    public HttpRequestFailedException(String message, Integer responseCode, Object responseBody) {
        super(message);
        this.responseCode = responseCode;
        this.responseBody = responseBody;
    }

    public HttpRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpRequestFailedException(String message, Throwable cause, Integer responseCode) {
        super(message, cause);
        this.responseCode = responseCode;
    }
}
