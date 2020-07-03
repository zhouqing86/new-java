package com.newjava.function.custom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WithoutFunctionalInterfaceTest {

    @Test
    void testAddMethod() {
        WithoutFunctionalInterface<Integer> integerAdd = (a, b) -> a + b;
        assertEquals(3, integerAdd.add(1, 2));

        WithoutFunctionalInterface<String> stringAdd = (str1, str2) -> str1.concat(str2);
        assertEquals("helloworld", stringAdd.add("hello", "world"));
    }

}