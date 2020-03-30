# 第5节：单元测试技巧

关于什么是单元测试，很多人的理解是不同的。Martin Fowler在其关于[微服务测试](https://martinfowler.com/articles/microservice-testing/#conclusion-summary)的文章中有一个定义：

```wiki
Exercise the smallest pieces of testable software in the application to determine whether they behave as expected.
单元测试对应用程序中可测软件的最小部分进行训练，通过训练来决定是否每个最小部分都能表现如预期。
```

维基百科中有更具体的关于单元测试中`单元`的描述:

```wiki
In procedural programming, a unit could be an entire module, but it is more commonly an individual function or procedure. In object-oriented programming, a unit is often an entire interface, such as a class, but could be an individual method.
面向过程编程中，一个单元可以是一个完整模块，但更普遍的说法是一个独立的函数或过程。
面向对象编程中，一个单元经常是一个完整的接口，例如一个类，但也可以是一个独立的方法。
```

#### 给单元测试定义写单元测试用例

笔者自从学习了测试驱动开发以后，有了两个从代码开发到影响实际生活的习惯：

- 面对复杂问题和抽象问题，先拆解成更小的可理解的问题

- 针对更小的可理解的问题，思考验收测试用例

针对单元测试用例的定义，一些验收测试用例：

```java
@ParameterizedTest
@ValueSource(strings = {
        "测试String类的方法：如concat, trim等",
        "测试String类的方法的链式调用，如.trim().toLowerCase()",
        "测试函数式接口的default方法",
        "测试类的静态方法",
        "测试类的构造",
})
void testTheScenarioThatIsUnitTest(String testcase) {
    assertTrue(isUnitTest(testcase));
}

@ParameterizedTest
@ValueSource(strings = {
        "测试对数据库操作，如增删改查等",
        "测试文件，图片等数据在网络上的传输与存储，如测试对Restful API接口的调用",
        "方法的性能测试",
})
void testTheScenarioThatIsNotUnitTest(String testcase) {
    assertFalse(isUnitTest(testcase));
}
```

#### FIRST原则

关于如何写好单元测试，谨记遵循FIRST原则：

- Fast，单元测试执行要快
- Independent，单元测试用例之间相互独立，相互之间不能有依赖或先后执行的关系
- Repeatable，单元测试的结果应与环境、时间无关
- Self-validation，单元测试的结果需要自动验证
- Timely，单元测试的编写越早越好

##### Fast

单元测试的执行要快，很直观如果开发过程中需要花大量时间去等待单元测试结果，那几乎是不可容忍的。有一些程序员甚至定义了一些规则，譬如：执行时间超过100ms的就不是单元测试。这种想法有些极端却不无道理，单元测试的目的是迅速的获取反馈，其会在开发过程中不断被重复执行，运行时间过长会直接导致开发效率下降，也会让程序员在开发过程中变得不那么专注。

为了让单元测试更快，有一些技巧：

- 使用`@BeforeAll`注解：在测试类中，不管有多少测试方法，`@BeforeAll`注解的静态方法将只被执行一次。

  ```java
  class ZoneInfoUtilsTest {
  
      private static Map<String, ZoneInfo> zoneInfoMap;
    
      @BeforeAll
      static void beforeAll() {
          zoneInfoMap = ZoneInfoTestHelper.buildZoneInfoMap();
      }
  }
  
  class ParseFileTest {
      @TempDir
      static Path sharedTempDir;
  
      @BeforeEach
      void setUp() throws FileNotFoundException {
          Path file = sharedTempDir.resolve("test.txt");
          try (PrintWriter pw = new PrintWriter(file.toFile())){
              pw.println("# Abbreviation, Zone Name, GMT Offset");
              pw.println("SHANGHAI, Asia/Shanghai, + 08:00");
              pw.println("ACST, Australia/Darwin, + 09:30");
          }
      }
  }
  ```

  > 对于大对象的构建和临时文件的常见，如果我们不希望每个测试用例开始是都进行这些比较耗时操作，尽量多的使用`@BeforeAll`注解

- 在同一个测试用例类中，由于不同的测试方法之间可能会需要不同的构造对象，那么久可以对测试方法进行分组，使用内部类和`@Nest`注解：

  ```java
  public class NestTestClassTest {
      @BeforeEach
      void setUp() {
          System.out.println("External setup");
      }
  
      @Nested
      class NestTestClass {
          @BeforeEach
          void setUp() {
              System.out.println("Nested setup ");
          }
  
          @Test
          void testMethod() {
              System.out.println("test method");
          }
      }
  }
  ```

  所有测试方法公用的构造放在外部类，一组测试方法单独需要的构造放在内部类中。

- 测试期望结果是直观的，尽量不要有任何的计算逻辑来获取期望结果。譬如，"ABC"作为期望结果就比" abc ".trim().toUppercase()要很多。

- 实在有非常耗时的测试用例，甚至如果调用时有可能会出现死锁情况的用例使用好`@Timeout`注解。

- 持续集成中考虑测试用例`Fail Fast`，尤其是测试用例比较多，测试用例执行总时间比较多，在遇到第一个失败的测试就可以提示开发者。如`gradle test --fail-fast`。

##### Independent

单元测试用例之间应是相互独立的：

- 不要出现测试用例之间有顺序执行依赖的情况，如测试用例A必须在测试用例B之前执行
- 不要出现测试用例相互干扰的情况，如果测试用例对共享变量的修改

一些技巧：

- 对于`@BeforeAll`注解构造出的静态变量，所有的测试用例都只应读取而不应有任何修改操作。

- 对于实现类中静态成员变量的修改，将会导致测试方法之间相互印象，举个例子：

  ```java
  public class ClassWithStaticField {
      public static Integer count = 1;
  
      public static Integer increment() {
         return ++count;
      }
  
      public static Integer decrement() {
          return --count;
      }
  }
  
  @Nested
  class TestClassStaticMember {
      @Test
      void testDecrement() {
          assertEquals(0, ClassWithStaticField.decrement());
      }
  
      @Test
      void testIncrement() {
          assertEquals(2, ClassWithStaticField.increment());
      }
  }
          
  ```

  上面的例子中如果`testDecrement`和`testIncrement`单独测试都会通过，但是如果对`TestClassStaticMember`进行测试，则发现其中一个测试用例会测试失败。

  解决方式是首先检查实现代码是否有可以其他可以优化的方式，如果不可避免需要用静态成员变量，每个测试用例在测试前保存其状态，测试后再恢复期状态。

  ```java
  @Nested
  class TestClassStaticMember {
  
      private Integer count;
  
      @BeforeEach
      void setUp() {
          count = ClassWithStaticField.count;
      }
  
      @AfterEach
      void tearDown() {
          ClassWithStaticField.count = count;
      }
  
      @Test
      void testDecrement() {
          assertEquals(0, ClassWithStaticField.decrement());
      }
  
      @Test
      void testIncrement() {
          assertEquals(2, ClassWithStaticField.increment());
      }
  }
  ```

- 对于系统环境变量的修改，需要执行单元测试之前保存其状态，测试结束后恢复其值

  ```java
  @Nested
  class EnvironmentVariableRelatedTest {
  
      @Test
      void testEnv1() {
          String env = System.getProperty("env");
          System.setProperty("env", "PROD");
          System.out.println("Do testEnv1 with env: " + System.getProperty("env"));
          System.setProperty("env", Objects.isNull(env) ? "" : env);
      }
  
      @Test
      void testEnv2() {
          String env = System.getProperty("env");
          System.setProperty("env", "PROD");
          System.out.println("Do testEnv2 with env: " + System.getProperty("env"));
          System.setProperty("env", env);
      }
  }
  ```

##### Repeatable

测试要是可以重复的，这个可重复不仅仅指的是重复多次，更多的是指测试用例是与时间无关、与环境无关的。

- 与时间有关的测试用例：

  ```java
  public static boolean isExpired(ZonedDateTime zonedDateTime) {
      ZonedDateTime now = ZonedDateTime.now();
      return zonedDateTime.isAfter(now);
  }
  
  @Test
  void testIsExpired() {
     String date = "2021-03-21T16:47:22.757+08:00[Asia/Shanghai]";
     ZonedDateTime zonedDateTime = ZonedDateTime.parse(date);
     assertTrue(TimeRelated.isExpired(zonedDateTime));
  }
  ```

  这个测试在2021年3月21日前一直会测试成功，但是到了2020年3月22日就会失败。

- 与环境相关的测试用例：

  ```java
  public static String generatePath(String pathPart1, String pathPart2) {
      return pathPart1 + File.separator + pathPart2;
  }
  
  @Test
  void testEnvironmentRelated() {
      assertEquals("dir1/dir2", EnvironmentRelated.generatePath("dir1", "dir2"));
  }
  ```

  这个测试用例在Windows环境就会测试失败！环境相关不仅仅是意味着与环境变量相关， 与系统相关也是需要注意的。

- 注意对`Map`和`Set`的使用:

  ```java
  @Test
  void testSetToString() {
      Set<Integer> set = new HashSet<>();
      set.add(5);
      set.add(7);
      assertEquals("[5, 7]", set.toString());
  }
  
  @Test
  void testMapToString() {
      Map<Integer, Integer> map = new HashMap<>();
      map.put(1, 3);
      map.put(2, 4);
      assertEquals("{1=3, 2=4}", map.toString());
  }
  ```

  `Map`与`Set`是无序结合，期望结果却依赖于与`Set`和`Map`的顺序。建议用`String.contains`方法来替换上面的测试方式。

- 如果实现中使用了`Math.Random()`之类的方法，也需要注意测试的可重复性。

##### Self-validation

自我认证是单元测试的基本要素。一些程序员只是为了满足测试覆盖率而写测试用例，在这个目标的驱动下，断言的使用就无足轻重了。

删除测试用例中的`System.out.println`，用断言去替代。

##### Timely

单元测试什么时候写最好呢，读者如果详细读了上一节关于测试驱动开发的内容。应该不难得出如下结论：

- 对于新开发代码，在写实现代码之前写最好。
- 修复系统BUG前先写测试用例，单元测试挂掉后再修复实现代码。
- 但是对于遗留系统的代码，在对代码做重构之前，先对已有打算重构的代码写好单元测试，然后再开始重构代码。











