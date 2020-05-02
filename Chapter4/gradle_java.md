# 第2节：Java项目管理

上一节介绍了`Gradle`的基本功能，其与`Java`程序的构建没有直接的关系。本节将重点介绍如何使用`Gradle 6.3.0`来构建和管理`Java`项目。

#### 创建项目

执行`gradle init`可以创建`Java`项目，中间会有很多选项：

```shell
⇒  gradle init

Select type of project to generate: #项目的类型
  1: basic #基础项目，生成的项目将不包括任何的实现/测试代码
  2: application #应用程序，如客户端程序或服务端程序
  3: library #如果是编写供应用程序使用的Java库，选择这个选项
  4: Gradle plugin
Enter selection (default: basic) [1..4] 2

Select implementation language: #项目使用的语言
  1: C++ 
  2: Groovy
  3: Java
  4: Kotlin
  5: Swift
Enter selection (default: Java) [1..5] 3

Select build script DSL: #build.gradle使用的语言
  1: Groovy
  2: Kotlin
Enter selection (default: Groovy) [1..2] 1

Select test framework: #单元测试使用的库
  1: JUnit 4
  2: TestNG
  3: Spock
  4: JUnit Jupiter #即Junit5
Enter selection (default: JUnit 4) [1..4] 4

Project name (default: learning-gradle-java): #项目名

Source package (default: learning.gradle.java): com.newjava.gradle #项目默认的包名


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

#### 依赖管理

##### java插件的依赖管理

在创建的`learning-gradle-java`项目的`build.gradle`中，可以找到依赖相关的`Groovy`代码段：

```groovy
dependencies {
    // 当前应用程序的依赖
    implementation 'com.google.guava:guava:28.2-jre'

    // 使用Junit5来进行单元测试
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.6.0'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.6.0'
}
```

这里面有三个类似语法糖的东西`implementation`、`testImplementation`和`testRuntimeOnly`。在`Gradle`中叫做依赖配置（`dependency configuration`）。`Gradle`通过依赖配置来确定对应的依赖项的使用范围或对依赖进行分组，这里的三个依赖配置是`java`插件提供的，在创建的项目的`build.gradle`中，引入了`java`插件：

```groovy
plugins {
    // 使用java插件来支持Java语言程序的构建
    id 'java'

  	//使用application插件来支持构建客户端支持的应用
    id 'application'
}
```

`java`插件针对Java程序构建运行过程（编译、打包、运行）的对第三方依赖库有所不同，定义了许多依赖配置，依赖配置的细分好处多多，譬如减少了不同场景的第三方依赖库因而提高了编译速率，也会减小发布包的大小。

依赖配置分为两大类，分别针对实现代码的依赖配置和测试代码的依赖配置。

关于实现代码的依赖：

- `implementation`是使用最多的依赖配置，`Java`应用程序实现代码中如果依赖了第三方库，这个第三方库一般都使用`implementation`依赖，如应用程序中如果使用了`guava`库定义的一些工具类。

- 当某个依赖的第三方库只在编译字节码时使用，在运行时并不需要，则使用`compileOnly`，如`Lombok`库协助将简化的`Java`实体类源码编译成字节码，在编译时添加`setter`和`getter`方法等；又如一些库虽然编译和运行时都需要，但是开发人员知道在运行环境的`Class Path`中已经准备好了这些库，如`servlet-api`库一般`servlet`容器（`tomcat`或`jetty`）都会提供，所以即便应用程序中使用了`HttpServletRequest`等，也只需要`compileOnly`依赖配置来修饰`servlet-api`库。

- 如果第三方库在编译时并不需要，只在运行时需要，则可以使用`runtimeOnly`，譬如将接口和实现拆分成两个库，代码中只使用接口，而实现类通过依赖注入(`XML`文件)或SPI（`Service Loader`)的方式在运行时被实现代码调用，则实现类的库就是只是运行时需要的库。

- `annotationProcessor`依赖配置是用来分类注解处理的库，一些库的功能是处理某些注解，譬如编译检查、生成代码、修改代码，如`Lombok`库提供注解处理器去处理应用程序`@Data`注解等，由于`@Data`注解也是`Lombok`库提供的，编译时，所以在`Lombok`的依赖配置是：

  ```groovy
  compileOnly 'org.projectlombok:lombok:1.18.12'
  annotationProcessor 'org.projectlombok:lombok:1.18.12'
  ```

- `Gradle`早期版本提供的一些粗粒度依赖配置，如`compile`，`runtime`将被废弃。

- `compileClassPath`依赖配置继承了`compile`、`compileOnly`和`implementation`配置，意味着其将包含这三个配置锁修饰的所有第三方库，这里可以自定义`Gradle`任务来打印`compileClassPath`解析出来的所有依赖。

  ```groovy
  task printCompileClassPath {
    doLast {
      println project.configurations['compileClasspath'].resolve()
      println project.configurations.compileClasspath.each { println it}
    }
  }
  ```

- `runtimeClasspath`依赖配置继承了`runtimeOnly`, `runtime`, `implementation`的配置。

关于测试代码的依赖：

- `testImplementation`依赖继承了`implmentation`的配置，很显然测试代码依赖了实现代码。
- `testCompileOnly`, `testRuntimeOnly`, `testCompileClasspath`, `testRuntimeClasspath`等依赖配置的含义与实现代码的依赖配置类似。
- 初始项目中的第三方库`junit-jupiter-engine`被`testRuntimeOnly`修饰，因为其是用来发现和执行`Junit5`单元测试用例的，其实现了`TestEngine`接口，进而能够被IDE或者Gradle之类的工具调用进而运行单元测试。

##### java-library插件的依赖管理

在初始化项目适，我们选择项目类型时选择的了`application`，如果选择的是`library`来编写`Java`库，则其将使用插件`java-library`，这个插件是对`java`插件的扩展。

其引入了`api`依赖配置，`api`与`implementation`类似，对于实现代码所依赖的第三方库，两者都可用，但是两者也有很大的区别，这种区别使得开发人员需要根据不同的第三方库在当前项目的使用情况来自行选择依赖配置。

- `api`依赖配置修饰的第三方库将暴露给当前库的使用者，假设当前编写的Java库为B，其使用了第三方库C的代码（在B的`build.gradle`中使用`api`以来配置来修饰库C），那么当项目A在`build.gradle`中声明了对库B的依赖后，不需要再声明对库C的依赖就可以使用库C中的类型/接口。意味着`api`依赖配置具有传递性。
- 与`api`不同，`implementation`依赖配置修饰的库C不会暴露给使用者A，A如果向使用库C的类型/接口，必须显示的在项目的`build.gradle`中声明对库C依赖。
- 什么时候使用`api`，什么时候使用`implementation`的原则是：如果库B对库C的仅仅是内部代码使用（譬如对外接口的返回值类型不是库C定义的类型/接口），则使用`implementation`，否则使用`api`。

##### 第三方依赖库的下载

在`build.gradle`的`dependencies`中声明了第三方库的依赖配置，这些第三方库可以是本地已经下载好的，也可以子项目，更多的是需要下载的第三方库：

```groovy
implementation 'com.google.guava:guava:28.2-jre' //需要下载的库
implementation project(':subproject1') //依赖本地的子项目
implementation files('libs/mysql-connector-java-8.0.19.jar') //依赖本地的JAR包
implementation fileTree('libs') { include '*.jar' } //依赖libs目录下的所有JAR包
```

对于需要下载的库，在初始化项目中默认是去`Jcenter`库里下载：

```groovy
repositories {
  	// 默认jcenter地址为https://jcenter.bintray.com
    jcenter()
}
```

- 可以将`jcenter()`替换为`mavenCentral()`，则去`Maven`中心库去下载第三方依赖库，`Maven`中心库的地址为[https://repo.maven.apache.org/maven2/](https://repo.maven.apache.org/maven2/)。

- 还可以将`jcenter()`替换为`google()`，则去`Google`的`Maven`镜像库里去下载第三方依赖库，其地址为[https://maven.google.com/](https://maven.google.com/)。

- 还可以同时在`repositories`中设置多个库，第三方依赖的下载将选择库从上往下尝试下载。

- 还可以自定义第三方库的下载地址，譬如国内常用的阿里的`Maven`镜像库：

  ```groovy
  maven {
  	url 'https://maven.aliyun.com/repository/central'
  }
  ```

- 还可以使用本地目录，如`mavenLocal()`将默认使用本地的`Maven`库目录，默认目录是`~/.m2/repository`；或者直接使用:

  ```groovy
  maven {
  	url uri('/opt/mvn-repo')
  }
  ```

`Gradle`能够从这么多地方下载，那么其下载哪了？默认其下载到字节的缓存目录`~/.gradle/caches`中，注意其与`mavenLocal`是不同的目录。

#### 构建/测试/发布

##### 构建与测试

创建项目后，就可以运行一些`gradle`命令来执行测试/构建任务：

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

  这里的`assemble`是一个生命周期任务，其依赖于`java`插件的`jar`任务，以及依赖于`application`插件的`distZip`和`distTar`任务。

  - `java`插件的`jar`任务将生成可以发布的`JAR`包，`JAR`包的文件是将项目的实现代码编译的字节码加上相应的资源文件，并不会包括依赖的第三方库。
  - `distZip`任务依赖了`java`插件提供的`jar`任务和`application`插件自身提供的`startScripts`任务，其最终会生成一个Zip包，Zip包中包括`jar`任务生成的`JAR`包，运行时锁依赖的所有`JAR`包，以及操作系统相关的程序启动脚本（给Linux系统下可用的`Shell`脚本和Windows系统下可用的`Bat`脚本）。
  - `distTar`任务与`distZip`类似，不过其生成的是`tar`包。

  > 查看某个任务都依赖了哪些任务，可以通过添加`--dry-run`来查看，如`gradle assemble --dry-run`。

- `gradle build`或`./gradlew build`命令既会运行测试，也会生成程序发布包，因为其依赖于`check`任务和`assemble`任务，而`check`任务又依赖于`test`任务。

##### JAR包发布

关于如何解决如何发布`JAR`包的问题，引入`maven-publish`插件，如：

```groovy
plugins {
	id 'maven-publish'
}
version '0.0.3'
group 'com.newjava.gradle' 

