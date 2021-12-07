package com.newjava.junit5;

import com.newjava.junit5.annotation.ApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.newjava.junit5.CustomStringUtils.beautifyString;
import static com.newjava.junit5.CustomStringUtils.capitalAndLowercaseRest;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Tags(value = {
        @Tag("regression"),
        @Tag("core")
})
class CustomStringUtilsTest {

    private void customAssertEquals(String expected, String actual) {
        if (actual.equals(expected)) {
            return;
        }
        throw  new RuntimeException("expected: " + expected + ", Actual: " + actual);
    }

    private void println(String str) {
        System.out.println(str);
    }

    @ApiTest
    @DisplayName("test beautify string")
    void testBeautifyString() {
        println(beautifyString(" hello  world,  new java!  "));
        assertEquals("hello world, new java!", beautifyString(" hello  world,  new java!  "));
    }

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "'  hELLO', 'Hello'",
            "'Hello World', 'Hello world'"
    })
    void testCapitalAndLowercaseRest(String input, String expected) {
        assertEquals(expected, capitalAndLowercaseRest(input));
    }

    @ParameterizedTest(name = "Year {0} is a leap year.")
    @ValueSource(ints = { 2016, 2020, 2048 })
    void testIsLeapYear(int year) {
        assertTrue(Year.isLeap(year));
    }

    @ParameterizedTest
    @EnumSource(ChronoUnit.class)
    void testWithEnumSource(TemporalUnit unit) {
        assertNotNull(unit);
    }

    @ParameterizedTest
    @MethodSource("stringIntAndListProvider")
    void testWithMultiArgMethodSource(String str, int num, List<String> list) {
        assertEquals(5, str.length());
        assertTrue(num >=1 && num <=2);
        assertEquals(2, list.size());
    }

    static Stream<Arguments> stringIntAndListProvider() {
        return Stream.of(
                arguments("apple", 1, Arrays.asList("a", "b")),
                arguments("lemon", 2, Arrays.asList("x", "y"))
        );
    }

    @ApiTest
    void testCustomTestAnnotation() {
        assertEquals("abc", "a".concat("bc"));
    }

    @Test
    void testAssertEquals() {
        assertEquals(2, 1+1);
        assertEquals("abc", "ab"+"c");
        assertEquals(2, 1+1, "failed to sum");
        assertEquals(2, 1+1, () -> "failed" + "to" + "sum");
        assertEquals(2.2123f, 2.2123f);

        assertSame(new String("123"), new String("123"));
    }

    @Test
    void testListEqual() {
        List<Integer> expectedList = Arrays.asList(1, 3, 5);
        List<Integer> integers = new LinkedList<>();
        integers.add(1);
        integers.add(3);
        integers.add(5);
        assertIterableEquals(expectedList, integers);
    }

    @Test
    void testMapEqual() {
        Map<String, String> expectedMap = Stream.of(new String[][] {
                {"France", "Paris"},
                {"China", "Beijing"}
        }).collect(Collectors.toMap(item -> item[0], item -> item[1]));

        Map<String, String> map = new HashMap<>();
        map.put("China", "Beijing");
        map.put("France", "Paris");
    }

    @Test
    void testAssertTrue() {
        assertTrue("hello world".contains("world"));
        assertTrue(Arrays.asList(1, 3, 5).contains(3));
    }

    @Test
    void testAssertThrows() {
        Function<Integer, Integer> div = num -> 2 / num;
        assertThrows(ArithmeticException.class, () -> div.apply(0));
        assertThrows(NullPointerException.class, () -> div.apply(null));
    }

    @Test
    void testAssertDoesNotThrow() {
        Function<Integer, Integer> div = num -> 2 / num;
        assertDoesNotThrow(() -> div.apply(1));
    }

    @Test
    void testAssertAll() {
        Map<String, String> map = new HashMap<>();
        map.put("China", "Beijing");
        map.put("France", "Paris");

        assertAll("map",
                () -> assertEquals("Paris", map.get("China")),
                () -> assertEquals("Beijing", map.get("France"))
        );
    }

}