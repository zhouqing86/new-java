package com.newjava.junit5;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class BigObjectUser {

    String calPrefix(String rawPrefix) {
        return rawPrefix.trim() + " ";
    }

    String calSuffix(String rawSuffix) {
        return " " + rawSuffix.trim();
    }

    public String process(BinaryOperator<String> calculator, String rawPrefix, String rawSuffix) {
        return calculator.apply(
                calPrefix(rawPrefix),
                calSuffix(rawSuffix)
        );
    }
}