publishing {
    publications {
        local(MavenPublication) {
            // version '0.0.4' //Project的version属性已经赋值，这里就可以不用赋值了
            // groupId 'com.newjava.gradle' //Project的group属性已经赋值，这里不用再赋值
            // artifactId 'learning-gradle-java' //默认使用settings.gradle里的rootProject.name
            from components.java
        }
    }

    repositories {
        maven {
            name = 'myRepo'
            url = "file://opt/mvn-repo" //发布到本地目录
        }
    }
}
```

- `components.java`是`java`插件提供的`components.java`即包含`jar`任务生成的`JAR`包，也包括此`JAR`包运行时依赖的第三方库的信息。

- 运行`gradle publishToMavenLocal`或`./gradlew publishToMavenLocal`将发布`JAR`包到`~/.m2/repository`中。

  ```shell
  ⇒  tree ~/.m2/repository/com/newjava/gradle/learning-gradle-java/
  ├── 0.0.1
  │   ├── learning-gradle-java-0.0.1.jar
  │   ├── learning-gradle-java-0.0.1.module
  │   └── learning-gradle-java-0.0.1.pom
  └── maven-metadata-local.xml
  ```

- 运行`gradle publish`或`./gradlew publish`将发布`JAR`包到`repositories`中定义的库里：

  ```shell
  ⇒  tree /opt/mvn-repo/com/newjava/gradle/learning-gradle-java
  /opt/mvn-repo/com/newjava/gradle/learning-gradle-java
  ├── 0.0.1
  │   ├── learning-gradle-java-0.0.1.jar
  │   ├── learning-gradle-java-0.0.1.jar.md5
  │   ├── learning-gradle-java-0.0.1.jar.sha1
  │   ├── learning-gradle-java-0.0.1.jar.sha256
  │   ├── learning-gradle-java-0.0.1.jar.sha512
  │   ├── learning-gradle-java-0.0.1.module
  │   ├── learning-gradle-java-0.0.1.module.md5
  │   ├── learning-gradle-java-0.0.1.module.sha1
  │   ├── learning-gradle-java-0.0.1.module.sha256
  │   ├── learning-gradle-java-0.0.1.module.sha512
  │   ├── learning-gradle-java-0.0.1.pom
  │   ├── learning-gradle-java-0.0.1.pom.md5
  │   ├── learning-gradle-java-0.0.1.pom.sha1
  │   ├── learning-gradle-java-0.0.1.pom.sha256
  │   └── learning-gradle-java-0.0.1.pom.sha512
  ├── maven-metadata.xml
  ├── maven-metadata.xml.md5
  ├── maven-metadata.xml.sha1
  ├── maven-metadata.xml.sha256
  └── maven-metadata.xml.sha512
  ```

  注意如果想发布`JAR`包到`~/.m2/repository`中，需要将`mavenLocal()`添加到`repositories`的中。

- 如果要发布到远端库，则需要设置用户名密码，常见的方式是通过参数传递的方式将用户名密码传递给`publish`任务，所以一般`repository`的定义如下：

  ```groovy
  repositories {
      maven {
          credentials {
              username project.someUsername
              password project.somePassword
          }
          if (project.version.endsWith("-SNAPSHOT")) {
              url project.someSnapshot
          } else {
              url project.someRelease
          }
      }
  }
  ```

  而在`gradle.properties`中会定义成:

  ```properties
  someUsername=xxx
  somePassword=xxx
  someSnapshot=url to snapshot
  someRelease=url to release
  ```

  这时，可以通过命令行参数来覆盖`gradle.properties`中的配置，如`gradle -PsomeUsername=realUsername`。具体的参数覆盖方面的知识可参考本章第一节内容。

  另注意的一点是对于`Java`来说，`Snapshot`库往往是是版本可以更新覆盖的，而`Release`库的版本是唯一且不能更新的，所以在`repositories`的定义中对其做了区分。

- 如果项目中有多个包需要发布，譬如这里把`distZip`生成的`zip`包和`distTar`任务生成的`Tar`包都发布到库中，则在`publishing`中可以定义：

  ```groovy
  publications {
      publish1(MavenPublication) {
          from components.java
          artifact distZip
        	artifact distTar
      }
  }
  ```

  运行`gradle publish`后就会发现`tar`包和`zip`包都发布到了`repositories`定义的库中。

  > `publications`中可以定义多个类型为`MavenPublishcation`的配置项，也可以根据需要定义不同的`MavenPublish`可以发布到不同的库中，感兴趣的读者可以查看文档[https://docs.gradle.org/current/userguide/publishing_customization.html](https://docs.gradle.org/current/userguide/publishing_customization.html)

#### 基础java插件

##### 源集

`Gradle`在`Java`项目中引入了源集（`Source Set`）的概念，因为在`Java`项目中，代码文件和资源文件往往逻辑上是可以被分组的，如可分组为应用实现代码、单元测试和集成测试。这些分组逻辑上也有不同的依赖和`Classpath`：

- 默认项目中有一个`main`源集，其代表着所有实现代码相关的文件，默认目录`src/main/java`下的所有文件，`compileJava`任务、`processResources`任务、`compileOnly`依赖配置、`implementation`依赖配置等都是基于`main`源集。

- `Java`插件还创建了一个`test`源集，其代表着单元测试相关的文件，默认为目录`src/test/java`下的所有文件，`compileTestJava`任务、`processTestResources`任务、`testcompileOnly`依赖配置、`testImplementation`依赖配置等都基于`test`源集。

- 可以修改源集的默认目录，如在`build.gradle`中：

  ```groovy
  sourceSets {
      main {
          java {
              srcDirs = ['src/java']
          }
          resources {
              srcDirs = ['src/resources']
          }
      }
      test {
          java {
              srcDirs = ['test/java']
          }
          resources {
              srcDirs = ['test/resources']
          }
      }
  }
  ```

> 源集本质上是一个`SourceSet`类，其有很多默认属性，如 `compileClasspath`、`runtimeClasspath`、  `java`、 `java.srcDirs`、`resources`、`resources.srcDirs`、`output`等，具体可查看官方文档[https://docs.gradle.org/current/userguide/java_plugin.html](https://docs.gradle.org/current/userguide/java_plugin.html)

##### 定义的任务

`java`插件被用来作为`Java`等基于`JVM`的语言的项目最基本插件，其在`base`插件的基础上定义了一套自己的生命周期任务：

- `assemble`任务，其将依赖于非生命周期任务`jar`，以及所有能够生成包且和`archives`建立了管理的包
- `check`任务，其依赖于非生命周期任务`test`
- `build`任务，其依赖于`assemble`和`check`生命周期任务
- `buildNeeded`任务，其依赖于`build`生命周期任务，其将`build`当前项目以及其所依赖的所有项目
- `buildDependents`任务，其依赖于`build`生命周期任务，其将`build`当前项目以及依赖此项目的其他项目

其他一些常用的非周期任务：

- `compileJava`将使用默认编译在`src/main/java`中的源代码，其更强大的是会依赖于所以对当前项目`classpath`有贡献的任务
- `processResources`将默认拷贝`src/main/resources`下的资源到`build`目录下的相应目录
- `classess`任务依赖于`compileJava`和`processResources`
- `jar`任务依赖于`classes`，将编译的代码和拷贝的资源文件打包成`JAR`包
- `javadoc`生成当前项目的`Java`文档
- `test`任务执行单元测试
- `clean`将删除`build`目录

> 所有的任务都生成了以`clean`为前缀的任务，如`cleanCompileJava`将清除`compileJava`任务在`build`目录下产生的文件。

##### 约定由于配置

`java`插件也定义了很多默认的约定，如：

- 默认的测试报告（如`.html`）将生成在`$buildDir/reports`目录下

- 默认的测试结果（如`.xml`）将生成在`$buildDir/test-results`目录下

- 默认的文档生成在`$buildDir/docs`目录下

- 默认将为`JAR`包生成`manifest`文件`META-INF/MANIFEST.MF`文件，可以在`build.gradle`中自定义`manifest`文件的属性如:

  ```groovy
  jar {
      manifest {
          attributes 'Main-Class': 'com.newjava.gradle.App'
      }
  }
  ```

  使用`gradle jar`生成的`JAR`包此时可以直接使用`java -jar build/libs/learning-gradle-java-0.0.1.jar`运行此`JAR`包。

##### java扩展

`java`插件也定义了一个`java`扩展，意味着在`build.gradle`，直接可以配置一些`JAVA`语言相关的属性，如项目使用的`JAVA`版本:

```groovy
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
```

#### 其他插件

一些常用且建议了解的`JAVA`语言标准插件：

- `java-library`插件是对`java`插件的扩展，其继承了`java`插件的所有功能，其用于辅助开发`Java`库项目， 如其提供了`api`依赖配置来处理`JAR`包之间的依赖传递问题。

- `application`插件是为了更方便的创建可执行的`JAVA`程序，将提供任务生成可便携且一键执行的`TAR`包或`ZIP`包，生成的包里将包含操作系统相关的应用程序启动脚本。其提供了`application`扩展以便于设置相关参数：

  ```groovy
  application {
      mainClassName = 'com.newjava.gradle.App'
      applicationDefaultJvmArgs = ['-Dgreeting.language=en']
  }
  ```

- `war`插件也是对`java`插件的扩展，其默认`Web`应用资源（如`JSP`文件，`HTML`文件等）放置在目录`src/main/webapp`中，其提供了`war`任务来生成`Web`应用程序包`WAR`包。

- `java-platform`插件用来声明平台库，平台库是一种不包含`JAVA`源代码的库，其仅用于来引用其他库，譬如多个子项目依赖许多共同的第三方库，则可以使用平台库来声明共同依赖的第三方库以及其版本。通过此插件可以生成`Gradle`模块化元数据（`Gradle Module Metadata`）或`Maven`物料清单（`Maven BOM`）。具体内容下一节将会介绍。

- `maven-publish`提供了将构建的`JAVA`库发布到`Maven`库的能力，其提供了`publishing`扩展来配置需要发布的产品包以及目的`Maven`库相关信息。本节前面已经对其做了较详细的介绍。

- `findbugs`插件、`checkstyle`插件分别为`JAVA`语言代码静态检查工具`FindBugs`和`CheckStyle`提供支持

- `jacoco`插件为测试覆盖率工具`JaCoCo`提供支持

> 对于标准插件，只需要在`plugins`块中声明如`id 插件`名即可， 不需要考虑其版本信息等。

一些非标准插件：

- `gretty`插件为`JAVA`语言`Web`程序在`jetty`容器或`tomcat`容器下运行提供了支持

- `spring-boot`插件提供了对`Spring Boot`框架的支持

- `gradle-one-jar`插件可以将依赖的第三方库都打包到`JAR`包中

  ```groovy
  task awesomeFunJar(type: OneJar) {
      mainClass = 'com.newjava.gradle.App'
  }
  ```

> 非标准插件的引入与标准插件引入不同，其需要提供插件的全称和版本，如`id "com.github.onslip.gradle-one-jar" version "1.0.5"`。









#### 







