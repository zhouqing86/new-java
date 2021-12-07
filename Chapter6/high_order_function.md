# 第1节：高阶函数

返回值为函数的函数即是高阶函数，可以把高级函数看成一个函数工厂，高阶函数当然也可以返回高阶函数；高阶函数的参数也可以是函数/高阶函数。这就让高级函数在使用上变得灵活且复杂。

> 这里我们把Java中常称为的方法也可以统称为函数。

#### 函数工厂

顾名思义，生成函数的函数即为工厂函数：

```java
Function<Integer, Integer> squareFunctionFactory(Integer base) {
    return new Function<Integer, Integer>() {
        @Override
        public Integer apply(Integer i) {
            return base + i * i;
        }
    };
}
```

通过这个工厂函数我们就可以随意的生成很多的函数，如:

```java
@Test
void testSquareFuncWithBase() {
    Function<Integer, Integer> squareFuncWithBase1 = squareFunctionFactory(1);
    Function<Integer, Integer> squareFuncWithBase2 = squareFunctionFactory(2);

    assertEquals(5, squareFuncWithBase1.apply(2));
    assertEquals(6, squareFuncWithBase2.apply(2));
}
```

`Java`在各种函数式接口中提供了大量的工厂函数，如`Function`接口中最简单的工厂函数：

```java
static <T> Function<T, T> identity() {
    return t -> t;
}
```

调用静态方法`identity`可以生产出支持各种类型的`identity`函数：

```java
@Test
void testIdentity() {
    Function<Integer, Integer> integerIdentityFunction = Function.identity();
    assertEquals(10, integerIdentityFunction.apply(10));

    Function<String, String> stringIdentityFunction = Function.identity();
    assertEquals("Hello", stringIdentityFunction.apply("Hello"));
}
```

#### 函数型参数

当一个高阶函数能够处理函数类型的参数时，这个高阶函数就会变得异常的灵活好用。可以用这样的高阶函数来实现简单的模板模式：

```java
<T,R> R loggingFunction(T t, Function<T, R> function) {
    R r = function.apply(t);
    System.out.println(String.format("Input is %s, result is %s", t, r));
    return r;
}

@Test
void testLoggingFunction() {
    loggingFunction(1, t -> t + 1);
    loggingFunction(2, t -> t * t);
}
```

对于列表/数组型数据，此类高阶函数尤其好用，可以减少大量重复代码的编写，譬如我们定义一个合并两个列表的函数：

```java
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
```

> 例子会略有点复杂，但是希望读者能够花时间完全理解这个例子

我们可以很方便的使用这个函数：

```java
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
```

这里定义了一个`PhoneSale`来测试这个`mergeTwoList`方法。不过`mergeTwoList`方法实际上已经可以用于任意一个实体对象，只需要使用者提供两个函数：实体对象的`keyExtractorFunction`和处理两个实体对象的参数的`mergeFunction`即可。

`Java`中有大量这样的高阶函数，如`List`接口中的`forEach`、`replaceAll`等，`Optional`对象中的`filter`、 `map`、 `orElseGet`、 `ifPresent`等，`Stream`接口中的`map`、 `flatMap`、 `filter`等等。

```java
//List接口中
default void forEach(Consumer<? super T> action) {
    Objects.requireNonNull(action);
    for (T t : this) {
        action.accept(t);
    }
}

//Optional对象中
public void ifPresent(Consumer<? super T> action) {
    if (value != null) {
        action.accept(value);
    }
}

//Stream接口中
<R> Stream<R> map(Function<? super T, ? extends R> mapper);
```

#### 函数型参数的工厂函数

如果一个函数即接收函数作为参数，又返回另外一个函数，则其显得更加灵活但难懂。但是这样的函数也是会经常用到的，典型的如`Function`接口的`compose`和`andThen`函数：

```java
default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
    Objects.requireNonNull(before);
    return (V v) -> apply(before.apply(v));
}

default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (T t) -> after.apply(apply(t));
}
```

通过`compose`和`andThen`就可以很方便的把函数组合起来。关于这两个方法的使用读者可以参考第一章第一节的内容。

`Java`的`Comparator`方法也提供了大量的这样的工厂函数，如:

```java
public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
        Function<? super T, ? extends U> keyExtractor)
{
    Objects.requireNonNull(keyExtractor);
    return (Comparator<T> & Serializable)
        (c1, c2) -> keyExtractor.apply(c1).compareTo(keyExtractor.apply(c2));
}
```

这么一个简单的函数，让代码复用变得简单，且代码更易读：

```java
@Test
void testComparing() {
    Comparator<PhoneSale> comparing = Comparator.comparing(PhoneSale::getPhone).thenComparing(PhoneSale::getSales);
    assertEquals(Integer.valueOf(13).compareTo(16),
            comparing.compare(new PhoneSale("Iphone", 13), new PhoneSale("Iphone",16)));
    assertEquals("Iphone".compareTo("Xiaomi"),
            comparing.compare(new PhoneSale("Iphone", 13), new PhoneSale("Xiaomi",13)));
}
```





