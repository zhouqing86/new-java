# 第2节：Java项目管理

上一节介绍了`Gradle`的基本功能，其与`Java`程序的构建没有直接的关系。本节将重点介绍如何使用`Gradle 6.3.0`来构建和管理`Java`项目。

#### 创建项目

执行`gradle init`可以创建`Java`项目，中间会有很多选项：

```shell
⇒  gradle init

Select type of project to generate:
  1: basic
  2: application
  3: library
  4: Gradle plugin
Enter selection (default: basic) [1..4] 2

Select implementation language:
  1: C++
  2: Groovy
  3: Java
  4: Kotlin
  5: Swift
Enter selection (default: Java) [1..5] 3

Select build script DSL:
  1: Groovy
  2: Kotlin
Enter selection (default: Groovy) [1..2] 1

Select test framework:
  1: JUnit 4
  2: TestNG
  3: Spock
  4: JUnit Jupiter
Enter selection (default: JUnit 4) [1..4] 4

Project name (default: learning-gradle-java):

Source package (default: learning.gradle.java): com.newjava.gradle


> Task :init
Get more help with your project: https://docs.gradle.org/6.3/userguide/tutorial_java_projects.html

BUILD SUCCESSFUL in 50s
2 actionable tasks: 2 executed
```

这样一个简单的`Java`项目就被创建了，其将自动生成`Gradle`相关文件、`Java`项目结构和一个简单的`App.java`即其测试文件`AppTest.java`。

```shell
⇒  tree .
├── build.gradle
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── newjava
    │   │           └── gradle
    │   │               └── App.java
    │   └── resources
    └── test
        ├── java
        │   └── com
        │       └── newjava
        │           └── gradle
        │               └── AppTest.java
        └── resources

15 directories, 8 files
```

在第三章第4节还介绍了一种使用参数来代替交互式输入的方式来创建项目：

```shell
⇒  gradle init --type learning-gradle-java --dsl groovy --package com.newjava.gradle --project-name gradle-app --test-framework junit-jupiter
```

创建项目后，就可以运行一些`gradle`命令来执行任务：

- `gradle test`或`./gradlew test`来执行测试，执行完此命令过程中可以看到`Gradle`会自动去下载一些依赖包，执行命令结束后`build`目录会被创建，`build`目录中既有编译好的字节码文件，也有测试报告文件等：

  ```shell
  ⇒  tree .
  ├── build
  │   ├── classes
  │   │   └── java
  │   │       ├── main
  │   │       │   └── com
  │   │       │       └── newjava
  │   │       │           └── gradle
  │   │       │               └── App.class
  │   │       └── test
  │   │           └── com
  │   │               └── newjava
  │   │                   └── gradle
  │   │                       └── AppTest.class
  │   ├── reports
  │   │   └── tests
  │   │       └── test
  │   │           ├── classes
  │   │           │   └── com.newjava.gradle.AppTest.html
  │   │           ├── css
  │   │           │   ├── base-style.css
  │   │           │   └── style.css
  │   │           ├── index.html
  │   │           ├── js
  │   │           │   └── report.js
  │   │           └── packages
  │   │               └── com.newjava.gradle.html
  │   ├── test-results
  │   │   └── test
  │   │       ├── TEST-com.newjava.gradle.AppTest.xml
  │   │       └── binary
  │   │           ├── output.bin
  │   │           ├── output.bin.idx
  │   │           └── results.bin
  ```

- `gradle assemble`或`./gradlew assemble`来生成程序发布包：

  ```shell
  ⇒  tree .
  ├── build
  │   ├── distributions
  │   │   ├── learning-gradle-java.tar
  │   │   └── learning-gradle-java.zip
  │   ├── libs
  │   │   └── learning-gradle-java.jar
  ```

  `learning-gradle-java.jar`生成纯粹的当前项目的`JAR`包，`JAR`包里不会有任何第三方依赖。

  `learning-gradle-java.zip`和`learning-gradle-java.tar`将依赖的第三方`JAR`包以及当前项目的`JAR`包都放入压缩包`lib`目录下，同时也生成`bin/learning-gradle-java`使得此包在有`Java`运行环境的操作系统中直接运行。

- `gradle build`或`./gradlew build`命令既会运行测试，也会生成程序发布包。

#### 依赖管理



#### Java常用Gradle插件



#### 构建/测试/发布



#### 多项目管理



#### 







