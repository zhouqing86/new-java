# 第2节：Java9特性概要

从JDK8到JDK9，等待了三年多，JDK9最终于2017年9月21日发布。本节将简要介绍JDK9的部分特性：

- 模块化系统，譬如使用jlink就可以根据需要定制运行时环境
- 响应式编程的支持
- 可交互式的REPL工具jshell
- 改进的HttpClient，支持HTTP 2
- Optional的改进
- 集合工厂方法

> 本节的内容是让读者快速的了解和入门Java9的新特性。关于模块化以及响应式编程，更详细的介绍可查看第七章和第八章。

## 模块化系统

##### 免安装JDK/JRE的Java程序包

介绍模块化系统之前，先来考虑这个需求：把某个Java程序打包不可独立运行的包或二进制文件，即操作系统中不安装JDK/JRE，也可以直接运行这个Java程序包。先看看Java9中如何来做到这一点：按如下目录结构创建两个文件`HelloWorldModule/module-info.java`和`HelloWorldModule/com/newjava/HelloWorld.java`：

```shell
├── HelloWorldModule
│   ├── com
│   │   └── newjava
│   │       └── HelloWorld.java
│   └── module-info.java
```

`HelloWorldModule/module-info.java`文件内容:

```java
module HelloWorldModule {
}
```

`HelloWorldModule/com/newjava/HelloWorld.java`文件内容：

```java
package com.newjava;

public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello Java9!");
    }
}
```

进入`HelloWorldModule`，编译这两个文件：

```shell
mkdir -p out/HelloWorldModule
javac -d out/HelloWorldModule com/newjava/HelloWorld.java module-info.java
```

> 注意这里的命令都是基于Shell脚本的命令，在Windows操作系统上基本也是差不多，读者自行参考。

运行编译后的文件，可以看到打印的结果`Hello Java9!`：

```java
java --module-path out --module HelloWorldModule/com.newjava.HelloWorld
```

也可打包成一个模块化JAR包来运行:

```shell
jar -cfe mods/HelloWorldModule.jar com.newjava.HelloWorld -C out/HelloWorldModule .
java --module-path mods --module HelloWorldModule #打印结果为Hello Java9!
```

打包成一个可独立运行的程序包的命令：

```shell
jlink --module-path out/:$JAVA_HOME/jmods --add-modules HelloWorldModule --launcher hello=HelloWorldModule/com.newjava.HelloWorld --output helloworld-image
```

命令运行结束后，会发现一个`helloworld-image`目录被创建了，其一级目录如下：

```java
├── bin
├── conf
├── include
├── legal
├── lib
└── release
```

`bin`目录下有三个文件，`java`, `keytool`和`hello`。直接运行`bin/hello`会发现也会打印出`Hello Java9!`，基于`helloworld-image`这个目录生成的压缩包`helloworld-image.zip`就是一个可独立运行的程序包了，可以在没有安装JDK/JRE的环境去运行。

```shell
zip helloworld-image.zip -r helloworld-image
ls -lh helloworld-image.zip #压缩包大小为13M，比JDK/JRE安装包小很多
```

> 注意的是MacOS操作系统上生成的包不能在Windows操作系统和Linux操作系统上运行；同样，Windows操作系统上生成的程序包不能在MacOS操作系统和Linux操作系统上运行。因为各个系统对于机器代码（二进制）的解释是不同的。

`bin/hello`在Linux/MacOS操作系统中实际上是一个shell脚本文件:

```shell
#!/bin/sh
JLINK_VM_OPTIONS=
DIR=`dirname $0`
$DIR/java $JLINK_VM_OPTIONS -m HelloWorldModule/com.newjava.HelloWorld $@
```

文件中`-m`是`--module`的缩写，shell文件中调用的命令只是比我们上面编译后运行少了`--module-path out`。那么这个`bin/java`与操作系统上完整安装的`java`到底有什么区别呢：

```shell
java --list-modules | wc -l #返回结果为75，意味着有75个module
> bin/java --list-modules #返回结果里只有两个module
HelloWorldModule
java.base@9.0.4
```

