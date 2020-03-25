package com.newjava.junit5;

import java.util.Map;
import java.util.StringJoiner;

public class BigObject {

    private String someField1;

    private String someField2;

    private String someField3;

    private BigObject() {
        super();
    }

    public String calculate(String prefix, String suffix) {
        StringJoiner joiner = new StringJoiner(", ", prefix, suffix);
        joiner.add(someField1);
        joiner.add(someField2);
        joiner.add(someField3);
        return joiner.toString();
    }

    public static class Builder {

        private static void buildStep1(BigObject obj, Map<String, String> map) {
            obj.someField1 = map.get("key1");
        }

        private static void buildStep2(BigObject obj, Map<String, String> map) {
            obj.someField2 = map.get("key2");
        }

        private static void buildStep3(BigObject obj, Map<String, String> map) {
            obj.someField3 = map.get("key3");
        }

        public static BigObject create(Map<String, String> map) {
            BigObject obj = new BigObject();
            buildStep1(obj, map);
            buildStep2(obj, map);
            buildStep3(obj, map);
            return obj;
        }
    }
}
