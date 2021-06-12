package com.letscooee.utils;

/**
 * Custom Exception to make sure that property name don't start with 'ce '
 *
 * @author Abhishek Taparia
 */
public class PropertyNameException extends RuntimeException {

    public PropertyNameException() {
        super("Property name cannot start with 'CE '");
    }

}