> 注意笔者这一节使用的是OpenJDK 9.0.4，如果你使用的是其他版本的JDK/JRE，系统JDK/JRE返回的`modules`数目会有不同

这个打包的例子里的知识点：

- 在Java9模块化系统之前，JDK的运行时库由一个庞大的`rt.jar`所组成，其大小超过了60MB，包含了Java大部分运行时类，自从Java9以后，`rt.jar`被废弃，基础类库被拆分成了多个模块
- 用`jlink`命令打包的程序包中包含了一个裁剪版的Java运行时环境，打包的程序包是绿色可便携的
- `module-info.java`是Java对模块化支持的一个重要文件，是模块描述符，是Java9模块化系统不可缺少元素
- `module-info.java`也需要被编译成字节文件
- 模块化`JAR`包与Java9之前的`JAR`包的不同是多了`module-info.class`文件
- `java.base`模块是Java提供的最底层的模块，提供最基本的API，如`java.lang`和`java.util`
- `module-info.java`中虽然没有声明对`java.base`的依赖，但是其是Java默认的隐式依赖
- `module-info.java`中有模块的命名应具有唯一性，JVM运行前如果检测到两个命名相同的模块在同一个目录，会看做有冲突而报错退出；如果在不同目录，将使用第一个遇到的模块而忽略其他相同命名模块
- `--module-path`不同于`--class-path`，`--module-path`中的内容优先于`--class-path`中的内容被加载

##### 模块化系统的基本知识介绍

