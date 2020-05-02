# 第1节：Gradle入门

#### 安装Gradle

通过链接[https://services.gradle.org/distributions/gradle-6.3-all.zip](https://services.gradle.org/distributions/gradle-6.3-all.zip)下载`Gradle`的全量包。解压后就可以直接使用，笔者在`MacOS`系统中解压后，运行如下命令检查当前环境:

```shell
⇒  java -version
java version "1.8.0_121"
Java(TM) SE Runtime Environment (build 1.8.0_121-b13)
Java HotSpot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)
⇒  /opt/gradle/gradle-6.3/bin/gradle -version

Welcome to Gradle 6.3!

Here are the highlights of this release:
 - Java 14 support
 - Improved error messages for unexpected failures

For more details see https://docs.gradle.org/6.3/release-notes.html

------------------------------------------------------------
Gradle 6.3
------------------------------------------------------------

Build time:   2020-03-24 19:52:07 UTC
Revision:     bacd40b727b0130eeac8855ae3f9fd9a0b207c60

Kotlin:       1.3.70
Groovy:       2.5.10
Ant:          Apache Ant(TM) version 1.10.7 compiled on September 1 2019
JVM:          1.8.0_121 (Oracle Corporation 25.121-b13)
OS:           Mac OS X 10.13.6 x86_64
```

笔者当前环境使用的Java版本是`1.8.0_121`，Gradle版本`6.3`。此版本的Gradle已经可以支持到Java的`14`版本，在上面打印中，还可以看到`Kotlin`，`Groovy`和`Ant`的版本，这里的版本信息到底意味着什么呢？让我们继续学习完后面的内容再来回答这个问题。

另外既然可以通过绝对路径的方式执行gradle命令，那也可以通过将`gradle`命令的路径设置进`PATH`，就可以直接执行`gradle -version`了，Linux/MacOS下设置`PATH`的方式：

```shell
export PATH=$PATH:/opt/gradle/gradle-6.3/bin
```

#### Gradle任务

##### 创建任务

执行`gradle`命令时候，默认会读取并解析当前目录下的构建配置文件`build.gradle`。这里先创建一个`build.gradle`，创建一个任务如下：

```groovy
task helloWorld {
  description "Task description"
  doLast {
    println 'Hello world.'
  }
}
```

这时执行`gradle hello`、`gradle hW`或`gradle helloWorld`中的任意一个，都会执行`helloWorld`这个任务，因为`gradle`命令有比较强大的搜索功能：

```shell
⇒  gradle hW

> Task :helloWorld
Hello world.

BUILD SUCCESSFUL in 715ms
1 actionable task: 1 executed
```

从这个简单的任务，可以获取到的知识点：

- `build.gradle`中的语句是遵从`Groovy`语法，`Groovy`是一门基于JVM平台的动态编程语言，语法非常简练和优美，其能与Java语言无缝对接。这里的`println`与Java语言中的`System.out.println("hello world")`是对等的。

- `build.gradle`隐含有一个`Project`对象`project`，这里的`helloWorld`任务全称应是`project.helloWorld`，只不过`project`通常被省略掉了。

- `task`意味着创建一个`Task`对象，而`doLast`则为`Task`的一个方法，所以上面`helloWorld`的定义也可以这样来：

  ```groovy
  task helloWorld
  helloWorld.description = "Task description"
  helloWorld.doLast {
    println 'Hello world.'
  }
  ```

  也可以这样定义任务：

  ```groovy
  tasks.create('helloWorld') {
    description "Task description"
    doLast {
      println 'Hello world.'
    }
  }
  ```

- `{}`中的代码块在`Groovy`中表示闭包，其是一个`Closure`的类，可以看做Java的一个函数接口。闭包中有一个隐含的参数`it`，与Java类中的`this`类似，这里的闭包也不需要`=`显示语法糖来赋值，因为其默认就是将闭包赋值给其前面定义的变量。

- 调用`gradle helloWorld`时，会先去执行`helloWorld`任务的闭包中没有定义在方法中的语句，而后在依次执行`doFirst`方法，`doLast`方法。

- 任务闭包中的语句也是`project`的配置，意味着在执行任何`gradle`命令时，都会先进行配置，所以如果直接在`task`的闭包里的`println`语句在所有的`gradle`命令执行时都会被执行：

  ```groovy
  task helloWorld {
    println "任何gradle命令执行时都会执行此语句"
  }
  ```

  > 因此，任务执行语句一般建议是写在doLast方法里

- 创建任务不仅仅可以使用`Groovy`语法，`Gradle`自从版本`5.1`以后，也支持`Kotlin`的语法，如果要使用`Kotlin`语法，创建`build.gradle.kts`文件如:

  ```kotlin
  tasks.create("helloWorld"){
    doLast {
      println("Hello world.");
    }
  }
  ```

  而后执行`gradle -b build.gradle.kts hello`就可以看到结果。

虽然`Gradle`即支持`Kotlin`语法也支持`Groovy`语法，笔者本章将主要使用`Groovy`语法。

使用`gradle tasks --all`将列出当前项目的所有任务，既有`Gradle`自带的任务，也有自定义的任务。对于自定义的任务，如果设置了`description`，则也会将`description`的内容打印出来。

##### 属性变量

使用`gradle properties`命令查看当前项目下可用的属性变量。当前我们没有定义任何属性变量，但是此命令会打印出很多变量，因为`Gradle`自身会内嵌很属性变量。

如果需要在`build.gradle`中自定义变量，需要定义在`ext{}`闭包中，或给变量添加前缀`ext.`。如：

```groovy
ext {
  personName = "Somebody"
}
task helloWorld
helloWorld.doLast {
  println "Hello $personName"
}
```

> `project.ext.personName`，`project.personName`以及`personName`都可以读取到这个自定义变量的值。

除了能够直接在`build.gradle`中自定义变量，也可以创建`gradle.properties`文件，在此文件中自定义变量且赋值，如:

```properties
anotherPersonName=Zhang San
```

在`build.gradle`中就可以和使用`ext{}`闭包里的自定义变量一样去使用此变量。

```groovy
helloWorld.doLast {
  println "Hello ${anotherPersonName}"
}
```

定义在`gradle.properties`中的变量的好处是其能通过命令行参数来覆盖：

```shell
gradle -PanotherPersonName="Li Si" helloWorld 
```

这里的`-P`以为着其是`Project`的变量，命令行的`anotherPersonName`的值将覆盖`gradle.properties`中的值。注意如果`build.gradle`中也设置了`anotherPersonName`的值，则其设置的值优先级最高。

还可以通过命令行`-D`选项（`Java`系统属性）来赋值`gradle`自定义变量，变量名需要添加前缀`org.gradle.project`：

```shell
gradle -Dorg.gradle.project.anotherPersonName="Li Si" hello
```

还可以通过环境变量来赋值`gradle`自定义变量，环境变量名需要添加前缀`ORG_GRADLE_PROJECT_ `:

```shell
export ORG_GRADLE_PROJECT_anotherPersonName="Li Si"
gradle hello
```

`Gradle`的自定义变量相信读者已经基本了解，那么现在有个问题，自定义的变量名与`gradle`的内嵌变量名冲突怎么办？譬如`version`，其为`gradle`的内嵌属性，`build.gradle`定义如下：

```groovy
version = "0.0.2"
ext {
  version = "0.0.1"
}
task printVersion {
  doLast {
    println "version is ${version}"
    println "project.version is ${version}"
    println "project.ext.version is ${version}"
  }
}
```

运行`gradle -q pV`的结果如下：

```shell
⇒  gradle -q pV
version is 0.0.2
project.version is 0.0.2
project.ext.version is 0.0.2
```

> 关于`Gradle`的`Project`对象的内嵌属性，可以查看文档[https://docs.gradle.org/current/dsl/org.gradle.api.Project.html](https://docs.gradle.org/current/dsl/org.gradle.api.Project.html)

##### 自定义函数

在`build.gradle`中，可以自定义函数，如：

```groovy
def add(num1, num2) {
  return num1 + num2;
}

task callCustomMethod {
  doLast {
    println "Add result: ${add(1,2)}"
  }
}
```

这里的`add`函数可以这样定义：`def add = { num1, num2 -> num1 + num2}`。

动态创建任务的例子：

```groovy
def createTask(taskName) {
  tasks.create(taskName){
    doLast {
      println "Execute task $taskName"
    }
  }
}

["autoTask1", "autoTask2"].each {
  createTask it
}
```

##### 自定义类

这里在`build.gradle`中自定义一个`GreetingTask`，其将继承`Gradle`自带的`DefaultTask`类:

```groovy
class GreetingTask extends DefaultTask {
    @Input
  	@Option(option = "person-name", description="Set Person Name")
    String personName = ""

    @TaskAction
    def greet() {
        println "Hello $personName"
    }
}
task greeting(type: GreetingTask) {
  personName = 'Zhang San'
  doLast {
    println "doLast"
  }
}
```

- `@TaskAction`注解的方法在任务执行时会被调用，上面例子中会先调用`greet`方法，而后再调用`task`中定义的`doLast`方法

- `@Input`注解的属性是可以被设置的，`@Input`注解也可以注解到`set`或`get`方法上
- `@Option`注解使得其可以通过命令行传入参数值，如`gradle greeting --person-name "Li Si"`

`Gradle`默认已经定义了很多有用的类：

- `Copy`类可以方便的对文件或目录进行拷贝：

  ```groovy
  task copyDocs(type: Copy) {
       from('src') {
          include '**/*.txt' //拷贝src目录以及其所有子目录下的后缀为.txt的文件
       }
       from('docs') {
          exclude '**/*.md' //拷贝docs目录以及其所有子目录下的后缀不为.md的文件
          rename 'DOCS_(.*)', '$1' //如果文件名的前缀为DOCS_，则拷贝时文件名去掉DOCS_
       }
       into 'dest' //上面from定义的文件拷贝到dest目录下
   }
  ```

- `Exec`类可以执行命令行命令：

  ```groovy
  task commandLineTask(type:Exec) {
     workingDir 'src'
     commandLine 'ls', '-lart'
   }
  ```

  注意每一个`Exec`任务只能有一个`commandLine`，所以建议如果要执行`shell`脚本，建议把要执行的`shell`脚本写在一个`shell`文件中，而后通过`commandLine`来调用脚本文件。

> 查看默认的任务相关的类和接口定义： [https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/package-summary.html](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/package-summary.html)

##### 任务依赖关系

如果任务之间有依赖关系，如两个任务`task1`和`task2`，`task2`需要依赖`task1`执行，`build.gradle`中：

```groovy
task task1 {
  doLast {
    println "Task1"
  }
}

task task2 {
  doLast {
    println "Task2"
  }
}
```

- 可以在`build.gradle`中添加`task2.dependsOn "task1"`就建立了依赖关系
- 也可以直接在`task2`的闭包里添加`depondsOn "task1"`语句
- 还可以在定义`task2`时确认其依赖关系，如`task task2(dependsOn: ["task1"]) `

> 任务之间如果发生了循环依赖，使用`gradle`来执行任务时会报错，不会陷入死循环。

建立好任务的依赖关系后，可以设置`gradle`命令的默认任务，在`build.gradle`中添加：

```groovy
defaultTasks 'task2'
```

这样在执行`gradle`没有指定具体任务时会默认执行任务`taks2`。

#### 子项目管理

如果当前项目下有多个子项目，目录结构如：

```shell
├── build.gradle #主项目的build.gradle文件
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle #多项目管理的关键文件
├── subproject1
│   └── build.gradle #子项目1的build.gradle文件
└── subproject2
    └── build.gradle #子项目2的build.gradle文件
```

- `settings.gradle`中也是支持`Groovy`的语法，整个项目（包括子项目）只能有一个`settings.gradle`文件，多个子项目必须使用此文件，单个项目可以没有此文件：

  ```groovy
  rootProject.name = 'learning-gradle' //主项目的项目名
  include 'subproject1', 'subproject2' //包含的子项目
  ```

- 每个子项目都可以有一个`build.gradle`文件去定义自己的任务，如`subproject1/build.gradle`:

  ```groovy
  task hello {
    doLast {
      println "This is subproject1"
    }
  }
  ```

  通过`gradle subproject1:hello`来执行此任务。

- 也可以在主项目的`build.gradle`文件中定义子项目的任务：

  ```groovy
  allprojects {
    task commonTask { //所有项目中都会创建commonTask任务
      doLast {
        println "Common Task"
      }
    }
  }
  
  project(':subproject1') {
    task specificTask { //仅仅子项目1创建specificTask任务
      doLast {
        println "Specific Task"
      }
    }
  }
  ```

#### Gradle文件模块化支持

`build.gradle`中可以导入其他的`Gradle`文件，这里创建一个`other.gradle`文件:

```groovy
task other {
  doLast {
    println "Task in other.gradle"
  }
}
```

在`build.gradle`中导入的方式:

```groovy
apply from: 'other.gradle' //主项目中导入other.gradle
project(':subproject2') {
  apply from: '../other.gradle' //子项目中导入other.gradle
}
```

开发者可以将一些常用且可共用的任务抽取成独立的`Gradle`文件。甚至进而可以将这些独立的`Gradle`文件进而打包成`Plugin`。

#### Gradle的Base插件

`Gradle`自带了`base`插件提供了一些共用功能，在`build.gradle`中引入：

```groovy
plugins {
    id 'base'
}
```

`base`插件定义了基本的生命周期任务（`Lifecycle tasks`），生命周期任务可以用来代表：

- 工作流步骤，譬如，使用`check`任务来运行所有检查/验证
- 可构建的东西，譬如，使用`debug32MainExecutable`任务来创建可供调试的32位可执行文件
- 可以代表很多相同逻辑执行的任务，譬如，使用`compileAll`任务来表示运行所有编译任务

`base`插件默认定义了`assemble`，`build`和`check`三个生命周期任务以及其依赖关系，开发人员编写自己的构建任务或插件时应该考虑与生命周期任务的关联性：

- `check`任务表示验证，譬如运行测试测试的任务就应该与其建立关系，通过`check.dependsOn(SomeTestTask)`
- `assemble`任务表示装配，生产发布包（如`zip`,`jar`包等）的任务应该与其建立关系，通过`assemble.dependsOn(SomeAssembleTask)`
- `build`表示构建，测试、生产产品包、生成文档都应该与其关联。但是很少直接将任务与`build`直接关联，因为其默认依赖于`check`和`assemble`任务

`base`插件还定义了一些非生命周期任务和任务类：

- 任务类`Zip`可以方便的创建`zip`压缩包：

  ```groovy
  task simpleZip(type: Zip) {
    from 'src'
    archiveName 'learning-gradle.zip' //src目录下的文件默认打包并放到build/distributions目录下
  }
  ```

- 任务类`Tar`可以方便的创建`tar.gz`压缩包：

  ```groovy
  task simpleTar(type: Tar) {
    from 'src'
    archiveName 'learning-gradle.tar.gz'//src目录下的文件默认打包并放到build/distributions目录下
  }
  ```

- `clean`任务将删除`buildDir`变量设置的目录，默认`buildDir`为当前项目下的`build`目录。如果单独只想清除`simpleZip`或`simpleTar`生成的包，可以通过`cleanSimpleZip`任务或`cleanSimpleTar`任务来做清除。笔者并没有定义两个`clean`任务，而是`base`插件自动为其生成了`clean`任务。

`base`插件还有`archive`的概念，其就是文件，如`Tar`和`Zip`都属于可以生成`archive`的任务类，输出的文件都是`archive`：

```groovy
artifacts {
  archives simpleTar
  archives simpleZip
}
```

`gradle build`或`gradle buildArchives`都会同时创建`learning-gradle.zip`和`learning-gradle.tar.gz`。

构建了`archive`后，`base`插件还支持其上传到存储库中，譬如上传到本地某个目录：

```groovy
uploadArchives {
  repositories {
    flatDir {
        name "fileRepo"
        dirs "repo"
    }
  }
}
```

运行`gradle uploadArchives`即可将构建的`archive`发布到`repo`目录下，其在发布前会先构建`archive`。

> 注意：`uploadArchives`任务将在未来的`Gradle 7`中移除。

#### 约定大于配置

在`Gradle`中，很多任务和成员变量并需要显示的声明或在`build.gradle`中赋值，因为`Gradle`奉行约定大于配置（`Convention Over Configuration`）。因此`Gradle`中有大量的默认约定（`conventions`）。

如在`base`插件中:

- `archivesBaseName`默认就是取`${project.name}`:

  ```groovy
  task defaultZip(type: Zip) {
    from 'src'
  }
  ```

  `defaultZip`任务与上面定义的`simpleZip`不同之处在于其没有对`archiveName`进行复制，其将默认使用`${project.name}.zip`生成`ZIP`包。

- `archiveVersion`默认取`${project.version}`，如在`build.gradle`中赋值`version = '1.0'`，则`defaultZip`生成的包如`learning-gradle-1.0.zip`

- `buildDir`默认构建目录为`build`，不论是源码编译后的目标目录还是资源文件将要拷贝去的目录

- `distsDirName`默认生成的非库类型的产品包（如`JAR`包，`WAR`包）的发布目录名为`distributions`

- `libsDirName`默认生成的库类型的产品包（如`ZIP`包，`TAR`包）的发布目录名为`libs`

- `destinationDirectory`默认为`$buildDir/$distsDirName`或`$buildDir/$libsDirName`

#### Gradle Wrapper

是否可以在没有安装`Gradle`的情况下运行`gradle`相关命令，`Gradle Wrapper`让这成为可能。在安装有`Gradle`的机器上，在`build.gradle`所在目录，执行：

```shell
⇒ gradle wrapper --gradle-version=6.3
⇒  tree .
.
├── build.gradle
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar #包含下载gradle和调用gradle的Java类文件
│       └── gradle-wrapper.properties #定义gradle的下载路径等属性
├── gradle.properties
├── gradlew #Linux/MacOS下运行Gradle的入口
└── gradlew.bat #Windows下运行Gradle的入口
```

有了`Gradle Wrapper`后，就可以在当前目录执行`./gradlew`，就会自动开始下载Gradle版本。而后，所有的使用`gradle`执行的命令都可以通过`./gradlew`来执行。