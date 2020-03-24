# 第2节：Junit5基础知识

这一节主要介绍Junit5使用的一些细节，具体来说分为两大块`注解`，`断言`，这两大块之外的内容，笔者认为有必要介绍的都放到了`其他`。

## 常用注解

### @Test与@ParameterizedTest

`@Test`注解的方法就是一个测试用例，但是需要注意的是这个方法不能是`static`方法或`private`方法。注意的是`@Test`的注解只支持在实例方法和其他注解类型上，且此注解本身没有任何的属性可以设置。上一节的测试用例都使用了此注解。

```java
@Test
void testCapitalAndLowercaseRest() {
  assertEquals("Hello", capitalAndLowercaseRest("  hELLO  "));
}
```

`@ParameterizedTest`参数提供了一种好用的测试代码复用的方式，通过注解和参数化的方式使得一个测试方法可以测试多个输入：

```java
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
```

`@ValueSource`虽然用起来很方便，但是有一个缺点是定义`null`和空值时并没有那么直观，所以在使用`@ValueSource`的同时，添加注解如`@NullSource`、`@EmptySource`、`@EmptySource`。

除了上面例子里给出的`@CsvSource`, `@ValueSource`和`@EnumSource`。更强大的注解如`@MethodSource`能够自定义产生输入参数的工厂方法：

```java
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
```

> 本章的测试用例大都来自于[Junit5的官方文档](https://junit.org/junit5/docs/current/user-guide/#writing-tests-annotations)

### @BeforeAll、@AfterAll、@BeforeEach、@AfterEach

`@BeforeAll`注解的方法必须是静态方法，在此测试类执行第一个测试用例之前执行的方法，当前测试类中测试用例中一些公用的成本比较高的操作都可以在这里调用。

`@AfterAll`注解的方法也必须是静态方法，在此测试类执行最后一个测试用例之后执行，一般用于释放`@BeforeAll`中创建的资源。

`@BeforeEach`是测试类中每个测试用例执行前需要执行的方法。

`@AfterEach`是测试类中每个测试用例执行后需要执行的方法。

### @Tag与@Tags

`@Tags`和`@Tag`注解给测试用例以标签，通过标签可以对测试用例进行分类，通过标签可以在执行时根据规则过滤测试用例。基于`@Test`和`@Tag`自定义一个注解：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("api")
public @interface ApiTest {
}
```

有了这个自定义注解后，就可以在测试类中使用`@ApiTest`这个注解了：

```java
@Tags(value = {
        @Tag("regression"),
        @Tag("core")
})
class CustomStringUtilsTest {
    @ApiTest
    void testCustomTestAnnotation() {
        assertEquals("abc", "a".concat("bc"));
    } 
}
```

关于如何使用标签来过滤测试，笔者使用的是`Gradle`，需要添加点自定义逻辑:

```groovy
test {
    String itags = System.getProperty("includeTags");
    String etags = System.getProperty("excludeTags");
    useJUnitPlatform{
        if (itags) {
            includeTags  itags
        }
        if (etags) {
            excludeTags  etags
        }
    }
}
```

使用`gradle test`时就能很方便的过滤测试用例了：

```shell
gradle clean test -DincludeTags='core' -DexcludeTags='api'
```

### 其他注解

` @TestTemplate`和`@TestFactory`可以动态生成测试用例。

`@DisplayName`自定义单元测试的描述。

`@Nested`可以注解在测试类的内部类，利用好`@Nested`和内部类可以很好的对大的测试类进行分层。

`@Disabled`注解意味着执行测试时会跳过这个测试用例。

`@Timeout`注解设置测试执行的超时时间，对于耗时的计算或等待，此注解可以避免无限等待。

`@TempDir`可以用来注解测试方法的参数或者测试类成员变量，在测试时生成临时目录，可用于文件操作相关的测试用例。

## 常用断言

### assertEquals、assertSame、assertArrayEquals、assertIterableEquals

`assertEquals`方法是被大量使用的断言，其判断两个对象是否相等，注意其第一个参数为期望结果，第二个参数为实际结果。对于期望结果，要用原生值，不是万不得已，不要用任何表达式计算获取的期望值。因为这样会让测试变得更不好理解，且也容易在计算期望值时发生错误。

```java
assertEquals(2, 1+1);
assertEquals("abc", "ab"+"c");

//第三个参数为自定义出错时的提示信息
assertEquals(2, 1+2, "failed to sum");

//第三个参数为函数式接口，出错信息如果需要通过计算获取，函数式接口就可以延迟对消息的计算
assertEquals(2, 1+2, () -> "failed" + "to" + "sum");
```

在Junit5中，`assertEquals`最终会调用`AssertionUtils.objectsAreEqual`方法:

```java
static boolean objectsAreEqual(Object obj1, Object obj2) {
  if (obj1 == null) {
    return (obj2 == null);
  }
  return obj1.equals(obj2);
}
```

底层方法调用的是对象的`equals`方法，如果自定义的对象需要准确使用`assertEquals`方法，在方法中需要实现`equals`方法.

`assertNotEquals`方法的参数与`assertEquals`是一样的，其不经常用到，笔者也不建议使用。

`assertSame`方法用来确定期望值和实际值是否指向同一个对象，`assertSame(new String("123"), new String("123"));`将抛出异常，因为两个对象地址不同。

`assertArrayEquals`用来判断数组是否相等，其将循环比较数组的每个元素。

`assertIterableEquals`可以用来比较任何实现了`Iterable`接口的对象，譬如`List`、`Set`等。

### assertTrue、assertFalse、assertNull、assertNotNull

`assertTrue`用来断言值或表达式返回值为`true`，譬如我们要判断返回的`String`里面是否包含某些字段，或者某个集合包含某个元素:

```java
@Test
void testAssertTrue() {
  assertTrue("hello world".contains("world"));
  assertTrue(Arrays.asList(1, 3, 5).contains(3));
}
```

> 注意，虽然assertTrue一般会和一些表达式结合在一起用，但是建议表达式越简单越好。

`assertFalse`用来断言值或表达式返回值为`false`。

`assertNotNull`与`assertNull`用来断言值或者表达式返回值是否为空。

### assertThrows、assertDoesNotThrow

`assertThrows`可以用来断言某个方法调用会抛出异常：

```java
@Test
void testAssertThrows() {
  Function<Integer, Integer> div = num -> 2 / num;
  assertThrows(ArithmeticException.class, () -> div.apply(0));
  assertThrows(NullPointerException.class, () -> div.apply(null));
}
```

Junit5中与Junit4的对于异常的断言方式有所不同。Junit5的断言方式更精准，能够精确断言哪一个调用会抛出异常。

`assertDoesNotThrow`用来断言某个方法调用不会抛出异常。 

### assertAll

`assertAll`可以把一组断言合并到一起，不管是否有哪个断言失败，这一组断言都会被执行，最终会将所有的错误合并在一起返回。

```java
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
```









