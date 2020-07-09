# 第3节：Stream集合操作

Java8中定义了`Stream`接口，其实对集合进行声明式编程的很重要的接口，其定义了很多接收函数式接口参数的方法，如`map`、`filter`、`flatMap`、`reduce`、`find`、`match`、`sorted`等。本节将详细介绍基于`Stream`中的一些基本概念以及其强大的集合操作能力。

```java
List.of(3, 4, 5, 6, 7)
  .stream()
  .map(Math::sqrt)
  .filter(d -> d > 2)
  .mapToInt(Double::intValue)
  .limit(2)
  .reduce(0, Integer::sum);
```

#### 什么是Stream

上面有提到，`Stream`是Java语言中定义的接口，Java语言就很多实现`Stream`接口的类，为了方便这里把这些实现也统称为`Stream`，那么关于`Stream`的语义，这里有一个来自[Oracle](https://www.oracle.com/technical-resources/articles/java/ma14-java-se-8-streams.html)的简单定义：

```wiki
a sequence of elements from a source that supports aggregate operations
来自某个源的一系列元素，能够支持各种聚合操作
```

每个`Stream`对象只是对数据源的封装，从而使得程序员可以对数据源进行一系列操作（计算、排序、过滤、查找、分组等等）。其有一些非常鲜明且必须记住的特点：

- 基于`Stream`的任何操作都不会修改底层数据源。
- `Stream`中将各种聚合操作方法分为两类：中间操作方法（`intermediate operation `）和终止操作方法（`Terminal Operations`，中间操作方法返回的还是当前`Stream`对象，终止操作才返回具体的操作后结果。
- `Stream`对象不能被重复使用，意味着不能在被使用过（调用过终止操作方法）后的`Stream`对象上再调用任何聚合操作。
- `Stream`对象中间操作方法的调用并不会直接触发相关计算，只有终止操作方法的调用才会触发计算，这个计算将组合所有中间操作方法相关的函数。
- `Stream`的底层数据源往往是`Iterable`的对象，`Stream`将自己在内部进行迭带操作，`Stream`的使用者不需要编写数据源迭带相关代码。

我们可以根据这个定义尝试编写我们自己的`Stream`的简单实现来理解以上几条:

```java
class MyIntegerStream {
    Integer input;
    Function<Integer, Integer> calFunction = UnaryOperator.identity();
    MyIntegerStream(Integer input) {
        this.input = input;
    }
    public MyIntegerStream intermediateOperation1(UnaryOperator<Integer> function) {
        calFunction = calFunction.andThen(function);
        return this;
    }

    public MyIntegerStream intermediateOperation2(BinaryOperator<Integer> binaryFunction, Integer secondParam) {
        Function<Integer, Integer> curryFunction = i -> binaryFunction.apply(i, secondParam);
        calFunction = calFunction.andThen(curryFunction);
        return this;
    }

    public Integer terminateOperation() {
        return calFunction.apply(input);
    }
}

@Test
void testMyIntegerStream() {
    Integer result = new MyIntegerStream(3)
            .intermediateOperation1(num -> num * num)
            .intermediateOperation2((num1, num2) -> Math.max(num1, num2), 10)
            .terminateOperation();
    assertEquals(10, result);
}
```

我们自定义的这个`MyIntegerStream`有两个中间操作，中间操作只是将传入的函数式接口进行组合，并不进行实际上的运算。在`terminateOperation`方法里才会真正的进行计算。

> `MyIntegerStream`仅仅是为了方便读者对Stream中的规则的理解，Java语言中的Stream的实现比`MyIntegerStream`的实现要复杂很多，能够理解并看懂Stream的源码并非易事。

#### Stream对象的创建

`Stream`接口中定义了一些静态构造方法：

```java
//构建空Stream
public static<T> Stream<T> empty(){...}
Stream<Integer> empty = Stream.empty();

//构建只有一个元素的Stream，不允许传入的参数为null, Stream.of(null)将抛出空指针异常
  public static<T> Stream<T> of(T t){...}
Stream<Integer> oneElementStream = Stream.of(1);

//构建只有一个元素的Stream或空Stream
public static<T> Stream<T> ofNullable(T t){...}
Stream<Integer> oneElementOrEmptyStream = Stream.ofNullable(null);

//构建包含多个元素的Stream，Stream中将保留传入参数的先后顺序，values中不能有null元素
public static<T> Stream<T> of(T... values){...}
Stream<Integer> multipleElementsStream = Stream.of(1, 2, 3);

//创建无限流，第一个参数为第一个元素，第二个参数是一个函数，其给出根据上一个元素值计算下一个元素值的方法
public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {...}
Stream<Integer> infiniteStream = Stream.iterate(1, num -> num + 2);

//创建有边界的流，第一个参数为第一个元素，第二个参数判断是否还需要继续生成下一个参数，第三个参数给出基于上一个元素计算下一个元素的方法
public static<T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next){...}
Stream<Integer> boundedStream = Stream.iterate(1, num -> num < 10, num -> num + 2);

//创建无限流，每个元素的计算都是通过传入的`Supplier`函数式接口得到
public static<T> Stream<T> generate(Supplier<? extends T> s);
Stream<Double> infiniteRandomStream = Stream.generate(Math::random);
```

> 注意：`Stream.of(new int[]{1, 2, 3})`调用的是`Stream<T> of(T t)`方法，Stream.of(new Integer[]{1, 2, 3})才会调用`Stream<T> of(T... values)`方法。对于元型数组，建议使用Arrays.stream的方式去创建。

`Stream`接口中还定义了一个`Builder`接口，因而还可以通过这个`Builder`接口来创建`Stream`对象:

```java
Stream<Integer> builderStream = Stream.<Integer>builder().add(1).add(2).build();
```

更为常用的生成`Stream`对象的方法，往往是直接调用`Collection`接口中的`stream`方法：

```java
Stream<Integer> lstStream = List.of(1, 2, 3).stream();
Stream<Integer> setStream = Set.of(1, 2, 3).stream();
```

> 注意：`List`或`Set`不能存在`null`元素，否则会抛出空指针异常。

或者直接将数组转变成`Stream`对象:

```java
Stream<Integer> stream = Arrays.stream(new Integer[]{1, 2, 3});
```

比较底层的创建`Stream`对象的方式是使用`StreamSupport.stream`方法：

```java
Iterable<Integer> iterable = List.of(1, 2, 3);
Stream<Integer> stream = StreamSupport.stream(iterable.spliterator(), false);
```

另外，为了方便对Java语言中元类型`int`、`long`、`double`的处理，定义了`IntStream`、`LongStream`和`DoubleStream`，其也定义各自特性化的创建`Stream`对象的方法，如`IntStream`和`LongStream`中定义的`range`和`rangeClosed`方法：

```java
@Test
void testCreateIntStreamByRange() {
    assertArrayEquals(new int[]{1, 2}, IntStream.range(1, 3).toArray());
    assertArrayEquals(new int[]{1, 2, 3}, IntStream.rangeClosed(1, 3).toArray());
}
```

#### Stream终止操作

先来熟悉下Java语言中常见的Stream终止操作。

##### forEach

对于初学者，最熟悉的就是`forEach`了：

```java
List.of(1, 2, 3).stream().forEach(System.out::println);
```

##### count

`count`方法用来计算`Stream`中元素的个数：

```java
@Test
void testStreamCount() {
    assertEquals(1, Stream.of(new int[]{1, 2, 3}).count());
    assertEquals(3, Stream.of(new Integer[]{1, 2, 3}).count());
}
```

##### findFirst/findAny

`findFirst`方法返回一个`Optional`的对象：

```java
@Test
void testStreamFindFirst() {
    assertEquals(Optional.empty(), Stream.ofNullable(null).findFirst());
    assertEquals(Optional.of(1), Stream.of(1, 2, 3).findFirst());
}
```

`findAny`方法与`findFirst`类似，也是返回一个`Optional`的对象：

```java
@Test
void testStreamFindAny() {
    assertEquals(Optional.empty(), Stream.ofNullable(null).findAny());
    assertNotEquals(Optional.empty(), Stream.of(1, 2, 3).findAny());
}
```

> `findFirst`或`findAny`操作与Stream中间操作方法如`filter`结合起来会比较有用。

##### anyMatch/allMatch/noneMatch

`anyMatch`、`allMatch`和`noneMatch`这三个方法都接收一个`Predicate`类型的函数式接口作为参数，返回`boolean`：

```java
@Test
void testStreamAnyMatch() {
    assertTrue(Stream.of(1, 2, 3).anyMatch(num -> num % 2 == 0));
    assertFalse(Stream.of(1, 2, 3).anyMatch(num -> num > 3));
}

@Test
void testStreamAllMatch() {
    assertFalse(Stream.of(1, 2, 3).allMatch(num -> num % 2 == 0));
    assertTrue(Stream.of(1, 2, 3).allMatch(num -> num < 4));
}

@Test
void testStreamNoneMatch() {
    assertFalse(Stream.of(1, 2, 3).noneMatch(num -> num % 2 == 0));
    assertTrue(Stream.of(1, 2, 3).noneMatch(num -> num > 3));
}
```

##### max/min

`max`和`min`这两个方法都接收一个`Comparator`类型的函数式接口作为参数，返回`Optional`对象：

```java
@Test
void testStreamMax() {
    assertEquals(Optional.empty(), Stream.<Integer>ofNullable(null).max(Comparator.naturalOrder()));
    assertEquals(Optional.of(3), Stream.of(2, 3, 1).max(Comparator.naturalOrder()));
}

@Test
void testStreamMin() {
    assertEquals(Optional.empty(), Stream.<Integer>ofNullable(null).min(Comparator.naturalOrder()));
    assertEquals(Optional.of(1), Stream.of(2, 3, 1).min(Comparator.naturalOrder()));
}
```

##### toArray

`Stream`对象可以直接转换成数组:

```java
@Test
void testStreamToArray() {
    assertArrayEquals(new Object[]{1, 2, 3}, Stream.of(1, 2, 3).toArray());
    assertArrayEquals(new Integer[]{1, 2, 3}, Stream.of(1, 2, 3).toArray(Integer[]::new));
}
```

> 注意不带参数的`toArray`方法返回的是`Object[]`，而带参数的`toArray`方法返回的值与其参数又关，其参数为`IntFunction`，而`Integer[]::new`、`String[]::new`等都可以赋值给`IntFunction`类型。

##### reduce

`Stream`接口中定义了三个`reduce`方法，第一个`reduce`方法的声明：

```java
T reduce(T identity, BinaryOperator<T> accumulator);
```

第一个`reduce`方法最容易理解，第一个参数`identity`可以理解为初始化值。譬如对`Stream`对象中的所有元素进行求和，那么和的初始值就为`0`; 第二个参数是一个`BinaryOperator`类型的函数式接口，这个接口代表的函数接收两个参数，第一个参数是已经遍历过的元素的和，第二个参数是下一个元素：

```java
@Test
void testStreamReduceWithIdentityAndAccumulator() {
    assertEquals(6, Stream.of(1, 2, 3).reduce(0, (identity, element) -> identity + element));
  	assertEquals(0, Stream.<Integer>ofNullable(null).reduce(0, (identity, element) -> identity + element));
}
```

如果读者对`identity`和`accumulator`还是不太了解，我们可以用一种古老的方式来解释：

```java
int identity = 0;
BinaryOperator<Integer> accumulator = (i, element) -> i + element;
for (Integer i : List.of(1, 2, 3)){
    identity = accumulator.apply(identity, i);
}
```

第二个`reduce`方法的声明：

```java
Optional<T> reduce(BinaryOperator<T> accumulator);
```

第二个`reduce`方法中并没有一个初始化的值，我们假定其内部会默认将`identity`初始化`null`值，而`identity`与第一个元素的结合并不会使用`accumulator`函数，而是会直接取第一个元素的值来替换掉`identity`的初始`null`值。

```java
@Test
void testStreamReduceWithAccumulator() {
    assertEquals(Optional.of(6), Stream.of(1, 2, 3).reduce((result, nextElement) -> result + nextElement));
    assertEquals(Optional.empty(), Stream.<Integer>ofNullable(null).reduce((result, nextElement) -> result + nextElement));
		
  	//下面的这种情况使用第一种reduce方法就不合适了，因为初始值不论取true或false都可能会影响最终结果
    assertEquals(Optional.of(true), Stream.of(false, false, true).reduce((result, nextElement) -> result || nextElement));
}
```

第三个`reduce`方法的声明：

```java
<U> U reduce(U identity,
             BiFunction<U, ? super T, U> accumulator,
             BinaryOperator<U> combiner);
```

前面两个`reduce`的方法的参数中都使用的是`BinaryOperator`，意味着参与计算的所有中间资源都是同类型的。假设有一个`User`列表，我们需要计算所有的`User`的年龄(`age`)之和，前面两种`reduce`就无法达到目标：

```java
List<User> users = List.of(new User("A", 24), new User("B", 34));

users.stream().reduce(0, (identity, user) -> identity + user.getAge()); //编译出错
users.stream().reduce((identity, user) -> identity + user.getAge()); //编译出错
```

编译出错的原因是前两个`reduce`方法接收的是`BinaryOperator<Integer>`类型，即`BiFunction<Integer, Integer, Integer>`类型，并不接收`BiFunction<Integer, User, Integer`类型。第三个`reduce`方法可以解决这个问题：

```java
@Test
void testStreamReduceWithIdentityAccumulatorCombiner() {
    List<User> users = List.of(new User("A", 24), new User("B", 34));
    assertEquals(58, users.stream().reduce(0, (identity, user) -> identity + user.getAge(), Integer::sum));
}
```

正常来理解有`identity`和`accumulator`两个参数就可以完成这个年龄求和的问题，为什么有`combiner`参数的存在呢，是因为`Stream`接口也考虑了多线程并行计算的情况，如两个线程，线程1计算了一部分结果，线程2计算了另一部分结果，两个结果的合并就通过`combiner`函数进行。

在单线程中，`combiner`函数不会被调用，只有在多线程计算时才有可能被调用：

```java
users.stream().parallel().reduce(0, (identity, user) -> identity + user.getAge(), (a, b) -> {
    System.out.println("a=" + a + ", b=" + b);
    return a + b;
});
```

> `parallel`方法的调用使得Stream的`reduce`操作并行处理。

#### collect



#### Stream中间操作





#### Stream的并行处理

