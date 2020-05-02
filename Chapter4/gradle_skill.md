# 第3节：常用技巧



#### 国内Maven镜像库



#### 与IDEA集成



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

#### Maven项目转Gradle







#### 自定义Gradle插件

为了辅助开发自定义`Gradle`插件，引入`java-gradle-plugin`:

```groovy
plugins {
    id 'java-gradle-plugin'
}
```



