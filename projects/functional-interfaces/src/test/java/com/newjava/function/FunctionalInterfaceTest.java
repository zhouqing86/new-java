package com.newjava.function;

import com.google.common.base.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FunctionalInterfaceTest {

    @Test
    void testPredicateTestMethod() {
        Predicate<Integer> isEven = num -> 0 == num % 2;
        Predicate<String> isUppercase = str -> str.equals(str.toUpperCase());
        Predicate<String> isEmpty = str -> Objects.isNull(str) || 0 == str.length();
        Predicate<Boolean> negate = b -> !b;

        String str = "abc";
        assertTrue(negate.test(isEmpty.test(str)) && negate.test(isUppercase.test(str)));
        assertTrue(isEmpty.negate().and(isUppercase.negate()).test(str));

        Predicate<String> customPredicate = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.equals(s.toUpperCase());
            }

            @Override
            public Predicate<String> and(Predicate<? super String> other) {
                return t -> other.test(t) && test(t);
            }
        };
        assertFalse(customPredicate.and(Objects::nonNull).test(null));
    }

    private Double calSomething(Supplier<Double> lazyLogic) {
        return lazyLogic.get() * 2;
    }

    @Test
    void testSupplierGetMethod() {
        Supplier<Integer> randInt = () -> new Random().nextInt();
        Supplier<String> newString = String::new;

        Supplier<Double> lazySupplier = () -> {
            try {
                Thread.sleep(1000);
                return 2d;
            } catch (InterruptedException e) {
                return 0d;
            }
        };

        Stream.generate(UUID::randomUUID)
                .limit(10)
                .forEach(item -> {
                    try {
                        Thread.sleep(1000);
                        System.out.println(item);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    void testSupplierFromInstance() {
        String hello = "Hello";
        Supplier<Integer> lenSupplier = hello::length;
        System.out.println(lenSupplier.get());
        hello = "Hello New Java!";
        System.out.println(lenSupplier.get());
        hello = null;
        System.out.println(lenSupplier.get());
    }

    @Test
    void testConsumer() {
        Consumer<Object> writeToFile = str -> {
            try {
                Files.writeString(Path.of("test.txt"), str.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Consumer<Object> consumer = System.out::println;
        consumer.accept(1);
        consumer.accept("hello");

        consumer.andThen(writeToFile).accept("hello");

        List<Integer> list = List.of(1, 3, 5);
        List.of(1, 3, 5).forEach(consumer);
        Stream.of(1, 3, 5).forEach(consumer);

        Stream.of(1, 2, 3, 4, 5)
                .filter(num -> num > 3)
                .filter(num -> num % 2 != 0)
                .forEach(System.out::println);
    }

    @Test
    void testFunctionInterface() {
        Function<Integer, Integer> add = a -> a + 1;
        Function<String, Integer> parseIt = Integer::parseInt;
        System.out.println(add.apply(2));
        System.out.println(parseIt.apply("12"));

        System.out.println(parseIt.andThen(add).apply("12")); //结果为13
        System.out.println(add.compose(parseIt).apply("12")); //结果为13
    }

    @Test
    void testFunctionInterfaceWithStream() {
        Stream.of(1, 2, 3, 4, 5)
                .map(Math::sqrt)
                .map(Double::intValue)
                .forEach(System.out::println);

    }

    @Test
    void testOperators() {
        Integer min = List.of(3, 2, 1, 5, 4)
                .stream()
                .reduce(BinaryOperator.minBy(Comparator.naturalOrder()))
                .get();
        System.out.println(min);

        Integer max = List.of(3, 2, 1, 5, 4)
                .stream()
                .reduce(BinaryOperator.maxBy(Comparator.naturalOrder()))
                .get();
        System.out.println(max);
    }
}
