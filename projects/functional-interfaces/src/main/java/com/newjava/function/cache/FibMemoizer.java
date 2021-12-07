package com.newjava.function.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class FibMemoizer {
    public static Long callMemorized(BiFunction<UnaryOperator<Long>, Long, Long> function, Long n) {
        UnaryOperator<Long> memoized = new UnaryOperator<>() {
            private final Map<Long, Long> store = new HashMap<>();
            @Override
            public Long apply(Long input) {
                if (store.containsKey(input)) {
                    return store.get(input);
                }
                Long result = function.apply(this, input);
                store.put(input, result);
                return result;
            }
        };
        return memoized.apply(n);
    }
}
