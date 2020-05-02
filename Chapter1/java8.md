# 第1节：Java8新特性概要

JDK8是在2014年3月19日发布的，以目前时间2020年4月计算，已经过去了六年多，但是还是有一些公司在使用JDK7甚至JDK6。而有很多公司公司，虽然升级到了JDK8以上的版本，但是开发人员还是很少去使用JDK8提供的一些新特性。

笔者接触过一些Java开发人员，在2020年的今天，还只知道Java Lambda却从未在项目中使用过，潜意识中还会觉得”用Lambda会导致程序性能会比较差”。一些Java开发人员从来没有接触也没有学习过Stream, for/while循环还是其对集合进行操作的默认选择。当你和这些Java开发人员谈起用Java进行函数式编程时，他们更是一脸懵懂。

实际上，JDK8中引入的一些新特性给Java带来了革命性的变化，以一个Java程序员的基本职业素养，需要去了解、熟悉以及日常使用这些新特性，精进自己的Java编程技艺。闲话少叙，让我们介绍下Java8中的新特性，如果你对Java8的新特性已经了如指掌，可直接跳过这一节。

> 本节的内容是让读者快速的了解和入门Java8的新特性，而关于Java8新特性更详细更高级的介绍在第五章和第六章。

## 函数式接口与Lambda表达式

在介绍Java8的函数式接口和Lambda之前，这里想先展示一种Javascript中的函数定义的方式，不使用`function`关键字定义一个`add`函数：

```javascript
var add = (a,b) => a + b;
```

这个函数定义其实和下面使用`function`关键字定义的`add`函数是一样的：

```javascript
function add(a, b) {
  return a + b;
}
```

对add函数的调用很简单，使用如`add(1,2)`的方式即可。Javascript是弱类型的语言，所以这里并不需要给出`a`和`b`以确定的类型，也不需要明确给出返回值的类型。所以在Javascript中很容易理解这两种函数定义是等价的。

Java作为一种强类型语言，如果我们也期望像Javascript一样，用一行代码简洁的定义一个函数呢？Java8让这种想法成为可能。

```java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
```

这里有两个知识点：

- `(a, b) -> a + b`就是Java的Lambda表达式，这里的Lambda表达式本身并没有携带任何类型相关信息。
- `BiFunction`是函数式接口，意味着这这一条Java语句中，Lambda表达式的类型就是`BiFunction`这个函数式接口。

当然这个Lambda表达式也可以赋值给`合适`的其他函数式接口，关于如何自定义一个合适的类似的接口，是我们稍后会讨论的内容，目前让我们聚焦在`BiFunction`的定义上，`BiFunction`是Java8提供的一个函数式接口，定义如下:

```java
@FunctionalInterface
public interface BiFunction<T, U, R> {
  R apply(T t, U u);
}
```

除了多了个注解`@FunctionalInterface`和使用了泛型，其与我们常见的Java接口并无任何不同。这个接口里定义了一个方法`R apply(T t, U u)`给出了这个函数式接口的调用方式。所以上面我们定义的`add`方法的调用方式就是`add.apply(1,2)`。照葫芦画瓢，我们可以定义一个自己的接口来成为Lambda表达式`(a, b) -> a + b`的类型，如定义一个`MyFunctionInterface`:

```java
interface MyFunctionInterface<T> {
  T calculate(T p1, T p2);
}
MyFunctionInterface<Integer> add = (a, b) -> a + b;
```

这里对add函数的调用就变成了`add.calculate(1, 2)`。注意这里的`MyFunctionInterface`并没有添加任何注解，是因为`@FunctionalInterface`只是一个信息注解。那函数式接口与普通接口有何不同呢？其实函数式接口只是普通接口的一个子集，一种特例，怎么个`特`法呢，就是函数式接口只应含有一个抽象方法。含有两个以上的抽象方法的接口为什么不能变成函数式接口呢，举个例子，定义一个有两个抽象方法的接口`TwoAbstractMethodInteface`:

```java
interface TwoAbstractMethodInteface<T, U, R> {
  T calculate(T p1, T p2);
  R apply(T t, U u);
}
TwoAbstractMethodInteface<Integer, Integer, Integer> add = (a, b) -> a + b; //编译出错
```

如果`TwoAbstractMethodInteface`允许成为Lambda表达式`(a, b) -> a + b`的类型的话，怎么来调用`add`函数呢，是`add.calculate(1, 2)`还是`add.apply(1, 2)`，还是都可以？为了避免这种二（多）义性的产生，函数式接口就索性约定了只能有一个抽象方法。

