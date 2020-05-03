# 第3节：常用技巧

本节将重点介绍如何使用`Gradle 6.3.0`来构建和管理`Java`项目的一些常用技巧和经典实践。

#### 国内镜像库

在第2节介绍使用`Gradle`下载第三方依赖库时，可以使用自定义的`Maven`库：

```groovy
maven {
	url 'https://maven.aliyun.com/repository/central'
}
```

为了提升依赖的下载速度，建议使用国内的`Maven`镜像库，如这里的阿里`Maven`镜像库。

除了对第三方`JAVA`库的下载可以自定义`Maven`库地址，也可以自定义`Gradle`插件下载的插件库地址。在`settings.gradle`中：

```groovy
pluginManagement {
    repositories {
        maven {
            url 'https://maven.aliyun.com/repository/gradle-plugin'
        }
        gradlePluginPortal() //默认https://plugins.gradle.org/m2/
    }
}
```

> `Gradle`还可以在`build.gradle`中定义`buildScript`的方式来配置插件，不过此种方式将被废弃，不建议使用。
>
> ```groovy
> buildscript {
>   repositories {
>     maven {
>       url 'https://maven.aliyun.com/repository/gradle-plugin'
>     }
>   }
>   dependencies {
>     classpath "gradle.plugin.org.gretty:gretty:3.0.2"
>   }
> }
> 
> apply plugin: "org.gretty"
> ```

#### IDE相关插件

使用`idea`插件可以很方便的生成`Intellij IDEA`项目相关的文件，引入插件:

```groovy
plugins {
  id 'java'
	id 'idea'
}
```

- 使用`gradle idea`命令将生成项目的工程文件（`.ipr`，`.iws`和`.iml`）文件，这使得工程的导入变得非常简单。如使用`Intellij IDEA`打开生成的工程文件`.ipr`即可导入整个工程到`IDEA`中。

- 使用`gradle openIdea`命令将生成`IDEA`工程文件并在`IDEA`中打开工程文件，导入项目。

- 使用`gradle cleanIdea`命令清除生成的`IDEA`工程文件。

使用`eclipse`插件或`eclipse-wtp`插件与`idea`插件类似，不过其是为了生成`Eclipse`的工程文件，可以使用`gradle eclipse`命令和`gradle cleanEclipse`命令等。

#### 区分集成测试与单元测试

一个常见的需求是将单元测试和集成测试区分开来，因为集成测试往往有依赖、需要网络请求等，需要耗费更多的时间。当然`Junit5`提供了标签的方法可以区分单元测试和集成测试，但在`Gradle`管理的项目中，更建议将单元测试的源集与集成测试的源集分开。

这里假设单元测试用的是`Junit5`单元测试框架，而集成测试仍然使用的是`Junit4`的单元测试框架。第2节已经介绍了`java`插件创建的`test`源集，这里在`build.grale`中为集成测试专门自定义`intTest`源集：

```groovy
configurations {
    intTestImplementation.extendsFrom implementation //自定义intTestImplementation依赖配置
    intTestRuntimeOnly.extendsFrom runtimeOnly //自定义intTestRuntimeOnly依赖配置
}
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.6.0' //单元测试使用Junit5
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.6.0' 
    intTestImplementation 'junit:junit:4.12' //集成测试使用Junit4
}
sourceSets {
    intTest { //默认自定义intTest源集的目录为src/intTest/java
        compileClasspath += sourceSets.main.output //修改intTest源集的compileClassPath属性
        runtimeClasspath += sourceSets.main.output //修改intTest源集的runtimeClassPath属性
    }
}
test {
    useJUnitPlatform() //Junit5默认需要在test任务中使用Junit Platform
}
task integrationTest(type: Test) { //自定义integrationTest来执行集成测试
    description = 'Runs integration tests.'
    group = 'verification'

    testClassesDirs = sourceSets.intTest.output.classesDirs //覆盖Test类型的testClassesDirs
    classpath = sourceSets.intTest.runtimeClasspath //覆盖Test类型的classpath
    shouldRunAfter test //integrationTest应在test后运行，这不是依赖关系，而是运行的弱先后关系
}
check.dependsOn integrationTest //将interationTest附着在check生命周期任务上
```

- 使用`gradle test`命令可以运行单元测试，默认运行定义在`src/test/java`目录下的单元测试用例

