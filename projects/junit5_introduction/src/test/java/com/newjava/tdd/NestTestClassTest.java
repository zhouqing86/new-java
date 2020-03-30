package com.newjava.tdd;

import org.junit.jupiter.api.*;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NestTestClassTest {
    
    @BeforeEach
    void setUp() {
        System.out.println("External setup");
    }

    @Nested
    class NestTestClass {
        @BeforeEach
        void setUp() {
            System.out.println("Nested setup ");
        }

        @Test
        void testMethod() {
            System.out.println("test method");
        }
    }

    @Nested
    class TestClassStaticMember {

        private Integer count;

        @BeforeEach
        void setUp() {
            count = ClassWithStaticField.count;
        }

        @AfterEach
        void tearDown() {
            ClassWithStaticField.count = count;
        }

        @Test
        void testDecrement() {
            assertEquals(0, ClassWithStaticField.decrement());
        }

        @Test
        void testIncrement() {
            assertEquals(2, ClassWithStaticField.increment());
        }
    }

    @Nested
    class EnvironmentVariableRelatedTest {

        @Test
        void testEnv1() {
            String env = System.getProperty("env");
            System.setProperty("env", "PROD");
            System.out.println("Do testEnv1 with env: " + System.getProperty("env"));
            System.setProperty("env", Objects.isNull(env) ? "" : env);
        }

        @Test
        void testEnv2() {
            String env = System.getProperty("env");
            System.setProperty("env", "PROD");
            System.out.println("Do testEnv2 with env: " + System.getProperty("env"));
            System.setProperty("env", env);
        }
    }

    @Nested
    class TimeRelatedTest {

        @Test
        void testIsExpired() {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse("2021-03-21T16:47:22.757+08:00[Asia/Shanghai]");
            assertTrue(TimeRelated.isExpired(zonedDateTime));
        }
    }

    @Test
    void testEnvironmentRelated() {
        assertEquals("dir1/dir2", EnvironmentRelated.generatePath("dir1", "dir2"));
    }

    @Nested
    class MapSetRelatedTest {
        @Test
        void testSetToString() {
            Set<Integer> set = new HashSet<>();
            set.add(5);
            set.add(7);
            assertEquals("[5, 7]", set.toString());
        }

        @Test
        void testMapToString() {
            Map<Integer, Integer> map = new HashMap<>();
            map.put(1, 3);
            map.put(2, 4);
            assertEquals("{1=3, 2=4}", map.toString());
        }
    }
}