除了可以将Lambda表达式赋值给函数式接口类型，我们还可以将类的函数赋值给函数式接口类型，这里以静态方法作为一个例子：

```java
static Integer staticCalculate(Integer p1, Integer p2) {
  return p1 + p2;
}
MyFunctionInterface<Integer> add2 = TestFunctionInterface::staticCalculate;
```

函数式接口的强大功能注解显露出来，那么Java8中定义了哪些常用的函数式接口呢？在`java.util.function`包下，有40多个定义好的函数式接口，我们先记住如下四个常用函数式接口，后面的章节中我们会频繁使用这些接口：

```java
public interface Predicate<T> {
	boolean test(T var1);
}
public interface Function<T, R> {
  R apply(T t);
}
public interface Consumer<T> {
  void accept(T t);
}
public interface Supplier<T> {
  T get();
}
```

> 小问题：
>
> 1. System.out::println可以看做是上面四个函数式接口的哪一个接口类型呢？
>
> 2. String::new可以看做是哪个接口类型？



## 接口的default方法

当函数式接口被期望越来越多的被大量开发人员使用时，接口就被赋予了更多能量，很多之前抽象类才有的能力也赋予给了接口，虽然表现形式上略有不同。

个人直觉，是因为函数式接口对于default方法的刚性需求，才促成接口支持default方法的想法落地，进而打开全面赋能接口的大门。为什么说是刚性需求呢，这里需要先简单回顾下关于`函数复合运算`的数学基础知识，关于函数的复合运算的定义：

> 设有定义在由集合A到集合B的函数，f: A -> B和定义在集合B到集合C上的函数g: B -> C，则f和g的复合函数是一个集合A到集合C的函数，记为g.f: A -> C (或记为gf: A -> C)

如果觉得上面的定义有点抽象，更接地气的描述是：有两个函数，F函数`f(x)`和G函数`g(y)`，我们可以把F函数和G函数组合到一起变成复合函数`f(g(x))`或者`g(f(x))`，当然这两个复合函数计算的结果将是完全不同的。举个例子：

- f(x) = x + 1
- g(y) = y * y
- f(g(x)) 则是`x * x + 1`，假设输入x的值为2，则使用复合函数`f(g(x))`得到的结果是`5`
- g(f(x)) 则是`(x+1) * (x+1)`，假设输入x的值为2，则使用复合函数g(f(x))得到的结果是`9`

Java8提供了函数式接口，提供`函数复合运算`的支持也成为理所当然，但是怎么能让函数式接口能够`自然而然`的支持函数复合运算？绞尽脑汁，接口default方法横空出世（见`Function`接口）：

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

default方法的强大之处在于一个赋值操作就能给一个函数赋予更多的潜在能力。`a -> a + 1`Lambda表达式赋值给`Function<Integer, Integer>`类型后，就拥有了`compose`和`andThen`方法。

```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = y -> y * y;
Function<Integer, Integer> fg = f.compose(g); // f函数自动拥有了compose方法，fg函数即为f(g(x))
Function<Integer, Integer> gf = f.andThen(g); // f函数自动拥有了andThen方法, gf函数即为g(f(x))
assertTrue(5 == fg.apply(2));
assertTrue(9 == gf.apply(2));
```

有了接口中的default方法，我们就可以给任何一个Lambda表达式或者函数赋予各种能力。而复合函数可以把我们的各种功能的函数组合在一起，形成一个新的函数。

## Stream与集合操作

日常编程中，程序员需要经常对集合（List、Queue、Map、Tree）类型的数据进行操作。太多程序员已经习惯了起手就是for循环，如打印一个列表:

```java
List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
for (Integer integer : integers) {
  System.out.println(integer);
}
```

看着也挺`顺眼`，有没有可以优化的空间，当然是有的，基于我们已经有了的函数式基础，我们可以使用 `Iterable`接口（`List`接口的祖父接口）提供的`forEach`方法：

```java
integers.forEach(System.out::println);
```

需求变更是我们软件开发过程中经常遇到的事情，弹指间，`打印一个列表`的需求变成了`打印列表中的偶数`。某些程序员已经摩拳擦掌开始写`if`语句了:

```java
Predicate<Integer> isEven = num -> 0 == num % 2;
for (Integer integer : integers) {
  if (isEven.test(integer)) {
    System.out.println(integer);
  }
}
```

