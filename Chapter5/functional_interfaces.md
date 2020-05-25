# 第1节：函数式接口

在第一章第1节介绍Java8新特性时，对函数式接口已经有了初步的认识。Java在`java.util.function`包中已定义了大量的函数式接口，本节将进一步介绍常用的函数式接口。

> 本节的例子基于`openjdk 11.0.2`，使用`Gradle 6.3`来构建管理项目代码。

#### 生产者接口 — Supplier

`Supplier`可翻译为`生产者`，其用来生产数据，四个字描述“无中生有”。`Supplier`接口中的函数方法为`T get()`。

```java
Supplier<Integer> randInt = () -> new Random().nextInt();
Supplier<String> newString = String::new;
```

基于生产者实现懒加载或懒计算：

```java
private Double calSomething(Supplier<Double> lazyLogic) {
	return lazyLogic.get() * 2;
}

Supplier<Double> lazySupplier = () -> {
    try {
        Thread.sleep(1000);
        return 2d;
    } catch (InterruptedException e) {
        return 0d;
    }
};
```

在`log4j2`日志库中，定义了很多的日志打印相关的懒加载方法，如：

```java
//log4j2中定义的debug函数
void debug(String message, Supplier<?>... paramSuppliers);

//debug函数的使用，只有当需要打印debug级别的日志时，第二个参数的函数才会被执行
logger.debug("User Profile: {}", () -> generateAnString());
```

`Supplier`与`Stream`结合可以实现懒加载的无限流：

```java
//无限每隔1秒钟生成一个UUID并打印出来
Stream.generate(UUID::randomUUID)
        .forEach(item -> {
            try {
                Thread.sleep(1000);
                System.out.println(item);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
```

不仅仅类的静态方法可以赋值给`Supplier`接口，创建的对想的实例方法也可以赋值给`Supplier`接口：

```java
String hello = "Hello";
Supplier<Integer> lenSupplier = hello::length; //当前hello变量的值将被固化在lenSupplier中
System.out.println(lenSupplier.get()); //结果为5
hello = "Hello New Java!";
System.out.println(lenSupplier.get()); //结果为5
```

> 注意对`hello`变量的重新赋值并不影响`lenSupplier`的结果，这就是`Java`中的典型闭包。

另外为了让生产者更容易生成`Java`原生类型的数据，如`int`、`long`、`double`和`boolean`等，`Java`提供了相应的`IntSupplier`、`LongSupplier`、`DoubleSupplier`和`BooleanSupplier`等。这样做的目的是避免原始类型的自动拆装箱。

> 如`int`类型的变量需要自动装箱为`Integer`类型，一般情况下`Java`的自动拆装箱对性能的影响可以忽略，但是如果自动拆装箱在代码中过多发生时也会成为性能的负担。

#### 消费者接口 — Consumer

`Consumer`可翻译为`消费者`，其用来消费数据，四个字描述“只吃不拉”。`Consumer`接口中的函数方法为`void accept(T t);`。

```java
Consumer<Object> writeToFile = str -> {
    try {
        Files.writeString(Path.of("test.txt"), str.toString());
    } catch (IOException e) {
        e.printStackTrace();
    }
};
Consumer<Object> consumer = System.out::println;
consumer.accept(1);
consumer.accept("hello");
```

`Consumer`中还有一个默认方法为`andThen`，这样就可以让不同的消费者同时去消费同一个数据，如:

```java
consumer.andThen(writeToFile).accept("hello");
```

集合的迭代器`Iterable`中提供的`forEach`方法使用`Consumer`接口做为其参数：

```java
//Iterable接口中forEach函数的实现
default void forEach(Consumer<? super T> action) {
    Objects.requireNonNull(action);
    for (T t : this) {
        action.accept(t);
    }
}

//forEach函数的使用
List.of(1, 3, 5).forEach(consumer);
```

而`Stream`中也定义了`forEach`方法：

```java
Stream.of(1, 3, 5).forEach(consumer);
```

为了处理输入参数为原始类型（避免原始类型的自动拆装箱）的情况，`Java`中定义了`IntConsumer`、 `DoubleConsumer`和`LongConsumer`等。

`Java`中还定义了`BiConsumer`函数式接口来消费两种类型的数据，其提供的函数式方法是`void accept(T t, U u);`，读者也可以定义自己的消费者函数式接口来消费更多类型的数据。

#### 断言接口 — Predicate

`Predicate`一般被翻译为`谓语`，但笔者认为在`Java`中，翻译成`断言`更好理解。`Predicate`接口中的函数（断言）方法为`boolean test(T t)`。

```java
Predicate<Integer> isEven = num -> 0 == num % 2;
Predicate<String> isUppercase = str -> str.equals(str.toUpperCase());
Predicate<String> isEmpty = str -> Objects.isNull(str) || 0 == str.length();
Predicate<Boolean> negate = b -> !b;
```

`Predicate`接口往往代表着逻辑表达式，逻辑表达式经常是需要组合在一起的。根据上面的几个函数，如何满足需求`判断字符串不为空也不是大写字符串`呢？

```java
negate.test(isEmpty.test(str)) && negate.test(isUppercase.test(str))
```

上面的写法其实是比较繁琐且难理解，换一种写法会好很多：

```java
isEmpty.negate().and(isUppercase.negate()).test(str)
```

- `Predicate`接口实现了默认的`negate()`方法，其返回的是另一个`Predicate`接口:

  ```java
  default Predicate<T> negate() {
  		return (t) -> !test(t);
  }
  ```

