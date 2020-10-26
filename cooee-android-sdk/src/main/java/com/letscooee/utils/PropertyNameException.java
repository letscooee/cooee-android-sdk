package com.letscooee.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Custom Exception to make sure that property name don't start with 'ce '
 *
 * @author Abhishek Taparia
 */
public class PropertyNameException extends Exception {
    public PropertyNameException() {
        super("Property name cannot start with 'CE '");
    }
}