- 使用`gradle IntegrationTest`命令可以运行集成测试，默认运行定义在`src/intTest/java`目录下的集成测试用例，如`src/intTest/java/IntegrationTest.java`:

  ```java
  import org.junit.Test;
  import static org.junit.Assert.assertEquals;
  
  public class IntegrationTest {
      @Test
      public void testEqual() {
        assertEquals(1, 1);
      }
  }
  ```

- 使用`gradle check`命令将同时运行`test`和`integrationTest`任务，`test`任务一般情况下将运行在`integrationTest`之前， 但两个任务也可以并行运行，如果需要有严格的先后顺序，可以将`shouldRunAfter`替换为`mustRunAfter`。

`gradle IntegrationTest --dry-run`可以进一步查看`integrationTest`任务的依赖关系：

```shell
⇒  gradle integrationTest --dry-run
:compileJava SKIPPED
:processResources SKIPPED
:classes SKIPPED
:compileIntTestJava SKIPPED
:processIntTestResources SKIPPED
:intTestClasses SKIPPED
:integrationTest SKIPPED
```

可以推断出，定义了`intTest`后，其将自动生成`compileIntTestJava`任务和`processIntTestResources`任务，这是`Gradle`支持约定大于配置的又一体现。

#### 自定义Gradle插件

`Gradle`插件打包了可重用的构建逻辑，可在许多不同的项目和构建中使用。开发人员可以自定义`Gradle`插件。`Gradle`插件可以使用`Groovy`、`Kotlin`或`Java`等`JVM`语言编写，使用`Java`或`Kotlin`编写的插件性能上一般比`Groovy`编写的插件会更好一些。

##### build.gradle中定义插件

在`build.gradle`中可以编写`Java`插件：

```groovy
class GreetingPluginExtension {
    String message
    String greeter
}

class GreetingPlugin implements Plugin<Project> {
    void apply(Project project) {
        def extension = project.extensions.create('greeting', GreetingPluginExtension)
        project.task('hello1') {
            doLast {
                println "${extension.message} from ${extension.greeter}"
            }
        }
    }
}
apply plugin: GreetingPlugin

greeting {
    message = 'Hi'
    greeter = 'Gradle Plugin in build.gradle'
}
```

- 自定义的插件类需要实现`Plugin`接口，使用`apply`后将创建一个`GreetingPlugin`实例
- 定义名为`greeting`的扩展，使得`GreetingPlugin`可以被配置

##### buildSrc中定义插件

将上面定义在`build.gradle`中的插件我们抽取出来模块化。`Gradle`项目默认可以将当前项目自定义的插件放入`buildSrc`目录中：

````shell
⇒  tree buildSrc
buildSrc
├── build.gradle
└── src
    ├── main
    │   └── java
    │       └── custom.plugin
    │           ├── GreetingPlugin.java
    │           └── GreetingPluginExtension.java
    └── test
        └── java
            └── custom
                └── plugin
                    └── GreetingPluginTest.java
````

其目录结果与一个普通的`Java`项目类似：

- `build.gradle`中定义

  ```groovy
  plugins {
      id 'java'
  }
  repositories {
      jcenter()
  }
  dependencies {
      implementation gradleApi()
      testImplementation 'org.junit.jupiter:junit-jupiter-api:5.6.0'
      testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.6.0'
  }
  test {
      useJUnitPlatform()
  }
  ```

- `GreetingPlugin.java`将使用`Java`语言编写：

  ```java
  package custom.plugin;
  
  import org.gradle.api.Plugin;
  import org.gradle.api.Project;
  
  class GreetingPlugin implements Plugin<Project> {
      public void apply(Project project) {
          GreetingPluginExtension extension = project.getExtensions().create("greeting", GreetingPluginExtension.class);
          project.task("hello2")
                  .doLast(task -> {
                      System.out.println(extension.message + " from " + extension.greeter);
                  });
      }
  }
  ```

- `GreetingPluginExtension.java`：

  ```java
  package custom.plugin;
  
  public class GreetingPluginExtension {
      String message;
      String greeter;
  }
  ```

