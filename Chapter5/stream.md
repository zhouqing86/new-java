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

//创建无限流，每个元素的计算都是通过传入的Supplier函数式接口得到
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

`String`类中也添加了一些创建`Stream`对象的方法，如`chars`、`codePoints`和`lines`：

```java
@Test
void testCreateStreamWithStringMethods() {
    assertEquals(2, "Hello\nWorld".lines().count()); //lines方法获取Stream<String>类型的Stream对象
    assertEquals(2, "\uD83D\uDE03".chars().count()); //chars方法获取IntStream对象
    assertEquals(1, "\uD83D\uDE03".codePoints().count()); //codePoints方法获取IntStream对象
}
```

> Java语言中，char的范围只能是在\u0000到\uffff，char类型用UTF-16编码描述一个代码单元，而unicode的范围从000000 - 10FFFF，对于unicode大于0x10000的部分，如😀，在Java中占用两个char:\uD83D和\uDE03，大这两个char合起来只是一个codePoint。

#### Stream终止操作

先来熟悉下Java语言中常见的`Stream`终止操作，通过终止操作方法可以来获取`Stream`对象的最终计算/操作。

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
class User {
    private String name;
    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    ... setter and getter
}

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

##### collect

`reduce`方法可以做一些计算和聚合操作，但是其不能处理的其他情况，譬如将`Stream`对象的数据结果返回`List`或`Set`，这时`collect`方法就来了，`collect`方法应该是`Stream`接口中使用起来最多变/复杂的方法。

`Stream`接口中定义了两个`collect`方法，第一个`collect`方法：

```java
<R> R collect(Supplier<R> supplier,
        BiConsumer<R, ? super T> accumulator,
        BiConsumer<R, R> combiner);
```

其与第三个`reduce`方法有点类似，只是`identity`变成了`supplier`:

```java
@Test
void testStreamCollectWithSupplierAccumulatorCombiner() {
    BiConsumer<List<Integer>, Integer> accumulator = (lst, e) -> lst.add(e);
    BiConsumer<List<Integer>, List<Integer>> combiner = (lst1, lst2) -> lst1.addAll(lst2);
    assertEquals(List.of(1, 2, 3), Stream.of(1, 2, 3).collect(ArrayList::new, accumulator, combiner));
}
```

第一个`collect`方法写起来还是比较复杂的，需要使用者很清楚这三个参数，Java为了减轻使用者的负担，定义了`Collector`接口，将`supplier`、`accumulator`以及`combiner`都定义在这个接口里，并同时提供`Collectors`类提供大量生成`Collector`对象的静态方法，于是有了第二个`collect`方法:

```java
<R, A> R collect(Collector<? super T, A, R> collector);
```

有了这个方法，写起来会很简洁：

```java
@Test
void testStreamCollectWithCollector() {
    assertEquals(List.of(1, 2, 3), Stream.of(1, 2, 3).collect(Collectors.toList()));
  	assertEquals(Set.of(1, 2, 3), Stream.of(1, 2, 3).collect(Collectors.toSet()));
}
```

因为“脏活”都让`Collectors.toList()`和`Collectors.toSet()`帮忙做了。如果去查看`Collectors`的源代码，会发现其定义了大量的静态方法。有了这个强大的愿意干脏活的`Collectors`，`collect`方法也能放飞自我了。

如使用`Collectors.joining`将元素拼接在一起：

```java
@Test
void testStreamCollectWithCollectorsJoining() {
    assertEquals("{A,B,C}", Stream.of("A", "B", "C").collect(Collectors.joining(",", "{", "}")));
}
```

再如使用`Collectors.groupingBy`根据用户的年龄进行分组：

