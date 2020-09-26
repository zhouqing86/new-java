package com.newjava.function.optional;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class OptionalTest {
    @Test
    void testOptionalIfPresent() {
        Optional.ofNullable(1).ifPresent(System.out::println);
    }

    @Test
    void testOptionalFlatMap() {
        Optional
                .ofNullable(1)
                .flatMap(value -> Optional.ofNullable(value+1))
                .ifPresent(System.out::println);
    }

    @Test
    void testOptionalFilter() {
        Lists.newArrayList(1, null, 2, null, 3, null, 4)
                .forEach(
                        num -> Optional.ofNullable(num).filter(i -> i%2==0).ifPresent(System.out::println)
                );
    }
}