- 测试类中`GreetingPluginTest.java`:

  ```java
  package custom.plugin;
  
  import org.gradle.api.Project;
  import org.gradle.testfixtures.ProjectBuilder;
  import org.junit.jupiter.api.Test;
  import static org.junit.jupiter.api.Assertions.*;
  
  public class GreetingPluginTest {
  
      @Test
      public void greetingTest() {
          Project project = ProjectBuilder.builder().build();
          project.getPluginManager().apply(GreetingPlugin.class);
          assertNotNull(project.getTasks().getByName("hello2"));
      }
  }
  ```

- 在`buildSrc`目录中执行`gradle test`来运行测试。

在父项目的`build.gradle`中:

```groovy
apply plugin: custom.plugin.GreetingPlugin
greeting {
    message = 'Hi'
    greeter = 'Gradle Plugin in buildSrc'
}
```

而后在父项目目录执行`gradle hello2`就可以执行`buildSrc`中`GreetingPlugin`插件定义的`hello2`任务。

##### 独立的插件项目

`buildSrc`中定义的插件只能给当前项目使用，如果需要将定义的插件给更多的项目使用。则需要创建独立的插件项目。

从零开始创建`Gradle`的插件的命令：

```shell
gradle init --type java-gradle-plugin --dsl groovy --package custom.plugin --project-name custom-plugin
```

但是这里将复用`buildSrc`的代码：

- 先创建一个`greeting-plugin`的项目，将上面`buildSrc`目录的文件都拷贝到此项目中

- `build.gradle`中引入`java-gradle-plugin`插件，其将默认将`implementation gradleApi()`导入`dependencies`，且能够为`jar`任务生成插件的元文件

- `build.gradle`中引入`maven-publish`插件来发布插件到本地库（`mavenLocal()`），`build.gradle`中的内容为:

  ```groovy
  plugins {
      id 'java-gradle-plugin'
      id 'maven-publish'
  }
  repositories {
      jcenter()
  }
  dependencies {
      testImplementation 'org.junit.jupiter:junit-jupiter-api:5.6.0'
      testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.6.0'
  }
  
  test {
      useJUnitPlatform()
  }
  
  version='0.0.1'
  group='com.newjava'
  
  gradlePlugin {
      plugins {
          simplePlugin {
              id = 'com.newjava.greeting'
              implementationClass = 'custom.plugin.GreetingPlugin'
          }
      }
  }
  ```

- 添加`settings.gradle`：

  ```groovy
  rootProject.name='greeting'
  ```

插件的源代码不需要做任何修改，运行` gradle publishToMavenLocal`将发布插件到本地`~/.m2/repository`中。

在其他`Gradle`管理的项目中如何引入这个插件呢，在`build.gradle`中：

```groovy
plugins {
	id "com.newjava.greeting" version "0.0.1"
}
greeting {
    message = 'Hi'
    greeter = 'Gradle Plugin in standone plugin'
}
```

注意在`settings.gradle`中，将`mavenLocal()`添加进`repository`中：

```groovy
pluginManagement {
    repositories {
        mavenLocal()
      	gradlePluginPortal()
    }
}
rootProject.name = 'learning-gradle-java'
```

运行`gradle hello2`时将会从本地的`~/.m2/repository`下载插件`com.newjava.greeting`并执行。

> 如果想要将写的插件发布到`Gradle`的插件网站上，可以参考[https://guides.gradle.org/publishing-plugins-to-gradle-plugin-portal/](https://guides.gradle.org/publishing-plugins-to-gradle-plugin-portal/)

#### 其他

##### Maven项目转Gradle项目

在定义了`pom.xml`的项目中执行`gradle init`，`Gradle`会解析`pom.xml`中的内容并生成`build.gradle`。但是并不能保证`build.gradle`能百分之百解释正确`pom.xml`的内容，所以仍然需要开发人员的手动修改。

不过建议一直保持项目中的`pom.xml`，直到当前`Gradle`在当前项目上的使用已经稳定。

##### 项目的Gradle版本升级

如果项目中使用的是老的`Gradle`版本，需要升级为新的`Gradle`版本，常规操作步骤是：

- 运行`gradle help --warning-mode=all`来查看哪些特性已经被废弃或者标位即将废弃，根据输出的结果升级`build.gradle`中使用的插件版本。

- 运行`gradle wrapper --gradle-version 6.3`对项目使用的`Gradle`进行升级。

  