# 第1节：基础模块介绍

Java9以后`rt.jar`被废弃，取而代之的是几十个模块，本节将介绍其中的一些常用基础模块。这些基础模块有两种前缀:

- `java.`前缀的模块定义在JAVA标准平台规范中

  ```wiki
  The Java Platform, Standard Edition (Java SE) APIs define the core Java platform for general-purpose computing. These APIs are in modules whose names start with java.
  Java平台标准版（Java SE）API为通用计算定义了核心Java平台。这些API位于名称以java开头的模块中。
  ```

- `jdk.`前缀的模块是定义在JDK规范中

  ```java
  The Java Development Kit (JDK) APIs are specific to the JDK and will not necessarily be available in all implementations of the Java SE Platform. These APIs are in modules whose names start with jdk.
  Java开发工具包（JDK）API是JDK特有的，不一定在Java SE平台的所有实现中都可用。这些API位于名称以jdk开头的模块中。
  ```

对于Java11，可以通过链接[https://docs.oracle.com/en/java/javase/11/docs/api/index.html](https://docs.oracle.com/en/java/javase/11/docs/api/index.html)获取这些模块的文档。

#### JAVA.BASE

`java.base`模块是一个特殊的模块，是平台所有模块的根模块，所有的模块都将隐性依赖此模块。其部分模块描述符内容:

````java
module java.base {
    exports java.io;
    exports java.lang;
    exports java.lang.annotation;
    exports java.lang.invoke;
    exports java.lang.module;
    exports java.lang.ref;
    exports java.lang.reflect;
    exports java.math;
    exports java.net;
    ...
}
````

java

#### JAVA.SE



