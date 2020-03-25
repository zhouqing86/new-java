package com.newjava.junit5;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class BigObjectTest {

    @Test
    void testBuildBigObjectAndCalculate() {
        HashMap<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        BigObject obj = BigObject.Builder.create(map);
        assertEquals("value1, value2, value3", obj.calculate("", ""));
    }
}