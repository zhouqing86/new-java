package com.newjava.function;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TestImperativeProgramming {
    @Test
    void testListIterationWithOldWay() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        for (Integer i : list) {
            System.out.println(i);
        }

        int sum = 0;
        for (Integer i : list) {
            sum += i;
        }
    }

    @Test
    void testListIterationWithNewWay() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        list.forEach(System.out::println);
        list.stream().reduce(0, Integer::sum);
    }
}