关于模块，不同的人的理解有所不同，基本能够达成共识的是模块应该有高内聚，对外接口开放的特性。在OpenJDK网站上的文章[The State of the Module System](http://openjdk.java.net/projects/jigsaw/spec/sotms)中的给出Java中对模块的理解和定义:

```wiki
In order to provide reliable configuration and strong encapsulation in a way that is both approachable to developers and supportable by existing tool chains we treat modules as a fundamental new kind of Java program component. A module is a named, self-describing collection of code and data. Its code is organized as a set of packages containing types, i.e., Java classes and interfaces; its data includes resources and other kinds of static information.
为了提供可靠的配置和强大的封装，也考虑对开发人员友好和被现有工具链支持，我们将模块视为基础的新Java程序组件。一个模块是一个命名的且自描述的代码和数据的集合。模块的代码被组织成一组包含类型（如Java类和接口）的包；模块的数据包括资源文件和其他各种静态信息。
```

上面定义中：

- 模块的自描述的代码即`module-info.java`
- 正常的模块必须是被命名的，Java9以前的jar包都被当做是没有命名的模块
- 可靠的配置是通过在模块描述代码中精确的声明自身的依赖来实现，其将逐渐取代之前Java中长期存在的易错的`classpath`机制
- 强大的封装是通过在模块描述代码中精确的声明哪些公共类型/接口可以被外部模块访问来实现，没有声明的类型/接口是不能够被外部访问的

这里给出两个例子：

- Java9中`java.xml.crypto`模块的[module-info.java](http://hg.openjdk.java.net/jdk-updates/jdk9u/jdk/file/d54486c189e5/src/java.xml.crypto/share/classes/module-info.java):

  ```java
  module java.xml.crypto {
      requires java.logging;
  
      requires transitive java.xml;
  
      exports javax.xml.crypto;
      exports javax.xml.crypto.dom;
      exports javax.xml.crypto.dsig;
      exports javax.xml.crypto.dsig.dom;
      exports javax.xml.crypto.dsig.keyinfo;
      exports javax.xml.crypto.dsig.spec;
  
      provides java.security.Provider with
          org.jcp.xml.dsig.internal.dom.XMLDSigRI;
  }
  ```

- Java9中`java.logging`模块的[module-info.java](http://hg.openjdk.java.net/jdk-updates/jdk9u/jdk/file/d54486c189e5/src/java.logging/share/classes/module-info.java)

  ```java
  module java.logging {
      exports java.util.logging;
  
      provides jdk.internal.logger.DefaultLoggerFinder with
          sun.util.logging.internal.LoggingProviderImpl;
  }
  ```

> 访问[http://hg.openjdk.java.net/jdk-updates/jdk9u/jdk](http://hg.openjdk.java.net/jdk-updates/jdk9u/jdk)选择左边的菜单`browser`即可查看JDK9的源代码

关于这两个例子中的关键字介绍：

- `requires`关键字声明本模块所依赖的模块
- `transitive`关键字修饰`requires`时，意味着依赖模块的依赖也会引入本模块，`transitive`的依赖会递归追溯
- `exports`关键字声明本模块能被外部模块访问的包，此包中的`public`类型/接口能被外部模块所使用
- `provides with`关键字声明了模块提供了某个接口的实现，其主要用于支持`ServiceLoader`机制，这里的接口一般由所需的另一个模块中提供；而实现类一般是本模块提供

##### 模块化系统如何兼容Java9以前的版本

如何做到兼容，需要解决两个问题：

- 模块化包应能调用Java9前Java版本中生成的非模块化`JAR`包中的接口。

  解决方式：如果在命名模块中没有加载到定义的类型，则模块系统将尝试从`classpath`中加载。从`classpath`中加载的类型都会被当做`无名模块`的内容。`无名模块`能够读取所有的模块声明可被访问的类型/接口，同时暴露其所有`public`的类型/接口。

- Java9生成的模块化`JAR`包能够被非模块化`JAR`包调用

  解决方式：将模块化`JAR`包作为`--class-path`中的内容，`module-info.class`将被直接忽略。

##### JMOD文件

在`$JAVA_HOME/jmods`目录下，有很多后缀名为`.jmod`的文件，如`java.se.jmod`。这是模块化`JAR`包之外的另外一种模块化包，其主要为了解决`JAR`包不支持本机代码(`Native Code`)的问题。而JRE/JDK的底层需要实现大量的`native code`。

读者也可以尝试将`HelloWorldModule`打包成`jmod`文件:

```
jmod create --class-path mods/HelloWorldModule.jar jmods/HelloWorldModule.jmod
```

`HelloWorldModule.jmod`可以在编译(`javac命令`)时和链接(`jlink`命令)时使用， 运行(`java命令`)时不支持`JMOD`格式。

##### 模块的版本

模块的声明不包括版本字符串，也不包含对其依赖的模块的版本字符串的约束。 这种设计师有意的：解决版本选择问题不是模块系统的目标，最好是构建工具和容器应用程序。

`module-info.java`中没有可以设置模块版本的关键字，但在使用`jar`命令生成模块`JAR`包时可以设置版本：

```shell
$ jar --module-version 0.0.1 -c -f mods/HelloWorldModule.jar -e com.newjava.HelloWorld -C out/HelloWorldModule .
$ jar -d -f mods/HelloWorldModule.jar
HelloWorldModule@0.0.1 jar:file:///opt/HelloWorldModule/mods/HelloWorldModule.jar/!module-info.class
requires java.base mandated
contains com.newjava
main-class com.newjava.HelloWorld
```

> 即使模块`JAR`包中添加了版本系统，模块化系统也不会使用这个版本信息，模块完全是由名称解析的，如果在模块路径的同一目录中找到多个名称相同但版本不同的模块，会导致错误。版本的冲突解决是一个复杂且很有争议的话题，Java的构建工具Maven和Gradle等都有解决方案。

## Jshell

Java9开始提供了Java的交互式编程环境，Java开发人员又有了一个强大的工具，不用再羡慕其他语言的交互式编程环境了，一个计算年复利的例子：

```java
$ jshell
jshell> Math.pow(1+0.1, 20) //使用Math的pow方法来计算年利率为百分之十的情况下，20年后翻了多少倍
$1 ==> 6.727499949325611

jshell> BiFunction<Double, Integer, Double> calInterest = (annualRate, years) -> Math.pow(1+annualRate, years) //定义一个calInterest的函数
calInterest ==> $Lambda$14/1287712235@457e2f02

jshell> calInterest.apply(0.1, 20) //调用calInterest的函数
$3 ==> 6.727499949325611

jshell> $3 * 50000
$4 ==> 336374.99746628053

jshell> /save jshell_workspace.txt //将当前jshell执行的代码保存到文件中

jshell> /exit //退出jshell
```

Jshell也可以加载Java代码段文件，这个文件可以是导出的，也可以是新编写的代码段文件:

```java
$ jshell
jshell> $3
$3 ==> 6.727499949325611

jshell> $4
$4 ==> 336374.99746628053
```

Jshell可以通过`--class-path`加载任意`JAR`包或者`--module-path`加载模块化`JAR`包到Jshell的运行环境中，可以在运行`jshell`命令时传入参数，也可以在运行后通过`/env`命令来设置：

```java
$ jshell
jshell> /env --module-path out --add-modules HelloWorldModule
jshell> import com.newjava.HelloWorld;
jshell> HelloWorld.main(new String[]{})
|  java.lang.IllegalAccessError thrown: class REPL.$JShell$12 (in unnamed module @0x3dd4520b) cannot access class com.newjava.HelloWorld (in module HelloWorldModule) because module HelloWorldModule does not export com.newjava to unnamed module @0x3dd4520b
|        at (#2:1)
```

这里调用`HelloWorld.main`函数时报错，如何修复呢？

- 一种方式是通过`jshell --class-path out/HelloWorldModule/`的方式将`com.newjava.HelloWorld`导入忽略掉`module-info.java`的`无名模块`。
- 另一种方式是在`module-info.java`中添加`exports com.newjava;`使得`HelloWorld`对外部模块可见，而后重新编译模块。

## 响应式编程

面向对象编程聚焦的是什么是对象以及如何使用对象，函数式编程聚焦在什么是函数以及如何使用函数，而响应式编程聚焦在数据是如何流动的，以及对数据的修改如何能够被传递和响应。

Java9是第一个引入响应式编程的一些特性（响应式流）的标准JDK。先看一个例子：

````java
SubmissionPublisher<Integer> pub = new SubmissionPublisher<>();
pub.consume(System.out::println);
pub.submit(1);
Thread.sleep(1000);
pub.submit(2);
Thread.sleep(1000);
pub.submit(3);
````

这个程序段仅仅为了对响应式流有一个基本的认知：

- 创建了一个`SubmissionPublisher`发布者对象
- 通过调用创建的发布者对象的`cosume`方法注册一个消费函数`System.out::println`
- 每隔一秒钟调用发布者对象的`submit`方法来发布新的数据

将这个程序段写入文件`reactive_java_1.txt`而后在Jshell中运行:

```java
$jshell
jshell> /open reactive_java_1.txt //结果之间的打印会等待一秒钟
1
2
3
```

对Java观察者模式有所了解的读者应很了解例子中的模式，对于不了解观察者模式的读者，可以想象为一个设计好的电路中，`System.out::println`就是一个电压表，当电压变化时，电压表能够随时反应出实际电压。

这个`设计好的电路`，就是响应式编程中聚焦的数据流动流程。

## 集合静态工厂方法

Java9在`List`，`Set`和`Map`的接口中增加了静态工厂方法`of`，可以用其来创建不可变的`List`，`Set`或`Map`。这也大大方便了Java开发人员对集合的使用。

```shell
$jshell
jshell> List.of(1, 2, 3)
$1 ==> [1, 2, 3]

jshell> Set.of(1, 2, 3)
$2 ==> [1, 3, 2]

jshell> Map.of("key1", "value1", "key2", "value2")
$3 ==> {key2=value2, key1=value1}

jshell> Map.ofEntries(Map.entry("key1", "value1"))
$4 ==> {key1=value1}
```

## Optional改进

Java8中提供了`Optional`类来解决变量为`null`的问题，Java9增强了此类的功能，譬如其增加了`stream`方法，一个方法的增加让`Stream`的链式调用更加流畅。可以省掉方法/函数的参数校验：

```shell
jshell> Function<List<String>, Long> sizeFunc = (list) -> Optional.ofNullable(list).stream().flatMap(List::stream).count()

jshell> sizeFunc.apply(null)
$1 ==> 0

jshell> sizeFunc.apply(List.of())
$2 ==> 0

jshell> sizeFunc.apply(List.of("abc"))
$3 ==> 1
```



## 新HttpClient











