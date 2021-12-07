# 第5节：Java11之后版本新特性

Java11后的功能版本目前一直如期发布，虽然下一个长期版本的发布还需要等一年多时间，也可以提前了解下已发布的功能版本都有哪些新特性和改进。

## Java12

Java12于2019年3月19日正式发布，其包括的一些特性如：

- 引入一个新的垃圾收集器`Shenandoah`，其目前还是实验项目，旨在针对 JVM 上的内存收回实现低停顿的需求。

- `Switch`表达式的扩展，引入了`switch`表达式，其为预览特性：

  ```java
  int dayNumber = switch (day) {
      case MONDAY, FRIDAY, SUNDAY -> 6;
      case TUESDAY                -> 7;
      case THURSDAY, SATURDAY     -> 8;
      case WEDNESDAY              -> 9;
      default                      -> throw new IllegalStateException("Huh? " + day);
  }
  ```

- G1垃圾收集器的改进，使其能够在空闲时自动将 Java 堆内存返还给操作系统。在云或虚拟机上这个功能非常有用。

- Java微基准测试工具套件，此功能为JDK源代码添加了一套微基准测试，它基于`Java Microbenchmark Harness（JMH）`并支持JMH更新，可以轻松测试JDK性能。

## Java13

Java13于2019年9月17日正式发布，其包括的一些特性如：

- 改进了`AppCDS`，加大了 CDS 的使用范围，允许自定义的类加载器也可以加载自定义类给多个 JVM 共享使用。

- 改进了`ZGC`，使得其能释放未使用内存给操作系统，最大堆的大小从4TB到了16TB。

- 改进了`Socket`，引入 `NioSocketImpl` 的实现用以替换 `SocketImpl` 的 `PlainSocketImpl` 实现。

- 继续改进`Switch`，引入`yield`关键字返回结果，注意`switch`新特性仍是预览特性：

  ```java
  int dayNumber = switch (day) {
      case MONDAY, FRIDAY, SUNDAY:
      		yield 6;
      case TUESDAY:
      		yield 7;
      case THURSDAY, SATURDAY:
      		yield 8;
      case WEDNESDAY:
      		yield 9;
    	default:
      		throw new IllegalStateException("Huh? " + day);
  }
  ```

- 文本块作为预览功能出现：

  ```java
  String json = """
                 {
                     "name":"newjava",
                     "aliasName": "java9 to java14"
                 }
                 """;
  ```

  > 注意：预览功能在编译和执行时都要显示加入`--enable-preview`参数。

## Java14

Java14于2020年3月17日发布，其包括的新特性或改进如：

- `instanceof`的改进：

  ```java
  if (obj instanceof String str && str.length() > 5) {}
  ```

- `switch`表达式的标准化：

  ```java
  int j = switch (day) {
      case MONDAY  -> 0;
      case TUESDAY -> 1;
      default      -> {
          int d = day.toString().length();
          int result = f(d);
          yield result;
      }
  };
  ```

- G1收集器支持非一致性内存(`NUMA`)结构的CPU，NUMA服务器的基本特征是具有多个CPU模块，每个CPU模块由多个CPU(如4个)组成，并且具有独立的本地内存、I/O槽口等。NUMA架构中，访问远地内存的延时远远超过本地内存，因此当CPU数量增加时，系统性能无法线性增加。

- 引入`record`关键字，其目前仍是预览版本，如：

  ```java
  record Point(int x, int y) { } //其将自动生成构造方法，以及getter, setter，equals(), hashCode(), toString()等方法
  ```


## Java15

Java15于2020年9月15日发布，其包括的新特性或改进如：

- 封闭类（Sealed Classes），防止其他类或接口扩展或实现它们。目前为预览版本。

- 准备禁用和废除偏向锁。JDK15中默认禁用偏向锁，并弃用所有相关的命令行选项。

- 文本块：

  ```java
  String html = """
  <html>
    <body>
    	Hello World!
    </body>
  </html>
  """
  ```



## Java16

Java16于2021年3月16日发布，其包括的新特性或改进如：

- 向量操作的支持，目前还是孵化器版本。
- JDK源代码的管理从Mercurial 迁移到 Git，OpenJDK的代码现由Github托管。
- Java14引入的`record`关键字终于转正了。

