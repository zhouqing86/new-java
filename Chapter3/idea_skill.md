# 第4节：常用技巧

#### Maven/Gradle创建IDEA工程文件

Maven和Gradle都是Java的两大主流构建工具。

##### Maven生成IDEA工程文件

```shell
⇒ mvn -version
Apache Maven 3.5.2
⇒ mvn archetype:generate -DgroupId=com.learn.idea -DartifactId=maven-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false
```

而后使用`mvn idea:idea`就可以创建IDEA的工程文件，最后，项目文件列表如：

```shell
├── maven-app.iml
├── maven-app.ipr
├── maven-app.iws
├── pom.xml
└── src
    ├── main
    │   └── java
    │       └── com
    │           └── learn
    │               └── idea
    │                   └── App.java
    └── test
        └── java
            └── com
                └── learn
                    └── idea
                        └── AppTest.java
```

双击打开`maven-app.ipr`后IDEA就自动导入了此项目。

##### Gradle 生成IDEA工程文件

```shell
⇒  gradle -v
Gradle 5.6.2
⇒  mkdir gradle-app
⇒  cd gradle-app
⇒  gradle init --type java-application --dsl groovy --package com.learn.idea --project-name gradle-app --test-framework junit-jupiter
```

修改`build.gradle`，在`plugins`里添加`id: 'idea'`：

```groovy
plugins {
    // Apply the java plugin to add support for Java
    id 'java'

    // Apply the application plugin to add support for building a CLI application
    id 'application'
    id 'idea'
}
```

而后`gradle idea`生成IDEA的工程文件。双击打开`gradle-app.ipr`后IDEA就自动导入了此项目。

#### 自定义Live Templates



#### 使用IDEA进行Code Review



#### 



#### 内嵌的Tomcat





