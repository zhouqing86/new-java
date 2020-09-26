package com.newjava.function;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImmutableTest {
    @Test
    void testImmutableString() {
        String s = new String("Hello");
        s.concat("world");
        assertEquals("Hello", s);

        s.replace("H", "h");
        assertEquals("Hello", s);
    }

    @Test
    void testClosure() {
        final int increment = 1;
        UnaryOperator<Integer> add = num -> num + increment;
        assertEquals(2, add.apply(1));
    }

    @Test
    void testHighOrderFunction() {
        Function<Integer, UnaryOperator<Integer>> addN = n -> num -> num + n;
        UnaryOperator<Integer> add1 = addN.apply(1);
        assertEquals(2, add1.apply(1));

        UnaryOperator<Integer> add2 = addN.apply(2);
        assertEquals(3, add2.apply(1));
    }

    private static <T,R> Function<T, R> logRecordWrapper(Function<T, R> wrappedFunction) {
        System.out.print("Function: " + wrappedFunction + " will be called!");
        return wrappedFunction;
    }

    @Test
    void testHighOrderFunctionLogWrapper() {
        UnaryOperator<Integer> add1 = num -> num + 1;
        ImmutableTest.logRecordWrapper(add1).apply(1);
    }

    private static <T,U,R> Function<T, Function<U, R>> curring(BiFunction<T,U,R> biFunction) {
        return t -> u -> biFunction.apply(t, u);
    }

    @Test
    void testCurring() {
        BinaryOperator<Integer> add = (a, b) -> a + b;
        assertEquals(3, curring(add).apply(1).apply(2));
    }

    @Test
    void testCurringWithMap() {
        BinaryOperator<Integer> add = (a, b) -> a + b;
        List<Integer> lst = Stream.of(1, 2, 3)
                .map(num -> add.apply(num, 1))
                .map(num -> num * num)
                .map(num -> add.apply(num, 2))
                .collect(Collectors.toList());
        assertEquals(List.of(6, 11, 18), lst);

        Function<Integer, Function<Integer, Integer>> curring = curring(add);
        lst = Stream.of(1, 2, 3)
                .map(curring.apply(1))
                .map(num -> num * num)
                .map(curring.apply(2))
                .collect(Collectors.toList());
        assertEquals(List.of(6, 11, 18), lst);
    }
}