- `Predicate`接口实现了默认的`and`方法，其返回的是另一个`Predicate`接口：

  ```java
  default Predicate<T> and(Predicate<? super T> other) {
      Objects.requireNonNull(other);
      return (t) -> test(t) && other.test(t);
  }
  ```

- `Predicate`接口还实现了默认的`or`方法，其返回的是另一个`Predicate`接口：

  ```java
  default Predicate<T> or(Predicate<? super T> other) {
      Objects.requireNonNull(other);
      return (t) -> test(t) || other.test(t);
  }
  ```

有了`negate`、`and`和`or`方法，`Predicate`接口就可以链式调用来将不同的断言函数组合到一起，变成新的函数。

如果`Predicate`接口的默认函数不能满足使用需求，也可以定义`Predicate`的子接口来覆盖默认函数的实现，或直接使用：

```java
Predicate<String> customPredicate = new Predicate<String>() {
    @Override
    public boolean test(String s) {
        return s.equals(s.toUpperCase());
    }

    @Override
    public Predicate<String> and(Predicate<? super String> other) {
        return t -> other.test(t) && test(t);
    }
};
assertFalse(customPredicate.and(Objects::nonNull).test(null));
```

`Predicate`接口在`Stream`中被用来作为过滤条件：

```java
Stream.of(1, 2, 3, 4, 5)
        .filter(num -> num > 3)
        .filter(num -> num % 2 != 0)
        .forEach(System.out::println);
```

> `filter`函数的参数即为`Predicate`接口

`Java`中还定义了`BiPredicate`函数式接口，其提供了函数方法`boolean test(T t, U u);`来接收两个不同类型的参数值，读者也可以自行定义能够处理更多参数输入的`Predicate`接口。

另外，为了处理输入参数为原始类型（避免原始类型的自动拆装箱）的情况，`Java`中定义了`IntPredicate`、`DoublePredicate`和`LongPredicate`等。

#### 函数接口 — Function

`Function`可翻译为数学里的`函数`，其根据根据输入计算出输出，四个字描述“一元方程”。`Function`接口的函数式方法为`R apply(T t);`。

```java
Function<Integer, Integer> add = a -> a + 1;
Function<String, Integer> parseIt = Integer::parseInt;
System.out.println(add.apply(2));
System.out.println(parseIt.apply("12"));
```

`Function`接口提供了默认方法`compose`和`andThen`，通过这两个方法可以很方便的将函数组合到一起。

```java
System.out.println(parseIt.andThen(add).apply("12")); //结果为13
System.out.println(add.compose(parseIt).apply("12")); //结果为13
```

> `andThen`将使用当前函数处理后的结果再使用参数中的函数来处理；而`compose`恰好相反，参数中的函数处理后的结果再使用当前函数来处理。

`Function`接口中还提供了一个静态的`identity`方法，其将返回一个函数，输入和输出是相等/相同的:

```java
static <T> Function<T, T> identity() {
    return t -> t;
}
```

> 当函数成为一个变量时，我们就不能使用`null`作为其默认值，应该使用`Function.identity()`作为其默认值。

`Function`接口在`Stream`中被用来作为`map`函数的参数：

```java
Stream.of(1, 2, 3, 4, 5)
        .map(Math::sqrt)
        .map(Double::intValue)
        .forEach(System.out::println);
```

为了处理输入参数为原始类型（避免原始类型的自动拆装箱）的情况，`Java`中定义了`DoubleToIntFunction`、 `IntToLongFunction`、 `LongToDoubleFunction`等。

#### 操作符 — Operators

`Operator`族的各个接口是`Function`族的相应接口的子接口，当输入参数类型和输出结果类型相同时，用`Function`写起来略显啰嗦，如：

```java
Function<Integer, Integer> increment = x -> ++x;
```

用一元操作符`UnaryOperator`写起来就略好一些：

```java
UnaryOperator<Interger> increment = x -> ++x;
```

`UnaryOperator`做到这一点也很简单：

```java
@FunctionalInterface
public interface UnaryOperator<T> extends Function<T, T> {
  	static <T> UnaryOperator<T> identity() {
        return t -> t;
    }
}
```

`Java`提供了许多的`Operator`:

- `BinaryOperator`接口继承自`BiFunction`接口
- `IntUnaryOperator`、`DoubleUnaryOperator`和`LongUnaryOperator`等一元操作符的输入输出均为相应的`Java`原始类型
- `IntBinaryOperator`、`DoubleBinaryOperator`和`LongBinaryOperator`等二元操作符的输入输出也均为相应的`Java`原始类型

`Stream`中的`reduce`方法就是用`BinaryOperator`作为其参数，这里以`BinaryOperator`提供了两个非常有用的静态方法`minBy`和`maxBy`为例来获取列表中的最大值:

```java
Integer min = List.of(3, 2, 1, 5, 4)
        .stream()
        .reduce(BinaryOperator.minBy(Comparator.naturalOrder()))
        .get();
System.out.println(min); //返回1

Integer max = List.of(3, 2, 1, 5, 4)
        .stream()
        .reduce(BinaryOperator.maxBy(Comparator.naturalOrder()))
        .get();
System.out.println(max); //返回5
```

> `minBy`和`maxBy`的实现其实比较复杂，其接收一个函数作为一个参数，将返回另一个函数。关于高阶函数的详细介绍可查看第六章第一节。

#### 自定义函数式接口