还有一些程序员获得了`forEach`的启发，在尝试着用Lambda表达式来实现:

```java
Predicate<Integer> isEven = num -> 0 == num % 2;
Consumer<Integer> consumer = integer -> {
  if (isEven.test(integer)) {
    System.out.println(integer);
  }
};
integers.forEach(consumer);
```

而另一些`古灵精怪`的程序员陷入了思考，因为一些极妙的思维的火花在脑海里迸发了出来：

```java
//火花1
integers.forEach(isEven.andThen(System.out::println));

//火花2
Predicate<Integer> isEven = num -> 0 == num % 2;
integers.filter(isEven).forEach(System.out::println)
```

可惜，编译器摁灭了这些火花：”火花1“中`andThen`方法未定义，”火花2“中`filter`方法未定义。我们开始有很多疑问，譬如为什么`Iterable`提供了`forEach`方法却不提供`filter`方法呢？难不成要自己实现一套集合，造个轮子！未尝不可以造轮子，但是先了解以下Java8提供的Stream来怎么满足这个需求的:

```java
integers.stream()
  .filter(isEven)
  .forEach(System.out::println);
```

除了多了一个对`stream`方法的调用，已经算完美契合我们的想法了。那么这个`stream`方法究竟做了什么事情呢，直接看源代码：

```java
default Stream<E> stream() {
  return StreamSupport.stream(spliterator(), false);
}
```

源代码来自`Collection`接口（`List`接口的父接口，`Iterable`接口的子接口）。先不管`stream`方法是如何实现的，我们看到了返回值是一个`Stream`接口，而这个`Stream`接口中，我们看到了很多方法，`filter`方法和`forEach`方法的声明就在其中：

```java
Stream<T> filter(Predicate<? super T> predicate);
void forEach(Consumer<? super T> action);
```

而老练的Java程序员对于接口的`尽可能单一职责`应是熟记于心，`Stream`接口怎么就有这么多看似毫不相干的方法。这里需要引用一句源自`Stream`接口文档的一句话：

> A sequence of elements supporting sequential and parallel aggregate operations.
>
> 支持串行和并行聚合操作的一个元素序列

是不是很抽象，先看两个例子：

```java
Predicate<Integer> isEven = num -> 0 == num % 2;
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = y -> y * y;
//串行操作
Integer sum = integers.stream()
  .filter(isEven)
  .map(f)
  .map(g)
  .reduce(0, Integer::sum);

//并行操作
Integer parallelSum = integers.stream().parallel()
  .filter(isEven)
  .map(f)
  .map(g)
  .reduce(0, Integer::sum);

assertEquals(sum, parallelSum); //sum与parallelSum是相等的
```

这两个例子包含了很多知识点，有显示的知识点，也有隐含的知识点：

- 列表生成`Stream`后就可以进行链式(Pipeline)运算了
- `map`操作的链式调用方式可以等同于复合函数，即`.map(f).map(g)`等同于`.map(f.andThen(g))`，但是显然`.map(f).map(g)`的代码更清晰明了
- `reduce`操作可以进行聚合操作，这里是将一个列表通过`Integer::sum`方法进行计算聚合
- `Stream`对并行操作的支持非常方便，只需要额外调用一个`parallel`方法
- 对集合的`stream`方法调用以及后续的一系列`Stream`操作都不会修改源数据`integers`
- `filter`方法和`map`方法返回的仍然是`Stream`接口，可以定义其为`Stream`接口的中间方法
- `reduce`方法返回的是`Integer`类型的结果而不是`Stream`接口，可以定义其为`Stream`接口的终止方法
- `Stream`是惰性的，只有在执行了终止操作时才会执行计算，这点也许有点不太好理解，但是能够理解为上面介绍的复合函数，也就能够理解一定可以有一套合理的底层机制来保障惰性执行的正确性

- `Stream`终止方法被调用后，当前Stream就会被回收，回收的Stream上的任何操作都是错误的

> 小提示：我们自己在创建一些需要支持链式调用的类或接口时，可以参考Stream接口的设计方式，区分中间方法和终止方法。

## Optional接口

如果做一个调查，关于Java程序员遇到最多的异常，我猜测肯定是`NullPointerException`。

不管是哪种语言，空指针异常真是一个让人厌烦的存在，而Go语言中比较极端，处处要检查错误，举个从Map中读取数据的Go语言的例子：