```java
@Test
void testStreamCollectGroupingByUserAge() {
    User user1 = new User("A", 24);
    User user2 = new User("B", 50);
    User user3 = new User("C", 24);
    List<User> users = List.of(user1, user2, user3);
		//根据年龄的数字分组
    Map<Integer, List<User>> groupedResult = users.stream().collect(Collectors.groupingBy(User::getAge));

    assertEquals(List.of(user1, user3), groupedResult.get(24));
    assertEquals(List.of(user2), groupedResult.get(50));
    assertNull(groupedResult.get(100));

    Function<User, String> classifier = user -> {
      	//世界卫生组织将44岁以下的人群称为青年人
        if (user.getAge() <= 44) {
            return "Young";
        } else if (user.getAge() > 44 && user.getAge() <= 55 ) {
            return "Middle-aged";
        }
        return "Old";
    };
  	//根据年龄段分组
    Map<String, List<User>> groupedResult2 = users.stream().collect(Collectors.groupingBy(classifier));
  
    assertEquals(List.of(user1, user3), groupedResult2.get("Young"));
    assertEquals(List.of(user2), groupedResult2.get("Middle-aged"));
    assertNull(groupedResult2.get("Old"));
}
```

如果我们希望分组的结果`Map`中年龄最大的，可以这么做：

```java
User user4 = new User("D", 40);
users = List.of(user1, user2, user3, user4);
Map<String, Optional<User>> groupedResult3 = users.stream().collect(
        Collectors.groupingBy(
                classifier,
                Collectors.reducing(
                    BinaryOperator.maxBy(Comparator.comparing(User::getAge))
                )
        )
);
```

`groupingBy`方法也可以在第二个参数接收`Collector`对象。这就意味着，我们第二个参数也可以使用`Collectors.groupingBy`进行进一步根据姓名分组:

```java
Map<String, Map<String, List<User>>> groupedResult5 = users.stream().collect(
        Collectors.groupingBy(
                classifier,
                Collectors.groupingBy(User::getName)
        )
);
```

如果我们需要计算每个年龄分组里的年龄和：

```java
Map<String, Integer> groupedResult5 = users.stream().collect(
        Collectors.groupingBy(
                classifier,
                Collectors.reducing(0, User::getAge, Integer::sum)
        )
);
assertEquals(88, groupedResult5.get("Young"));
assertEquals(50, groupedResult5.get("Middle-aged"));
```

如果只想统计年龄段里的人个数：

```java
Map<String, Integer> groupedResult6 = users.stream().collect(
        Collectors.groupingBy(
                classifier,
                Collectors.reducing(0, user -> 1, (count, user)->count+1)
        )
);
assertEquals(3, groupedResult6.get("Young"));
assertEquals(1, groupedResult6.get("Middle-aged"));
```

#### Stream中间操作

`Stream`的中间操作（`Intermediate Operation`) 将一个`Stream`转换成另外一个`Stream`。因而中间操作之间可以进行链式调用。调用`Stream`的中间操作并不会触发结果计算，只有调用终止操作才会开始对结果的计算，因而可以说`Stream`的是惰性求值（`Lazy Evaluation`)的。

##### map

当我们需要对`Stream`中的每个元素进行转化，就可以使用`map`，如我们对每个元素进行计算，计算规则是是先进行平方而后再加上2：

```java
@Test
void testMap() {
    List<Integer> lst = Stream.of(1, 2, 3)
            .map(num -> num * num)
            .map(num -> num + 2)
            .collect(Collectors.toList());
    assertEquals(List.of(3, 6, 11), lst);
}
```

`map`操作后，输入`Stream`和输出`Stream`中元素的个数是不变的。

##### flatMap

如果`Stream`中的每个元素都是一个数组，而我们想把这些数组里的元素都串接到一起，可以使用`flatMap`来做：

```java
@Test
void testFlatMap() {
    List<String> words = Stream.of("Hello world", "Hello my friend")
            .map(str -> str.split(" "))
            .flatMap(arr -> Arrays.stream(arr))
            .collect(Collectors.toList());
    assertEquals(5, words.size());
}
```

本例中先通过`map`操作将Stream里的每个字符串转化成字符串数组，即将`Stream<String>`转换成了`Stream<String[]>`，而通过`flatMap`操作，将`Stream<String[]>`中的每个字符串数组提取出来串接到一起，变成了另一个`Stream<String>`。

`flatMap`操作会使得输出`Stream`中元素的个数发生变化，相对输入`Stream`，常见的是输入出`Stream`中元素个数变多了。

