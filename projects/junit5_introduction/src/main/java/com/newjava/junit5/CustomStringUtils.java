package com.newjava.junit5;

import java.util.Objects;

public class CustomStringUtils {

    public static String beautifyString(final String str) {
        Objects.requireNonNull(str);
        return str.replaceAll("\\s+", " ").trim();
    }

    public static String capitalAndLowercaseRest(final String str) {
        Objects.requireNonNull(str);
        String trimmedStr = str.trim();
        return trimmedStr.length() <  2
                ? trimmedStr.toUpperCase()
                : trimmedStr.substring(0, 1).toUpperCase() + trimmedStr.substring(1).toLowerCase();
    }

    public static void main(String[] args) {
        // test beautifyString
        System.out.println(beautifyString(" hello  world,  new java!  "));

        // test capitalAndLowercaseRest
        System.out.println(capitalAndLowercaseRest("  hELLO  "));
    }
}
