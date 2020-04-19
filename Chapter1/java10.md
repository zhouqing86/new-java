# 第3节：Java10特性概要

Java10于2018年3月20日发布，是Java9发布六个月后发布的另一个非长期维护Java版本，其引入的新特性并不多，但也不乏重要的特性修改，本节将介绍其引入的部分新特性：

- var关键字的引入
- 数据共享(AppCDS)
- 确定基于时间的版本发布策略

## 关键字var

如果对`JavaScript`有了解的读者对`var`关键字不会陌生，`var`可以在`Javascript`中用来声明/定义一个变量：

```javascript
var b; //仅声明一个变量
var a =1;
var addFunc = (a, b) => a + b;
```

> `ES5`以后，还可以使用`let`或`const`来定义变量。

好消息是Java10开始对于局部变量也能支持`var`关键字了，不过也是谨慎支持这个有用的语法糖。但Java语言仍旧是强类型语言，只是Java虚拟机能够根据表达式来推断变量的类型。

所以`var b;`这种变量声明语句Java10是不支持的，因为Java虚拟机无法从这个表达式推断出b的类型。那么如下的语句是否可以支持呢？

```java
var b;
b = "hello";
```

后面对变量`b`的字符串赋值可以推断出`b`为`String`类型，但是目前Java虚拟机的类型推断还没有这么强大，很遗憾Java10不支持，那么哪些情况是支持的呢？

- 方法/函数里的局部变量

  ```java
  void someMethod() {
    var a = 1;
  	var list = new ArrayList<String>();
  }
  
  Supplier someFunc = () -> {
      var a2 = 1;
      var list2 = new ArrayList<Integer>();
      list2.add(a2);
      return list2;
  };
  ```

- 类静态块里的局部变量

  ```java
  static {
      var a3 = 1;
      var list3 = new ArrayList<Integer>();
  }
  ```

- `for`循环里的索引变量

  ```java
  for (var item : list) {}
  for (var i=0; i<list.size(); i++) {}
  ```

其不支持的场景：

- 不能用于方法的参数类型
- 类成员变量不能使用var
- 不能从Lambda表达式推断出函数接口类型

## AppCDS

`CDS`(`Class-Data Sharing`)的作用是可以让类可以被预处理放到一个归档文件中，Java程序以后启动时直接可以将此文件映射到内存中。Java1.5就具有了`CDS`的特性，不过仅仅可用于根类加载器（`Boot Class Loader`）加载的类。

`AppCDS`是`CDS`的升级版，其不仅仅可以用于根类加载器加载的类，也可以用于应用类加载器（`App Class Loader`）加载的类。意味着第三方类库，自行开发的类，都将可能被`AppCDS`支持。

Java10对`AppCDS`的支持加强了对`Serverless`应用程序的支持，因为其可以大大提升Java程序的加载速度。

首先使用如下命令来尝试生成默认的归档文件：

```shell
java -Xshare:dump 
```

其将生成Java运行时环境依赖的类的归档文件，默认文件路径为`${JAVA_HOME}/lib/server/classes.jsa`，MacOS下大小18M左右。

尝试使用归档文件运行在第2节Java模块化系统中生成的`HelloWorldModule.jar`：

```shell
java -Xshare:on -Xlog:class+load:file=cds.log -jar mods/HelloWorldModule.jar
```

`-Xshare:on`会默认读取上面生成的归档文件，`cds.log`中有类似如下的日志:

```wiki
[0.026s][info][class,load] opened: /opt/gradle/jdk-11.0.2.jdk/Contents/Home/lib/modules
[0.053s][info][class,load] java.lang.Object source: shared objects file
[0.055s][info][class,load] java.io.Serializable source: shared objects file
[0.058s][info][class,load] java.lang.Comparable source: shared objects file
....
[0.280s][info][class,load] com.newjava.HelloWorld source: file:/Users/qzhou/project/java-expert/learning-java9/HelloWorldModule/mods/HelloWorldModule.jar
```

从日志中可以看出，`Object`、`Secriablizable`、`Comparable`等类是从归档文件中加载的，而`HelloWorld`并没有从归档文件中加载，因为生成的默认的归档文件中并没有`HelloWorld`类。

如何把`HelloWorld`类也写入归档文件中：

- 从`JAR`包中生成类列表

  ```shell
  java -XX:DumpLoadedClassList=classes.lst -jar mods/HelloWorldModule.jar
  ```

- 创建归档文件

  ```shell
  java -Xshare:dump -XX:SharedClassListFile=classes.lst -XX:SharedArchiveFile=HelloWorldModule.jsa --class-path mods/HelloWorldModule.jar
  ```

- 使用归档文件来运行程序

  ```shell
  java -XX:SharedArchiveFile=HelloWorldModule.jsa -Xlog:class+load:file=HelloWorldModule-cds.log -jar mods/HelloWorldModule.jar
  ```

  查看`HelloWorldModule-cds.log`，可以看到：

  ```wiki
  [0.164s][info][class,load] com.newjava.HelloWorld source: shared objects file
  ```

> 如果你下载Oracle的JDK，需要加上-XX:+UnlockCommercialFeature来开启AppCDS

## 基于时间的版本发布

从Java10开始，将采用了一种新的严格的基于时间的发布模式，意味着：

- 每六个月发布一个新的Java版本，如Java10在2018年3月发布，则Java11在2018年的9月发布，这些新的版本都被称之功能发布版本，预计将包含至少一个或两个重要功能
- 每个功能发布版本发布后只提供6个月的支持，即支持到下一个版本的发布
- 从2018年9月开始，每三年的功能发布版本应是一个长期版本，Java11就是一个长期版本，即一个长期版本至少会持续更新3年

改进了`java -version`命令，其将打印当前Java发布的时间:

```shell
$ java -version
openjdk version "10" 2018-03-20
OpenJDK Runtime Environment 18.3 (build 10+46)
OpenJDK 64-Bit Server VM 18.3 (build 10+46, mixed mode)
```

另版本的格式规定为`$FEATURE.$INTERIM.$UPDATE.$PATCH`:

- `$FEATURE`，功能版本，如Java10的功能版本就是`10`
- `$INTERIM`，对于包含兼容错误修复和增强功能的非功能版本，此计数器会递增，但不会有不兼容的更改，不会删除功能，也不会更改标准API，一般情况对于非长期版本，此位都为`0`
- `$UPDATE`，修复问题的兼容更新版本，按序递增
- `$PATCH`，紧急补丁版本

Java10也提供了相应的API来获取或解析版本：

```java
Runtime.Version version = Runtime.version();
version.feature();
version.interim();
version.update();
version.patch();

Runtime.Version version2 = Runtime.Version.parse("10.0.1");
version2.feature();
version2.interim();
version2.update();
version2.patch();
```