##### filter

如果只期望过滤出`Stream`中满足某些条件的元素，可以使用`filter`:

```java
@Test
void testFilter() {
    List<Integer> lst = Stream.of(1, 2, 3)
            .filter(num -> num % 2 == 0)
            .collect(Collectors.toList());
    assertEquals(List.of(2), lst);
}
```

`filter`操作的输出`Stream`中的元素个数往往会变少。需要注意的是是这里的`filter`操作会过滤出满足条件的元素，而不是把满足条件的元素给过滤出去，这是开发人员在刚开始使用`filter`时需要注意的。

##### distinct

如果想去除`Stream`中重复的元素，可以使用`distinct`:

```java
@Test
void testDistinct() {
    List<Integer> lst = Stream.of(1, 2, 3, 1, 2, 3)
            .distinct()
            .collect(Collectors.toList());
    assertEquals(List.of(1, 2, 3), lst);
}
```

##### takeWhile/dropWhile

`takeWhile`和`dropWhile`操作是Java9中引入的，其可以理解为对`while`循环的简化写法。这两个操作都接收函数式接口`Predicate`为其参数，这个`Predicate`给出的是退出`while`循环的条件。

```java
@Test
void testTakeWhile() {
    List<Integer> lst = Stream.of(1, 2, 3)
            .takeWhile(num -> num % 2 != 0)
            .collect(Collectors.toList());
    assertEquals(List.of(1), lst);
}
```

从`Stream`的第一个元素开始，如果满足`takeWhile`的条件，就添加到结果`Stream`中，直到遇到不满足条件的元素则退出`while`循环。

```java
@Test
void testDropWhile() {
    List<Integer> lst = Stream.of(1, 2, 3)
            .dropWhile(num -> num % 2 != 0)
            .collect(Collectors.toList());
    assertEquals(List.of(2, 3), lst);
}
```

从`Stream`的第一个元素开始，如果满足`dropWhile`的条件，则从结果`Stream`中删除，直到有不满足条件的元素出现退出`while`循环。

##### limit

获取`Stream`元素的前面几个，对于无限流或者排序好的流，`limit`非常管用：

```java
@Test
void testLimit() {
    List<Integer> lst = Stream.iterate(1, num -> num + 1)
            .limit(3)
            .collect(Collectors.toList());
    assertEquals(List.of(1, 2, 3), lst);
}
```

##### skip

如果需要在对`Stream`元素访问时有分页的效果，可以使用`skip`和`limit`:

```java
@Test
void testSkip() {
    int page = 2;
    int pageSize = 3;
    List<Integer> lst = Stream.iterate(1, num -> num + 1)
            .skip((page - 1) * pageSize)
            .limit(pageSize)
            .collect(Collectors.toList());
    assertEquals(List.of(4, 5, 6), lst);
}
```

##### sorted

如果需要对`Stream`的元素进行排序，可以使用`sort`：

```java
@Test
void testSorted() {
    List<Integer> lst = Stream.of(3, 1, 2)
            .sorted()
            .collect(Collectors.toList());
    assertEquals(List.of(1, 2, 3), lst);
}

@Test
void testSortedWithComparator() {
    List<Integer> lst = Stream.of(3, 1, 2)
            .sorted(Comparator.<Integer>naturalOrder().reversed())
            .collect(Collectors.toList());
    assertEquals(List.of(3, 2, 1), lst);
}
```

##### peek

如果我们想在链式的中间调用中打印以方便调试，可以使用`peek`：

```java
@Test
void testPeek() {
  List<Integer> lst = Stream.of(1, 2, 3)
          .peek(System.out::print) //第一个peek
          .map(num -> num * num)
          .peek(System.out::print) //第二个peek
          .collect(Collectors.toList());
  assertEquals(List.of(1, 4, 9), lst);
}
```

这里`peek`打印出来的结果是`112439`，因为对`Stream`里的各个元素的操作是顺序执行的。

##### 惰性求值

惰性求值在一些情况下会减少计算量，如`findFirst`终止操作并不要求遍历所有元素，只需要找到第一个符合条件的元素即可：

