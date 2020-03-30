package com.newjava.tdd;

public class ClassWithStaticField {
    public static Integer count = 1;

    public static Integer increment() {
       return ++count;
    }

    public static Integer decrement() {
        return --count;
    }
}