```go
var countryCapitalMap map[string]string
countryCapitalMap = make(map[string]string)
countryCapitalMap [ "China" ] = "中国"
capital, ok := countryCapitalMap [ "NOT_EXIST" ]
if (ok) {
  fmt.Println("首都是", capital)
} else {
  fmt.Println("首都不存在")
}
```

那么是否有更优雅的方式来解决这个空指针异常的问题呢？让我们把目光聚焦到Optional接口：

```java
HashMap<String, String> countryCapitalMap = new HashMap<>();
countryCapitalMap.put("China", "北京");

String s = Optional.of("NOT_EXIST")
  .map(countryCapitalMap::get)
  .map(c -> "首都是" + c)
  .orElse("首都不存在");
System.out.println(s); //将打印出"首都不存在"
```

基于这个例子分享的一些知识点：

- 可以通过静态方法`Optional.of`来创建一个Optional对象

- `Optional`的`map`函数如果接收到的值不存在，返回一个空的`Optional`对象；否则根据提供的函数进行计算，结果为空则返回一个空的`Optional`对象，不为空则返回一个带有计算值的`Optional`对象。关于`空Optional对象`:

  ````
  private static final Optional<?> EMPTY = new Optional<>();
  ```

- `OrElse`方法是`Optional`类的终止方法，如果值存在则返回当前值，如果值不存在就返回`OrElse`方法传入的参数。

- `Optional`类还其他一些非常有用的方法，如`filter`、 `ifPresent`、 `isPresent`、 `orElseGet`、 `orElseThrow`，现在就有兴趣的读者可以直接开始对这些方法的尝试和学习。

## 时间处理

Java8中重写了（借鉴了jodo-time）一套时间处理的类。比起以前的时间处理，好用了许多。时间处理是一个看似简单，实则很复杂的事情。复杂的时区、部分国家夏令时机制等。

Java8中的时间相关API使用的默认日历系统是[ISO-8601](http://www.iso.org/iso/home/standards/iso8601.htm)，这个日历系统是基于格里高利（Gregorian）日历系统，格里高利日历系统是大多数国家使用的标准日历系统，在维基百科中的定义：

```wiki
The calendar spaces leap years to make the average year 365.2425 days long, approximating the 365.2422-day tropical year that is determined by the Earth's revolution around the Sun. The rule for leap years is:

Every year that is exactly divisible by four is a leap year, except for years that are exactly divisible by 100, but these centurial years are leap years if they are exactly divisible by 400. For example, the years 1700, 1800, and 1900 are not leap years, but the years 1600 and 2000 are.

这个日历将将闰年隔开，这样每年平均有365.2425天，约等于一个太阳年-365.2422天。
闰年：能被4整除的年份，但同时要去除能被100整除但不能被400整除的年份。
```



Java8中对于时区的处理非常方便：

```java
//获取所有的支持的时区
ZoneId.getAvailableZoneIds().forEach(System.out::println);

//获取一个时区
ZoneId australiaZone = ZoneId.of("Australia/Victoria");

//创建一个带时区的时间，参数分别代表年，月，日，小时，分钟，秒，纳秒，时区
ZonedDateTime zonedDateTime = ZonedDateTime.of(2020, 1, 20, 0 , 0, 0, 0, australiaZone);

