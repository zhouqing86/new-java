package com.newjava.junit5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.function.BinaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class BigObjectUserTest {

    private BigObjectUser bigObjectUser;

    @BeforeEach
    void setUp() {
        bigObjectUser = new BigObjectUser();
    }

    @Test
    void testProcessPre() {
        HashMap<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        BigObject obj = BigObject.Builder.create(map);

        assertEquals("prefix value1, value2, value3 suffix",
                bigObjectUser.process(obj::calculate, "prefix", "suffix"));
    }

    @Test
    void testProcess() {
        BinaryOperator<String> calculator = (prefix, suffix) -> {
            return prefix + " calculated " + suffix;
        };
        assertEquals("prefix calculated suffix", bigObjectUser.process(calculator, "prefix", "suffix"));
    }
}