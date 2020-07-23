package com.newjava.function.stream;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestStreamTerminateOperation {
    @Test
    void testStreamBasicUsage() {
        Stream.of(1, 2, 3).parallel().reduce(0, Integer::sum);

        List.of(3, 4, 5, 6, 7)
                .stream()
                .map(Math::sqrt)
                .filter(d -> d > 2)
                .mapToInt(Double::intValue)
                .limit(2)
                .reduce(0, Integer::sum);


        List<Integer> list = Lists.newArrayList(1, 2, 3);
        Stream<Integer> stream = list.stream();
        list.add(4);
        stream.forEach(System.out::println);
    }

    class MyIntegerStream {
        Iterable<Integer> iterable;
        Function<Integer, Integer> calFunction = UnaryOperator.identity();
        MyIntegerStream(Iterable<Integer>  iterable) {
            this.iterable = iterable;
        }
        public MyIntegerStream intermediateOperation1(UnaryOperator<Integer> function) {
            calFunction = calFunction.andThen(function);
            return this;
        }

        public MyIntegerStream intermediateOperation2(BinaryOperator<Integer> binaryFunction, Integer secondParam) {
            Function<Integer, Integer> curryFunction = i -> binaryFunction.apply(i, secondParam);
            calFunction = calFunction.andThen(curryFunction);
            return this;
        }

        public List<Integer> terminateOperation() {
            List<Integer> list = Lists.newArrayList();
            Iterator<Integer> iterator = iterable.iterator();
            while(iterator.hasNext()) {
                list.add(calFunction.apply(iterator.next()));
            }
            return list;
        }
    }

    @Test
    void testMyIntegerStream() {
        List<Integer> result = new MyIntegerStream(List.of(1, 3, 5))
                .intermediateOperation1(num -> num * num)
                .intermediateOperation2((num1, num2) -> Math.min(num1, num2), 10)
                .terminateOperation();
        assertEquals(List.of(1, 9, 10), result);
    }

    @Test
    void testCreateStream() {
        Stream<Integer> empty = Stream.empty();
        Stream<Integer> oneElementStream = Stream.of(1);
        Stream<Integer> oneElementOrEmptyStream = Stream.ofNullable(null);
        Stream<Integer> multipleElementsStream = Stream.of(1, 2, 3);

        Stream<Integer> infiniteStream = Stream.iterate(1, num -> num + 2);
        infiniteStream.limit(3).forEach(System.out::println);

        Stream<Integer> boundedStream = Stream.iterate(1, num -> num < 10, num -> num + 2);
        boundedStream.forEach(System.out::println);

        Stream<Double> infiniteRandomStream = Stream.generate(Math::random);
        infiniteRandomStream.limit(10).forEach(System.out::println);

        Stream<Integer> builderStream = Stream.<Integer>builder().add(1).add(2).build();
    }

    @Test
    void testCreateStreamByCollection() {
        Stream<Integer> lstStream = List.of(1, 2, 3).stream();
        Stream<Integer> setStream = Set.of(1, 2, 3).stream();
    }

    @Test
    void testCreateStreamByArrays() {
        Stream<Integer> stream = Arrays.stream(new Integer[]{1, 2, 3});
    }

    @Test
    void testCreateIntStreamByRange() {
        assertArrayEquals(new int[]{1, 2}, IntStream.range(1, 3).toArray());
        assertArrayEquals(new int[]{1, 2, 3}, IntStream.rangeClosed(1, 3).toArray());
    }

    @Test
    void testCreateStreamByStreamSupport() {
        Iterable<Integer> iterable = List.of(1, 2, 3);
        Stream<Integer> stream = StreamSupport.stream(iterable.spliterator(), false);
    }

    @Test
    void testCreateStreamWithStringMethods() {
        assertEquals(2, "Hello\nWorld".lines().count());
        assertEquals(2, "\uD83D\uDE03".chars().count());
        assertEquals(1, "\uD83D\uDE03".codePoints().count());
    }

    @Test
    void testStreamForEach() {
        List.of(1, 2, 3).stream().forEach(System.out::println);
    }

    @Test
    void testDifferentiatePrimitiveTypeAndObject() {
        Stream.of(new int[]{1, 2, 3}).forEach(System.out::println);
        Arrays.stream(new int[]{1, 2, 3}).forEach(System.out::println);
        Stream.of(new Integer[]{1, 2, 3}).forEach(System.out::println);
    }

    @Test
    void testStreamCount() {
        assertEquals(1, Stream.of(new int[]{1, 2, 3}).count());
        assertEquals(3, Stream.of(new Integer[]{1, 2, 3}).count());
    }

    @Test
    void testStreamFindFirst() {
        assertEquals(Optional.empty(), Stream.ofNullable(null).findFirst());
        assertEquals(Optional.of(1), Stream.of(1, 2, 3).findFirst());
    }

    @Test
    void testStreamFindAny() {
        assertEquals(Optional.empty(), Stream.ofNullable(null).findAny());
        assertNotEquals(Optional.empty(), Stream.of(1, 2, 3).findAny());
    }

    @Test
    void testStreamAnyMatch() {
        assertTrue(Stream.of(1, 2, 3).anyMatch(num -> num % 2 == 0));
        assertFalse(Stream.of(1, 2, 3).anyMatch(num -> num > 3));
    }

    @Test
    void testStreamAllMatch() {
        assertFalse(Stream.of(1, 2, 3).allMatch(num -> num % 2 == 0));
        assertTrue(Stream.of(1, 2, 3).allMatch(num -> num < 4));
    }

    @Test
    void testStreamNoneMatch() {
        assertFalse(Stream.of(1, 2, 3).noneMatch(num -> num % 2 == 0));
        assertTrue(Stream.of(1, 2, 3).noneMatch(num -> num > 3));
    }

    @Test
    void testStreamMax() {
        assertEquals(Optional.empty(), Stream.<Integer>ofNullable(null).max(Comparator.naturalOrder()));
        assertEquals(Optional.of(3), Stream.of(2, 3, 1).max(Comparator.naturalOrder()));
    }

    @Test
    void testStreamMin() {
        assertEquals(Optional.empty(), Stream.<Integer>ofNullable(null).min(Comparator.naturalOrder()));
        assertEquals(Optional.of(1), Stream.of(2, 3, 1).min(Comparator.naturalOrder()));
    }

    @Test
    void testStreamToArray() {
        assertArrayEquals(new Object[]{1, 2, 3}, Stream.of(1, 2, 3).toArray());
        assertArrayEquals(new Integer[]{1, 2, 3}, Stream.of(1, 2, 3).toArray(Integer[]::new));
    }

    @Test
    void testStreamReduceWithIdentityAndAccumulator() {
        assertEquals(6, Stream.of(1, 2, 3).reduce(0, (identity, element) -> identity + element));
        assertEquals(0, Stream.<Integer>ofNullable(null).reduce(0, (identity, element) -> identity + element));

        int identity = 0;
        BinaryOperator<Integer> accumulator = (i, element) -> i + element;
        for (Integer i : List.of(1, 2, 3)){
            identity = accumulator.apply(identity, i);
        }
        assertEquals(6, identity);
    }

    @Test
    void testStreamReduceWithAccumulator() {
        assertEquals(Optional.of(6), Stream.of(1, 2, 3).reduce((result, nextElement) -> result + nextElement));
        assertEquals(Optional.empty(), Stream.<Integer>ofNullable(null).reduce((result, nextElement) -> result + nextElement));

        assertEquals(Optional.of(true), Stream.of(false, false, true).reduce((result, nextElement) -> result || nextElement));
    }

    class User {
        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Test
    void testStreamReduceErrorForBinaryOperator() {
        List<User> users = List.of(new User("A", 24), new User("B", 34));

        //Compile error
        //users.stream().reduce(0, (identity, user) -> identity + user.getAge());
        //users.stream().reduce((identity, user) -> identity + user.getAge());
    }

    @Test
    void testStreamReduceWithIdentityAccumulatorCombiner() {
        List<User> users = List.of(new User("A", 24), new User("B", 34), new User("C", 20));
        assertEquals(78, users.stream().parallel().reduce(0, (identity, user) -> identity + user.getAge(), (a, b) -> {
            System.out.println("a=" + a + ", b=" + b);
            return a + b;
        }));
    }

    @Test
    void testStreamCollectWithSupplierAccumulatorCombiner() {
        BiConsumer<List<Integer>, Integer> accumulator = (lst, e) -> lst.add(e);
        BiConsumer<List<Integer>, List<Integer>> combiner = (lst1, lst2) -> lst1.addAll(lst2);
        assertEquals(List.of(1, 2, 3), Stream.of(1, 2, 3).collect(ArrayList::new, accumulator, combiner));
    }

    @Test
    void testStreamCollectWithCollector() {
        assertEquals(List.of(1, 2, 3), Stream.of(1, 2, 3).collect(Collectors.toList()));
        assertEquals(Set.of(1, 2, 3), Stream.of(1, 2, 3).collect(Collectors.toSet()));
    }

    @Test
    void testStreamCollectWithCollectorsJoining() {
        assertEquals("{A,B,C}", Stream.of("A", "B", "C").collect(Collectors.joining(",", "{", "}")));
    }

    @Test
    void testStreamCollectGroupingByUserAge() {
        User user1 = new User("A", 24);
        User user2 = new User("B", 50);
        User user3 = new User("C", 24);
        List<User> users = List.of(user1, user2, user3);

        Map<Integer, List<User>> groupedResult = users.stream().collect(Collectors.groupingBy(User::getAge));

        assertEquals(List.of(user1, user3), groupedResult.get(24));
        assertEquals(List.of(user2), groupedResult.get(50));
        assertNull(groupedResult.get(100));

        Function<User, String> classifier = user -> {
            if (user.getAge() <= 44) {
                return "Young";
            } else if (user.getAge() > 44 && user.getAge() <= 55 ) {
                return "Middle-aged";
            }
            return "Old";
        };
        Map<String, List<User>> groupedResult2 = users.stream().collect(Collectors.groupingBy(classifier));
        assertEquals(List.of(user1, user3), groupedResult2.get("Young"));
        assertEquals(List.of(user2), groupedResult2.get("Middle-aged"));
        assertNull(groupedResult2.get("Old"));

        User user4 = new User("D", 40);
        users = List.of(user1, user2, user3, user4);
        Map<String, Optional<User>> groupedResult3 = users.stream().collect(
                Collectors.groupingBy(
                        classifier,
                        Collectors.reducing(
                            BinaryOperator.maxBy(Comparator.comparing(User::getAge))
                        )
                )
        );
        assertEquals(Optional.of(user4), groupedResult3.get("Young"));

        Map<String, Map<String, List<User>>> groupedResult4 = users.stream().collect(
                Collectors.groupingBy(
                        classifier,
                        Collectors.groupingBy(User::getName)
                )
        );

        Map<String, Integer> groupedResult5 = users.stream().collect(
                Collectors.groupingBy(
                        classifier,
                        Collectors.reducing(0, User::getAge, Integer::sum)
                )
        );
        assertEquals(88, groupedResult5.get("Young"));
        assertEquals(50, groupedResult5.get("Middle-aged"));

        Map<String, Integer> groupedResult6 = users.stream().collect(
                Collectors.groupingBy(
                        classifier,
                        Collectors.reducing(0, user -> 1, (count, user)->count+1)
                )
        );
        assertEquals(3, groupedResult6.get("Young"));
        assertEquals(1, groupedResult6.get("Middle-aged"));
    }
}
