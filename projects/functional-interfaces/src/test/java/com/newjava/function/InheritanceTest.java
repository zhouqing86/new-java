package com.newjava.function;

import org.junit.jupiter.api.Test;

public class InheritanceTest {

    class A {
        public Double cal(int a, int b) {
            return Double.valueOf(a + b);
        }
    }

    class B extends A {
        public Double cal(int a, int b) {
            return Double.valueOf(a + b);
        }
    }

    abstract class C {
        public void print() {
            System.out.println("print======");
        }
    }

    @Test
    void testOverridingWithDifferentReturnType() {

    }
}
