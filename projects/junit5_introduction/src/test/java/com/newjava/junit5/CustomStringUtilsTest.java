package com.newjava.junit5;

import org.junit.jupiter.api.Test;

import static com.newjava.junit5.CustomStringUtils.beautifyString;
import static com.newjava.junit5.CustomStringUtils.capitalAndLowercaseRest;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomStringUtilsTest {

    private void customAssertEquals(String expected, String actual) {
        if (actual.equals(expected)) {
            return;
        }
        throw  new RuntimeException("expected: " + expected + ", Actual: " + actual);
    }

    private void println(String str) {
        System.out.println(str);
    }

    @Test
    void testBeautifyString() {
        println(beautifyString(" hello  world,  new java!  "));
        assertEquals("hello world, new java!", beautifyString(" hello  world,  new java!  "));
    }

    @Test
    void testCapitalAndLowercaseRest() {
        println(capitalAndLowercaseRest("  hELLO  "));
        assertEquals("Hello", capitalAndLowercaseRest("  hELLO  "));
    }
}