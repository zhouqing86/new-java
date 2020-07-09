# 第五章 函数式Java编程基础知识

在Java8之前，`Java`是面向对象语言，那时的Java程序员习惯于命令式编程/指令式编程（`Imperative programming`）：

```java
List<Integer> list = Arrays.asList(1, 2, 3);
for (Integer i : list) {
    System.out.println(i);
}

int sum = 0;
for (Integer i : list) {
    sum += i;
}
```

> 命令式编程的程序代码的每一步都是指令，命令机器去执行某些操作。

Java8中引入了函数式接口，`Stream`和`Lambda`表达式之后，声明式编程（`Declarative programming`）的方式在Java语言中也变得常见：

```java
List<Integer> list = Arrays.asList(1, 2, 3);
list.forEach(System.out::println);
list.stream().reduce(0, Integer::sum);
```

> 声明式编程的程序代码描述的是目标而非流程，编译器或解释器采用一些固定的算法来达到这目标。

这一章首先将介绍函数式Java的一些基础知识：函数式接口、`Stream`和`Lambda`表达式。随后将介绍`JDK`中源代码中的一些函数式的使用以及一些函数式基本概念在`JDK`中的应用。