//判断是否是夏令时（中国曾经有使用过夏令时，但目前已经不再使用夏令时）
asaustraliaZone.getRules().isDaylightSavings(date.atZone(australiaZone).toInstant()); //true
```

Java8中比较容易混淆的是对`Instant`、`LocalDateTime`和`ZoneDateTime`的理解。

- `Instant`代表即时时间点，但是其本身没有时区信息，可以精确到纳秒，其内部是由两个Long字段组成，第一个部分保存的是自标准Java计算时代（就是1970年1月1日开始）到现在的秒数，第二部分保存的是纳秒数。如果在中国的你现在和一个美国人在聊天，在同一个时刻，在他电脑上获取的`Instant.now()`和在你电脑上获取的`Instant.now()`是完全一样的。

  ```java
  //末尾的'Z'表示是一个世界标准时间，我们可以按0时区的时间来理解，但其本身不带时区信息
  Instant instant = Instant.parse("2020-03-21T08:47:22.757Z");
  instant.getEpochSecond(); //1584780442
  instant.getNano(); //757000000
  Instant instant2 = instant.plusSeconds(1000); //当前时间将来的1000秒后的时间
  Instant instant3 = instant.plus(Duration.ofHours(8));
  Instant instant4 = instant.plus(Period.ofDays(1));
  
  //Formatter必须带有时区信息，不带失去信息的话会由于Formatter无法正常理解这个时间而抛出异常
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
  formatter.format(Instant.now()); //2020-03-21 16:47:22
  ```

  > `Instant`类和`DateTimeFormatter`类都是不可变的（Immutable）、线程安全的类。关于什么是不可变类，可以参考`String`类，`String`类就是一个不可变类

- `LocalDateTime`中的`Local`不应理解为本地，应表示的是`Locality`，意思是任何地点。其不带有时区信息，意味不同时区的人拿到LocalDateTime时会导致对这个信息理解不一致，举个例子，在中国的你跟一个在美国的人约在今天晚上八点`LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0))`线上聊天，你俩估计今天晚上大概率是碰不着面了。因为他和你所在的时区不同，你的晚上八点和他的晚上八点相对0时区的时间来说是不同的。

  ```java
  LocalDateTime dateTime = LocalDateTime.parse("2020-03-21T16:47:22.757");
  dateTime.getYear();	//2020
  dateTime.getDayOfYear(); //这一年的第81天
  dateTime.getDayOfWeek(); //星期六，DayOfWeek.SATURDAY
  
  LocalDateTime dateTime2 = dateTime.plus(Period.ofWeeks(1));
  LocalDateTime dateTime3 = dateTime.plusDays(1);
  LocalDateTime dateTime4 = dateTime.plusSeconds(1000);
  
  //Formatter可以不带时区信息，将使用系统的默认时区进行格式化
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  formatter.format(dateTime); //2020-03-21 16:47:22
  ```

  > `LocalDateTime`也是不可变的、线程安全的类

- `ZoneDateTime`表示的是带时区的日期时间。不同时区的人通过`ZoneDateTime`可以得到对某个时间点的一致理解。

  ```java
  ZonedDateTime zonedDateTime = ZonedDateTime.parse("2020-03-21T16:47:22.757+08:00[Asia/Shanghai]");
  zonedDateTime.getZone(); //Asia/Shanghai, ZoneId
  zonedDateTime.getOffset(); //+08:00, ZoneOffset
  zonedDateTime.getDayOfWeek(); //星期六，DayOfWeek.SATURDAY
  zonedDateTime.getMonth(); //三月份，Month.MARCH
  
  //当前时间增加1年1个月1天
  ZonedDateTime zonedDateTime2 = zonedDateTime.plus(Period.of(1, 1, 1));
  ZonedDateTime zonedDateTime3 = zonedDateTime.plusDays(1);
  ZonedDateTime zonedDateTime4 = zonedDateTime.plusSeconds(1000);
  
  //Formatter可以不带时区信息，将使用ZonedDateTime中包含的时区进行格式化
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  formatter.format(zonedDateTime); //2020-03-21 16:47:22
  ```

  > `ZoneDateTime`也是不可变的、线程安全的类

`Instant`、`LocalDateTime`和`ZoneDateTime`三者之间可以互相转换：

```java
//Instant转LocalDateTime和ZoneDateTime
Instant instant = Instant.now();
LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

//LocalDateTime转Instant和ZoneDateTime
LocalDateTime dateTime2 = LocalDateTime.now();
Instant instant2 = dateTime2.toInstant(ZoneOffset.of("+8"));
ZonedDateTime zonedDateTime2 = dateTime2.atZone(ZoneId.systemDefault());

//ZonedDateTime转Instant和LocalDateTime
ZonedDateTime zonedDateTime3 = ZonedDateTime.now();
Instant instant3 = zonedDateTime3.toInstant();
LocalDateTime dateTime3 = zonedDateTime3.toLocalDateTime();
```

Java8中提供了一些其他时间操作的其他类，如`LocalDate`和`LocalTime`，这里不再详述，有兴趣的读者可以自行搜索查看相关文档。

> 有了Java8中提供的对日期时间的处理，建议Java开发人员不要再使用以前Java版本中的Date, Calendar, SimpleDateFormat了。

## 其他

除了前面介绍的特性，Java8还引入了很多其他的特性和改进，譬如对JavaFX的很多改进、引入了命令行工具`jdeps`，`Security`的加强等，如果对其他引入/改进的内容感兴趣，可移步Oracle的官方网站 [What's New in JDK 8](https://www.oracle.com/technetwork/java/javase/8-whats-new-2157071.html )去学习。

