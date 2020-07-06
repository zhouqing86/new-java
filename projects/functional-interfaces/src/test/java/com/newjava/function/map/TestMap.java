package com.newjava.function.map;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMap {

    @Test
    void testMapForEach() {
        Map<String, String> map = Map.of("key1", "value1", "key2", "value2");
        map.forEach((k, v) -> System.out.println(k+"="+v));
    }

    @Test
    void testMapKeyValueSetIteration() {
        Map<String, String> map = Map.of("key1", "value1", "key2", "value2");
        map.entrySet().forEach(System.out::println);
        map.keySet().forEach(System.out::println);
        map.values().forEach(System.out::println);
    }

    @Test
    void testMapReplaceAll() {
        Map<String, Integer> map = new HashMap<>(Map.of("Key1", 1, "Key2", 2));
        map.replaceAll((k,v) -> v * v);

        assertEquals(1, map.get("Key1"));
        assertEquals(4, map.get("Key2"));
    }

    @Test
    void testMapComputeIfAbsent() {
        Map<String, List<String>> scoreStudentsMap = Maps.newHashMap();
        scoreStudentsMap.computeIfAbsent("A", k -> new LinkedList<>()).add("Student1");
        scoreStudentsMap.computeIfAbsent("B", k -> new LinkedList<>()).add("Student2");
        scoreStudentsMap.computeIfAbsent("A", k -> new LinkedList<>()).add("Student3");
        assertEquals(List.of("Student1", "Student3"), scoreStudentsMap.get("A"));
    }

    @Test
    void testCountCharacterNumber() {
        Map<Character, Integer> wordCountMap = Maps.newHashMap();
        "hello world"
                .chars()
                .mapToObj(e -> (char)e)
                .collect(Collectors.toList())
                .forEach(c -> {
                    wordCountMap.computeIfAbsent(c, k -> 0);
                    wordCountMap.computeIfPresent(c, (k, v) -> v + 1);
                });
        assertEquals(3, wordCountMap.get('l'));
        assertEquals(2, wordCountMap.get('o'));
    }

    @Test
    void testCountCharacterNumberWithCompute() {
        Map<Character, Integer> wordCountMap = Maps.newHashMap();
        "hello world"
                .chars()
                .mapToObj(e -> (char)e)
                .collect(Collectors.toList())
                .forEach(c -> {
                    wordCountMap.compute(c, (k,oldValue) -> Objects.isNull(oldValue) ? 1 : oldValue + 1);
                });
        assertEquals(3, wordCountMap.get('l'));
        assertEquals(2, wordCountMap.get('o'));
    }


    @Test
    void testCountCharacterNumberWithMerge() {
        Map<Character, Integer> wordCountMap = Maps.newHashMap();
        "hello world"
                .chars()
                .mapToObj(e -> (char)e)
                .collect(Collectors.toList())
                .forEach(c -> {
                    wordCountMap.merge(c, 1, (oldValue, value) -> oldValue + 1);
                });
        assertEquals(3, wordCountMap.get('l'));
        assertEquals(2, wordCountMap.get('o'));
    }

}
