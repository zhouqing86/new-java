package com.newjava.function.iterable;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TestIterable {
    @Test
    void testFunctionalIterable() {
        Iterable<Integer> iterable = () -> List.of(1, 2, 3).iterator();
        iterable.forEach(System.out::println);

        Consumer<Integer> anyIntegerConsumer = System.out::println;
        iterable.forEach(anyIntegerConsumer);
    }

    @Test
    void testListSetDequeue() {
        List<Integer> list = List.of(1, 2, 3);
        list.forEach(System.out::println);

        Set<Integer> set = Set.of(4, 5, 6);
        set.forEach(System.out::println);
        Queue<Integer> queue = Lists.newLinkedList(List.of(7, 8, 9));

        queue.forEach(System.out::println);
    }

    @Test
    void testTraditionalIteratorOverList() {
        Iterator<Integer> iterator = List.of(1, 2, 3).iterator();
        while(iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    @Test
    void testIteratorOverList() {
        List.of(1, 2, 3).iterator().forEachRemaining(System.out::println);
    }

    class RangeIterator implements Iterator {
        private int low;
        private int high;

        RangeIterator(int low, int high) {
            this.low = low;
            this.high = high;
        }

        @Override
        public boolean hasNext() {
            return low <= high;
        }

        @Override
        public Object next() {
            return low++;
        }
    }

    @Test
    void testRangeIterator() {
        new RangeIterator(1, 10).forEachRemaining(System.out::println);
    }


    class IntArrayIterator implements PrimitiveIterator.OfInt {

        private int []arr;
        private int index;

        IntArrayIterator(int ...a) {
            index = 0;
            arr = Arrays.copyOf(a, a.length);
        }

        @Override
        public int nextInt() {
            return arr[index++];
        }

        @Override
        public boolean hasNext() {
            return index < arr.length;
        }
    }

    @Test
    void testIntArrayIterator() {
        new IntArrayIterator(1, 2, 3).forEachRemaining((int num) -> System.out.println(num));
    }
}
