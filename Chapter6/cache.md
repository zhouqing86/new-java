# 第2节：计算缓存

相信大家对`斐波那契数列`都比较熟悉，其是一个数列，从第三项开始，每一项都等于前两项之和，如：

```wiki
1, 1, 2, 3, 5, 8, 13, 21, ....
```

在编程上其是一个介绍递归函数的典型例子：

```java
public static Integer fib(Integer n) {
    if (n <= 1) {
        return n;
    }
    return fib(n-1) + fib(n-2);
}
```

这并不是最优解法，因为其复杂度是指数级的，当`n`是一个比较大的值（如>50的值）时，调用`fib(n-1`)和`fib(n-2)`必然会带来大量的重复的结算。

```java
fib(n) = fib(n-1) + fib(n-2)
fib(n-1) = fib(n-2) + fib(n-3)
```

在上面的递归算法中，要获取`fib(n)`的值，需要计算两次`fib(n-2)`的值。如何解决这个重复计算的问题，除了从算法上做优化，也可以考虑使用计算缓存:

```java
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
```

`FibMemoizer`的`callMemorized`方法定义的很巧妙，第一个参数是`BiFunction<UnaryOperator<Long>, Long, Long>`，其应是负责主体计算逻辑，举个例子:

```java
(memoFunction, n) -> {
  if (n <= 1) {
    return n;
  }
  return memoFunction.apply(n-1) + meoFunction.apply(n-2);
}
```

在`callMeorized`函数内，定义了一个名为`memoized`函数，这个函数主要负责计算缓存的处理：

- 定义名为`store`的`HashMap`来存储中间的计算结果，在每次调用`callMemorized`方法时，都会创建一个新的`HashMap`。
- 传入的参数如果在`HashMap`中已经存在计算结果，直接返回；否则将调用上面定义的逻辑计算函数来进行计算。

巧妙的地方时，`memoized`函数中，在调用逻辑计算函数时，把自身也传给了逻辑计算函数，因而使得逻辑计算函数中的可以直接使用`memoized`函数。

```java
public class Fibonacci {

    public static Long fib(Long n) {
        BiFunction<UnaryOperator<Long>, Long, Long> compute = (memoFunc, input) -> {
            if (input <= 1) {
                return input;
            }
            return memoFunc.apply(input-1) + memoFunc.apply(input-2);
        };
        return callMemorized(compute, n);
    }

    public static Long normalFib(Long n) {
        if (n <= 1) {
            return n;
        }
        return normalFib(n-1) + normalFib(n-2);
    }
}
```

对其进行简单的性能测试：

```java
class FibonacciTest {
    @Test
    void testFib() {
        long l = System.currentTimeMillis();
        Fibonacci.fib(40L);
        System.out.println(System.currentTimeMillis() - l); //笔者的一次测试中结果为2

        l = System.currentTimeMillis();
        Fibonacci.normalFib(40L);
        System.out.println(System.currentTimeMillis() - l); //笔者的一次测试中结果为1454
    }
}
```





