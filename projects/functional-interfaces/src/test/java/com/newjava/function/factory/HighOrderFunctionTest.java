package com.newjava.function.factory;

import org.checkerframework.checker.nullness.Opt;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HighOrderFunctionTest {
    Function<Integer, Integer> squareFunctionFactory(Integer base) {
        return new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer i) {
                return base + i * i;
            }
        };
    }

    @Test
    void testSquareFuncWithBase() {
        Function<Integer, Integer> squareFuncWithBase1 = squareFunctionFactory(1);
        Function<Integer, Integer> squareFuncWithBase2 = squareFunctionFactory(2);

        assertEquals(5, squareFuncWithBase1.apply(2));
        assertEquals(6, squareFuncWithBase2.apply(2));
    }

    @Test
    void testIdentity() {
        Function<Integer, Integer> integerIdentityFunction = Function.identity();
        assertEquals(10, integerIdentityFunction.apply(10));

        Function<String, String> stringIdentityFunction = Function.identity();
        assertEquals("Hello", stringIdentityFunction.apply("Hello"));
    }


    <T, R> R loggingFunction(T t, Function<T, R> function) {
        R r = function.apply(t);
        System.out.println(String.format("Input is %s, result is %s", t, r));
        return r;
    }

    @Test
    void testLoggingFunction() {
        loggingFunction(1, t -> t + 1);
        loggingFunction(2, t -> t * t);
    }

    <T, K> List<T> mergeTwoList(
            List<T> list1,
            List<T> list2,
            Function<T, K> keyExtractorFunction,
            BiFunction<Optional<T>, Optional<T>, T> mergeFunction
    ) {
        Map<K, T> map1 = list1.stream()
                .collect(Collectors.toMap(keyExtractorFunction, Function.identity()));
        Map<K, T> map2 = list2.stream()
                .collect(Collectors.toMap(keyExtractorFunction, Function.identity()));
        Set<K> keys = Stream.concat(list1.stream(), list2.stream())
                .map(keyExtractorFunction)
                .collect(Collectors.toSet());
        return keys.stream()
                .sorted()
                .map(key -> mergeFunction.apply(Optional.ofNullable(map1.get(key)), Optional.ofNullable(map2.get(key))))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    static class PhoneSale {
        String phone;
        Integer sales;

        public PhoneSale(String phone, Integer sales) {
            this.phone = phone;
            this.sales = sales;
        }

        public Integer getSales() {
            return sales;
        }

        public String getPhone() {
            return phone;
        }

        public static PhoneSale merge(Optional<PhoneSale> sale1Opt, Optional<PhoneSale> sale2Opt) {
            String phone = Stream.of(sale1Opt, sale2Opt)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .map(PhoneSale::getPhone)
                    .orElse(null);
            Integer sales = Stream.of(sale1Opt, sale2Opt)
                    .filter((Optional::isPresent))
                    .map(Optional::get)
                    .map(PhoneSale::getSales)
                    .reduce(0, Integer::sum);
            return new PhoneSale(phone, sales);
        }
    }

    @Test
    void compareAddTwoListMap() {
        List<PhoneSale> seller1Sales = List.of(new PhoneSale("Iphone", 10));
        List<PhoneSale> seller2Sales = List.of(new PhoneSale("Iphone", 5), new PhoneSale("Xiaomi", 20));

        List<PhoneSale> totalSales = mergeTwoList(seller1Sales, seller2Sales, PhoneSale::getPhone, PhoneSale::merge);

        assertEquals(2, totalSales.size());
        assertEquals("Iphone", totalSales.get(0).phone);
        assertEquals(15, totalSales.get(0).sales);
        assertEquals("Xiaomi", totalSales.get(1).phone);
        assertEquals(20, totalSales.get(1).sales);
    }

    @Test
    void testComparing() {
        Comparator<PhoneSale> comparing = Comparator.comparing(PhoneSale::getPhone).thenComparing(PhoneSale::getSales);
        assertEquals(Integer.valueOf(13).compareTo(16),
                comparing.compare(new PhoneSale("Iphone", 13), new PhoneSale("Iphone",16)));
        assertEquals("Iphone".compareTo("Xiaomi"),
                comparing.compare(new PhoneSale("Iphone", 13), new PhoneSale("Xiaomi",13)));
    }
}
