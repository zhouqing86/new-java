package com.newjava.function.stream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestStreamParallel {

    @Test
    void testParallel() {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "2");
        BiConsumer<List<Integer>, Integer> accumulator = (lst, e) -> lst.add(e);
        BiConsumer<List<Integer>, List<Integer>> combiner = (lst1, lst2) -> {
            System.out.println("Combiner: " + Thread.currentThread().getName() + ", lst1=" + lst1 + ", lst2=" + lst2);
            lst1.addAll(lst2);
        };
        List<Integer> lst = Stream.of(1, 2, 3, 4, 5)
                .parallel()
                .map(num -> num * num)
                .peek(num -> {
                    System.out.println(Thread.currentThread().getName() + ": " + num);
                })
                .collect(ArrayList::new, accumulator, combiner);
        assertEquals(List.of(1, 4, 9, 16, 25), lst);
    }
}
