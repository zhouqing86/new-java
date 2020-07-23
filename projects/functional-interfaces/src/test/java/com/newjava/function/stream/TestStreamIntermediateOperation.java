package com.newjava.function.stream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStreamIntermediateOperation {
    @Test
    void testMap() {
        List<Integer> lst = Stream.of(1, 2, 3)
                .map(num -> num * num)
                .map(num -> num + 2)
                .collect(Collectors.toList());
        assertEquals(List.of(3, 6, 11), lst);
    }

    @Test
    void testFlatMap() {
        List<String> words = Stream.of("Hello world", "Hello my friend")
                .map(str -> str.split(" "))
                .flatMap(arr -> Arrays.stream(arr))
                .collect(Collectors.toList());
        assertEquals(5, words.size());
    }

    @Test
    void testDistinct() {
        List<Integer> lst = Stream.of(1, 2, 3, 1, 2, 3)
                .distinct()
                .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3), lst);
    }

    @Test
    void testFilter() {
        List<Integer> lst = Stream.of(1, 2, 3)
                .filter(num -> num % 2 == 0)
                .collect(Collectors.toList());
        assertEquals(List.of(2), lst);
    }

    @Test
    void testTakeWhile() {
        List<Integer> lst = Stream.of(1, 2, 3)
                .takeWhile(num -> num % 2 != 0)
                .collect(Collectors.toList());
        assertEquals(List.of(1), lst);
    }

    @Test
    void testDropWhile() {
        List<Integer> lst = Stream.of(1, 2, 3)
                .dropWhile(num -> num % 2 != 0)
                .collect(Collectors.toList());

        //从Stream的第一个元素开始，如果满足dropWhile的条件，则从结果Stream中删除，直到有不满足条件的元素出现退出while循环
        assertEquals(List.of(2, 3), lst);
    }

    @Test
    void testLimit() {
        List<Integer> lst = Stream.iterate(1, num -> num + 1)
                .limit(3)
                .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3), lst);
    }

    @Test
    void testSkip() {
        int page = 2;
        int pageSize = 3;
        List<Integer> lst = Stream.iterate(1, num -> num + 1)
                .skip((page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        assertEquals(List.of(4, 5, 6), lst);
    }

    @Test
    void testSorted() {
        List<Integer> lst = Stream.of(3, 1, 2)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of(1, 2, 3), lst);
    }

    @Test
    void testSortedWithComparator() {
        List<Integer> lst = Stream.of(3, 1, 2)
                .sorted(Comparator.<Integer>naturalOrder().reversed())
                .collect(Collectors.toList());
        assertEquals(List.of(3, 2, 1), lst);
    }

    @Test
    void testPeek() {
        List<Integer> lst = Stream.of(1, 2, 3)
                .peek(System.out::print)
                .map(num -> num * num)
                .peek(System.out::print)
                .collect(Collectors.toList());
        assertEquals(List.of(1, 4, 9), lst);
    }

    @Test
    void testPeekWhenFilter() {
        Stream.of(1, 2, 3)
                .map(num -> num * num)
                .peek(System.out::println)
                .filter(num -> num % 2 == 0)
                .collect(Collectors.toList());
    }

    @Test
    void testLazyEvaluation() {
        Optional<Integer> first = Stream.of(1, 2, 3, 4, 5)
                .map(num -> num * num)
                .peek(System.out::print)
                .filter(num -> num % 2 == 0)
                .findFirst();
        assertEquals(Optional.of(4), first);
    }

    @Test
    void testForIteration() {
        List<Integer> lst = List.of(1, 2, 3, 4, 5);
        for (Integer num : lst) {
            int temp = num * num;
            System.out.print(temp);
            if (temp % 2 == 0) {
                break;
            }
        }
    }

    @Test
    void testIntStream() {
        IntStream intStream = Stream.of(1, 2, 3).mapToInt(num -> num);
        Stream<Integer> stream = IntStream.range(1, 4).mapToObj(num -> num);
        Stream<Integer> stream2 = IntStream.range(1, 4).boxed();
    }
}
