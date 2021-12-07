package com.newjava.function.cache;

import org.junit.jupiter.api.Test;

class FibonacciTest {
    @Test
    void testFib() {
        long l = System.currentTimeMillis();
        Fibonacci.fib(100L);
        System.out.println(System.currentTimeMillis() - l);

        l = System.currentTimeMillis();
        Fibonacci.normalFib(40L);
        System.out.println(System.currentTimeMillis() - l);
    }
}