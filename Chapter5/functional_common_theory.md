# 第5节：函数式编程常用术语

通过对前面的章节的学习，读者应该已经了解了Java语言中的`Stream`、函数式接口和`Lambda`表达式。基于对这些具体实现的了解，也许我们会更容易理解一些常用的函数式编程的常用术语。

##### 不可变性

函数式编程的最基本特性之一是不可变性（`Immutable`)。对于一个值会对象，如果其被构建后，任何操作都不会使得其值或状态发生变化，则可以视其为不可变的。在`Java`语言中，典型的不可变类如`String`类：

- `String`类提供的API接口（如`concat`、`replace`等）都不会改变原有对象状态（对象的成员变量值）。

  ```java
  @Test
  void testImmutableString() {
      String s = new String("Hello");
      s.concat("world");
      assertEquals("Hello", s);
  
      s.replace("H", "h");
      assertEquals("Hello", s);
  }
  ```

  `concat`和`replace`方法会生成新的`String`对象，而不是改变原来的对象。

- `String`类实现不可变性的方式是将其成员变量定义为`final`，甚至类也是定义为`final`的:

  ```java
  public final class String implements java.io.Serializable, Comparable<String>, CharSequence {
      private final byte[] value;
      ...
  }
  ```

不可变类的状态不会发生改变，因而不会出现两个线程同时修改不可变类状态的情况，因而其是线程安全的。Java语言中也提供了`@Immutable`注解可以用来修饰一个不可变类。

Java9中还引入了`ImmutableCollections`类，其定义了一些`List`、`Set`以及`Map`相关的不可变类，同时也定义了构建相关不可变对象的工厂方法。由于`ImmutableCollections`类是非`public`的，因而其只能被`java.util`包下的类调用，`List.of`、`Set.of`、`Map.of`都是调用`ImmutableCollections`类的工厂方法生成不可变的集合对象：

```java
List.of(1, 2, 3);
Set.of(1, 2, 3);
Map.of("key1", "value1", "key2", "value2");
```

这些集合的不可变简单粗暴，调用`add`、`remove`等操作都会抛出`UnsupportedOperationException`异常。

##### 无副作用的纯函数

在Java中，我们把一个`Lambda`表达式，或者一个类/对象的方法看做一个函数。一般来说，函数由传入参数、计算逻辑和输出结果三部分组成。函数计算逻辑中有副作用的操作：

- 修改了传入参数
- 修改了非传入参数的其他状态值（如类的成员变量值）
- 抛出了异常
- 打印到终端或读取了用户输入
- 读取或写入了文件

纯函数就是无副作用的函数，其从外界获取信息的渠道只有参数，而输出信息到外界的方式是返回值。纯函数是线程安全的，同时其对于单元测试来说也是非常友好的。

##### 闭包

在百度百科中定义闭包（`Closure`）：

```java
闭包就是能够读取其他函数内部变量的函数。
```

在Java语言中，我们看一个闭包的例子：

```java
@Test
void testClosure() {
    final int increment = 1;
    UnaryOperator<Integer> add = num -> num + increment;
    assertEquals(2, add.apply(1));
}
```

  `add`函数将外部的变量`increment`的值`1`固化在了自己的函数计算逻辑里。

##### 高阶函数

对于一个函数，如果满足下面条件中的任意一个都可以看做是高阶函数：

- 接收一个或多个函数作为输入参数，如`Stream`接口的`map`、`flatMap`和`filter`等

- 输出结果是一个函数，如：

  ```java
  @Test
  void testHighOrderFunction() {
      Function<Integer, UnaryOperator<Integer>> addN = n -> num -> num + n;
      UnaryOperator<Integer> add1 = addN.apply(1);
      assertEquals(2, add1.apply(1));
  
      UnaryOperator<Integer> add2 = addN.apply(2);
      assertEquals(3, add2.apply(1));
  }
  ```

  这里的`addN`函数就是一个高阶函数，其接收一个整数作为参数，然后返回的是一个`UnaryOperator`类型的函数式接口。

两个条件都满足，接收一个函数作为参数，返回一个函数作为结果的高阶函数：

```java
private static <T,R> Function<T, R> logRecordWrapper(Function<T, R> wrappedFunction) {
    System.out.print("Function: " + wrappedFunction + " will be called!");
    return wrappedFunction;
}
```

##### 柯里化

柯里化（`Currying`）是把接受多个参数的函数变换成接受一个单一参数(最初函数的第一个参数)的函数，并且返回接受余下的参数且返回结果的新函数的技术。

```java
private static <T,U,R> Function<T, Function<U, R>> curring(BiFunction<T,U,R> biFunction) {
    return t -> u -> biFunction.apply(t, u);
}

@Test
void testCurring() {
    BinaryOperator<Integer> add = (a, b) -> a + b;
    assertEquals(3, curring(add).apply(1).apply(2));
}
```

对于一些已经定义好的高阶函数，如`Stream`对象的`map`、`flatMap`，其只接收一个参数的函数，这时如果我们需要传入已经定义好的两个参数的函数，则可以考虑用柯里化的方式将其转化成一个参数的函数：

```java
@Test
void testCurringWithMap() {
    BinaryOperator<Integer> add = (a, b) -> a + b;
  	//柯里化add函数之前的写法
    List<Integer> lst = Stream.of(1, 2, 3)
            .map(num -> add.apply(num, 1))
            .map(num -> num * num)
            .map(num -> add.apply(num, 2))
            .collect(Collectors.toList());
    assertEquals(List.of(6, 11, 18), lst);

    Function<Integer, Function<Integer, Integer>> curring = curring(add);
    //柯里化add函数之后的写法
    lst = Stream.of(1, 2, 3)
            .map(curring.apply(1))
            .map(num -> num * num)
            .map(curring.apply(2))
            .collect(Collectors.toList());
    assertEquals(List.of(6, 11, 18), lst);
}
```

##### 单子

单子（`Monad`）在函数式编程里是一个比较晦涩的词，非常难于理解。







