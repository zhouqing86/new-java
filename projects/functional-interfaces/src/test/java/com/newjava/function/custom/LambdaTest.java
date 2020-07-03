package com.newjava.function.custom;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LambdaTest {
    public static int v1 = 1;

    @Test
    void testLambdaScope() {
        int v2 = 2;
        int finalV = v2;
        UnaryOperator<Integer> addFunc = param -> param + v1 + finalV;
        v1++;
        v2++;
        assertEquals(5, addFunc.apply(1));
    }

    class ValueObject {
        int val;

        ValueObject(int val) {
            this.val = val;
        }
    }

    @Test
    void testLambdaScopeObject() {
        ValueObject v2 = new ValueObject(2);
        UnaryOperator<Integer> addFunc = param -> param + v2.val;
        v2.val++;
        assertEquals(4, addFunc.apply(1));
    }

    @Test
    void testLambdaIntSum() {
        int sum = 0;
//        Consumer<Integer> sumFun = param -> sum += param;
    }

    @Test
    void testLambdaBigIntegerSum() {
        BigInteger sum = BigInteger.valueOf(0);
        Consumer<Integer> sumFun = param -> sum.add(BigInteger.valueOf(param.intValue()));
        sum.add(BigInteger.valueOf(1));
        assertEquals(BigInteger.valueOf(2), sum);
    }

    @Test
    void testLambdaListSum() {
        List<Integer> list = Collections.synchronizedList(new ArrayList<>());
//        List<Integer> list = new ArrayList<>();

        IntConsumer addFun = param -> list.add(param);

        IntStream.range(0, 1000)
                .parallel()
                .forEach(addFun);

        assertEquals(1000, list.size());
    }


    @Test
    void testLambdaTypeInfer() {
        IntBinaryOperator addFun = (a, b) -> a + b;
        BinaryOperator<Integer> addFun2 = (a, b) -> a + b;
        BinaryOperator<String> addFun3 = (a, b) -> a + b;
    }

    private static void method1(IntBinaryOperator fun) {
    }

    private static <T> void method2(BinaryOperator<T> fun) {
    }

    @Test
    void testInvokeMethod1() {
        LambdaTest.method1((a, b) -> a + b);
    }

    @Test
    void testInvokeMethod2() {
        LambdaTest.method2((Integer a, Integer b) -> a + b);
        LambdaTest.method2((String a, String b) -> a + b);
    }

    @Test
    void testExceptionalFunction() {
        Arrays.asList(1, 2, 3, 0, 4, 5, 6).forEach(i -> {
            try {
                System.out.println(50 / i);
            } catch (ArithmeticException e) {
                System.err.println("Arithmetic Exception: " + e.getMessage());
            }
        });
    }

    private static void writeToFile(int i) throws IOException {
        throw new IOException("Mock IO error");
    }


    @FunctionalInterface
    interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }

    private static<T, E extends Exception> Consumer<T> consumerExceptionWrapper(ThrowingConsumer<T, E> consumer, Class<E> exceptionClass) {
        return  i -> {
            try {
                consumer.accept(i);
            } catch (Exception ex) {
                try {
                    E castedEx = exceptionClass.cast(ex);
                    System.out.println("Exception occured: " + castedEx.getMessage());
                    throw new RuntimeException(castedEx);
                } catch (ClassCastException castEx) {
                    throw new RuntimeException(castEx);
                }
            }
        };
    }

    @Test
    void testCheckedException() {
//        Arrays.asList(1, 2, 3, 0, 4, 5, 6).forEach(i -> LambdaTest.writeToFile(i));

//        Consumer<Integer> consumer = i -> writeToFile(i);

//        Arrays.asList(1, 2, 3, 0, 4, 5, 6).forEach(i -> {
//            try {
//                LambdaTest.writeToFile(i);
//            } catch (IOException e) {
//                throw new RuntimeException("IO Exception: " + e.getMessage());
//            }
//        });

        Arrays.asList(1, 2, 3, 0, 4, 5, 6).forEach(LambdaTest.consumerExceptionWrapper(i -> writeToFile(i), IOException.class));
    }
}