```java
@Test
void testLazyEvaluation() {
    Optional<Integer> first = Stream.of(1, 2, 3, 4, 5)
      			.map(num -> num * num)
            .peek(System.out::print)
            .filter(num -> num % 2 == 0)
            .findFirst();
    assertEquals(Optional.of(2), first);
}
```

这里`peek`方法打印出来的的结果是`14`，意味着在上面的中间操作`map`和`peek`并没有对所有`Stream`中的元素进行计算，其可以类似理解为所有的中间操作在一个循环里，如：

```java
List<Integer> lst = List.of(1, 2, 3, 4, 5);
for (Integer num : lst) { //顺序访问列表
    int temp = num * num; //对元素做map操作
    System.out.print(temp);
    if (temp % 2 == 0) { //对元素做filter检查
        break; //如果找到第一个，就退出循环
    }
}
```

##### 其他

另外，`Stream`接口中还提供了一些操作来支持对Java原始类型的支持，如`mapToInt`、 `mapToLong`、 `mapToDouble`、 `flatmapToInt`、 `flatmapToLong`、 `flatmapToDouble`。这些方法就生成`IntStream`、`DoubleStream`和`LongStream`。

而`IntStream`、`DoubleStream`和`LongStream`中也提供了`mapToObj`和`boxed`方法将生成`Stream`。

```java
IntStream intStream = Stream.of(1, 2, 3).mapToInt(num -> num);
Stream<Integer> stream = IntStream.range(1, 4).mapToObj(num -> num);
Stream<Integer> stream2 = IntStream.range(1, 4).boxed();
```

#### Stream的并行处理

上面的对`Stream`的各种操作默认是一个线程下顺序执行，意味着在一个线程下，`Stream`里的元素被一次访问和计算。如果`Stream`中有大量的元素，顺序执行的时间会比较长，`Stream`提供了`parallel`方法使得`Stream`在使用多线程并行计算结果：

```java
@Test
void testParallel() {
    System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "2");
    BiConsumer<List<Integer>, Integer> accumulator = (lst, e) -> lst.add(e);
    BiConsumer<List<Integer>, List<Integer>> combiner = (lst1, lst2) -> {
        System.out.println("Combiner: " + Thread.currentThread().getName() + ", lst1=" + lst1 + ", lst2=" + lst2);
        lst1.addAll(lst2);
    };
    List<Integer> lst = Stream.of(1, 2, 3, 4, 5)
            .parallel()
            .map(num -> num * num)
            .peek(num -> {
                System.out.println(Thread.currentThread().getName() + ": " + num);
            })
            .collect(ArrayList::new, accumulator, combiner);
    assertEquals(List.of(1, 4, 9, 16, 25), lst);
}
```

在笔者的电脑上运行这个测试用例，打印出来的结果是：

```java
ForkJoinPool.commonPool-worker-3: 25
ForkJoinPool.commonPool-worker-1: 4
main: 9
ForkJoinPool.commonPool-worker-1: 1
ForkJoinPool.commonPool-worker-3: 16
Combiner: ForkJoinPool.commonPool-worker-1, lst1=[1], lst2=[4]
Combiner: ForkJoinPool.commonPool-worker-3, lst1=[16], lst2=[25]
Combiner: ForkJoinPool.commonPool-worker-3, lst1=[9], lst2=[16, 25]
Combiner: ForkJoinPool.commonPool-worker-3, lst1=[1, 4], lst2=[9, 16, 25]
```

从这个例子，可以看出：

- 并行`Stream`底层使用`ForkJoinPool`的`commonPool`来提供线程参与并行计算，`commonPool`的默认线程个数与计算机的CPU核数有关。
- `collect`自行处理了多线程`combine`结果时的同步问题，`ArrayList`并不是线程安全的，但是这里的`collect`调用方式是线程安全的。
- `main`线程也会参与到计算当中。
- 计算结果在多线程上进行`combine`。

意外的是，虽然做了并行操作，但是我们得到的结果元素顺序还是和初始`Stream`中的元素顺序保持一致。



