package com.letscooee.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValueUtilTest {
    String input = "2px";
    String px = "px";
    String percent = "%";

    /**
     * valid inputs and valid unit
     */
    @Test
    public void parseToInt() {


        int output = ValueUtil.parseToInt(input, px);
        assertEquals(2, output);
    }

    /**
     * valid input invalid unit
     */
    @Test
    public void parseToIntInvalidUnit() {

        int output = ValueUtil.parseToInt(input, percent);
        assertEquals(0, output);
    }

    /**
     * valid input invalid unit
     */
    @Test
    public void parseToIntInvalidInput() {

        int output = ValueUtil.parseToInt("input", px);
        assertEquals(0, output);
    }

    /**
     * Method: ValueUtil.getCalculatedPixel
     * Inputs: valid input
     */
    @Test
    public void getCalculatedValue() {
        int output = ValueUtil.getCalculatedPixel(input);
        assertEquals(2, output);
    }

    /**
     * ValueUtil.getCalculatedPixel invalid input
     */
    @Test
    public void getCalculatedValueInvalid() {
        int output = ValueUtil.getCalculatedPixel("input");
        assertEquals(0, output);
    }

    @Test
    public void testGetCalculatedValue() {
    }

    @Test
    public void testGetCalculatedValue1() {
    }
}