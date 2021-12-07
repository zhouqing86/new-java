package com.newjava.function.cache;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static com.newjava.function.cache.FibMemoizer.callMemorized;

public class Fibonacci {

    public static Long fib(Long n) {
        BiFunction<UnaryOperator<Long>, Long, Long> compute = (memoFunc, input) -> {
            if (input <= 1) {
                return input;
            }
            return memoFunc.apply(input-1) + memoFunc.apply(input-2);
        };
        return callMemorized(compute, n);
    }

    public static Long normalFib(Long n) {
        if (n <= 1) {
            return n;
        }
        return normalFib(n-1) + normalFib(n-2);
    }
}
