# 第2节：Lambda表达式

从Java8开始，`Lambda`表达式成为了Java语言的一等公民，前面的部分章节的已经使用了`Lambda`表达式，`Lambda`表达式让代码更加简洁易懂。

```java
lambda参数列表 -> lambda函数体
```

参数列表值有一个参数时，可以写成：

```java
a -> a + 1;
```

多个参数时，需要添加括号:

```java
(a, b) -> a + b
```

对于lambda函数体，如果需要的计算比较复杂，需要多个步骤，则可以将多行代码用大括号扩起来：

```java
a -> {
  System.out.println("Input value is " + a);
  return a+1;
}
```

#### Lambda表达式类型

`Lambda`表达式是用来表示函数式接口的，从`Lambda`表达式是无法推断其类型的，只有将`Lambda`表达式复制给某个函数式接口类型或作为参数传递时，其才有了类型。

如`Lambda`表达式`(a,b)->a+b`是没有类型的，即无法判断Lambda表达式的类型，也无法判断其使用的参数`a`和`b`的类型，当其赋值给函数接口变量时，就有了类型:

```java
//Lambda表达式为IntBinaryOperator类型函数式接口，a和b的类型为int
IntBinaryOperator addFun = (a, b) -> a + b;

//Lambda表达式为BinaryOperator<Integer>类型函数式接口，a和b的类型为Integer
BinaryOperator<Integer> addFun2 = (a, b) -> a + b;

//Lambda表达式为BinaryOperator<String>类型函数式接口，a和b的类型为String
BinaryOperator<String> addFun3 = (a, b) -> a + b;
```

> 变量`a`和`b`的值都可以根据其函数式接口推断出来

当`Lambda`表达式作为参数传入如下函数时，需要注意一些小细节:

```java
private static void method1(IntBinaryOperator fun) {
}

private static<T> void method2(BinaryOperator<T> fun) {
}
```

对于`method1`，可以直接调用如:

```java
@Test
void testInvokeMethod1() {
    LambdaTest.method1((a,b)->a+b);
}
```

而对于`method2`，由于无法无法推断出`T`的类型值，需要在传入`Lambda`表达式时显示指定变量`a`和`b`的类型:

```java
@Test
void testInvokeMethod2() {
    LambdaTest.method2((Integer a, Integer b)->a+b);
    LambdaTest.method2((String a, String b)->a+b);
}
```

#### Lambda词法作用域

`Lambda`表达式的词法作用域与Java语言中其他表达式（如`for`和`while`）的词法作用域是一样的。在定义`Lambda`表达式的语境里能获取的变量值，在`Lambda`函数体中也能获取这些变量值。

```java
public class LambdaTest {
    public static int v1 = 1;

    @Test
    void testLambdaScope() {
        int v2 = 2;
      
      	//Lambda表达式中能够获取到V1, v2, v3的值
        UnaryOperator<Integer> addFunc = param -> param + v1 + v2;
        assertEquals(4, addFunc.apply(1));
    }
}
```

> lambda参数中的变量名，以及lambda函数体中定义的变量名不能与词法作用域中已经定义的变量名相同，否则会出错。

对于上面的程序，如果在`Lambda`表达式定义和函数式接口调用之间对变量进行修改，`assertEquals`还会成立么？

```java
@Test
void testLambdaScope() {
    int v2 = 2;
    UnaryOperator<Integer> addFunc = param -> param + v1 + v2;
    v1++;
    v2++;
    assertEquals(4, addFunc.apply(1));
}
```

修改后的测试用例会编译失败，在`v2`变量上有错误提示信息：

```wiki
Variable used in lambda expression should be final or effectively final
Lambda表达式中使用的变量应为final或事实上final
```

而关于什么是事实上final:

```wiki
A variable or parameter whose value is never changed after it is initialized is effectively final.
变量或参数的值在初始化后从未更改，这就是事实上地final
```

修复测试用例为：

```java
@Test
void testLambdaScope() {
    int v2 = 2;
    int finalV = v2;
    UnaryOperator<Integer> addFunc = param -> param + v1 + finalV;
    v1++;
    v2++;
    assertEquals(4, addFunc.apply(1));
}
```

与`v2`变量不同，这里的`v1`变量上并没有出现类似错误提示，是因为`v1`是类的成员变量，lambda函数体里实际上是`this.v1`，指向的是类变量的地址，与类方法里获取类变量的值是一样的。如此来理解，`assertEquals`会失败，因为`v1`变量值在函数式接口调用之前自增了`1`，变为了`2`，那么期望值应该是`5`而不是`4`。

> 局部变量的内存是在堆栈上分配，因而在Lambda函数体中保存其地址不现实，Java对lambda函数体中引用的局部变量在函数定义时就进行值替换。为了避免在函数定义后局部变量又发生改变容易引起的误解，因而添加了对lambda函数体中使用的变量的final或事实上final的约束。

上面的测试用例中，如果我们的意图是希望在函数式接口调用时使用的是修改后的v2变量，可以借助一个值对象来达成:

```java
class ValueObject {
    int val;
    ValueObject(int val){
        this.val = val;
    }
}

@Test
void testLambdaScopeObject() {
    ValueObject v2 = new ValueObject(2);
    UnaryOperator<Integer> addFunc = param -> param + v2.val;
    v2.val++;
    assertEquals(4, addFunc.apply(1));
}
```

还有一种情形，如果想再`Lambda`函数体中修改外部变量，是否会被允许呢：

```java
@Test
void testLambdaIntSum() {
    int sum = 0;
    Consumer<Integer> sumFun = param -> sum += param;
}
```

答案是这种也是不被允许的，`sum`变量在lambda函数体中已经被替换为值，在lambda函数体内是无法修改到`sum`变量的值的。而且`Java`也不容许这么做，编译时会直接报错。

Lambda表达式的初学者经常容易或习惯写如下的代码：

```java
List<Integer> list = new ArrayList<>();
IntConsumer addFun = param -> list.add(param);
```

lambda表达式不会报错，与`ValueObject`的逻辑类似，`list`这个变量在lambda函数体内并没有被重新赋值。但是此种方式是不被推荐使用的，因为容易在方法内部出现让人忽视的并发问题：

```java
@Test
void testLambdaListSum() {
    List<Integer> list = new ArrayList<>();
    IntConsumer addFun = param -> list.add(param);

    IntStream.range(0, 1000)
            .parallel()
            .forEach(addFun);

    assertEquals(1000, list.size());
}
```

这个测试将无法通过，定义的`addFun`中使用了线程不安全的外部变量`list`，从而使得Lambda表达式也是线程不安全的。

#### Lambda异常处理

`Lambda`表达式让函数式接口的定义变得更加整洁，但是`Lambda`表达式中处理异常的方式并不整洁：

```java
Arrays.asList(1, 2, 3, 0, 4, 5, 6).forEach(i -> {
    try {
        System.out.println(50 / i);
    } catch (ArithmeticException e) {
        System.err.println("Arithmetic Exception: " + e.getMessage());
    }
});
```

尤其对于检查性异常（`Checked Exception`），`Lambda`表达式更难处理：

```java
private static void writeToFile(int i) throws IOException {
  throw new IOException("Mock IO error");
}

Arrays.asList(1, 2, 3, 0, 4, 5, 6).forEach(i -> LambdaTest.writeToFile(i)); //编译错误
```

上面的语句会出现变异错误，因为`forEach`方法的定义与Lambda表达式不匹配，`forEach`方法期望的是一个`Consumer`函数式接口，而此接口定义的抽象方法签名中并没有`throw Exception`。

```java
default void forEach(Consumer<? super T> action) {
    Objects.requireNonNull(action);
    for (T t : this) {
        action.accept(t);
    }
}
```

简单的说，即如下的赋值是不能成功的：

```java
Consumer<Integer> consumer = i -> {
		throw new IOException("abc");
};
```

最简单的解决方式是，在`Lambda`表达式中将检查性异常`Catch`，而后抛出非检查性异常（`Unchecked Exception`）：

```java
Arrays.asList(1, 2, 3, 0, 4, 5, 6).forEach(i -> {
    try {
        LambdaTest.writeToFile(i);
    } catch (IOException e) {
        throw new RuntimeException("IO Exception: " + e.getMessage());
    }
});
```

但如果`Lambda`表达式中到处都充斥中`try...catch`，代码将相当的难看。解决方式是定义一个高阶函数来解决这个问题：

```java
@FunctionalInterface
interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E;
}

private static<T, E extends Exception> Consumer<T> consumerExceptionWrapper(ThrowingConsumer<T, E> consumer, Class<E> exceptionClass) {
    return  i -> {
        try {
            consumer.accept(i);
        } catch (Exception ex) {
            try {
                E castedEx = exceptionClass.cast(ex);
                System.out.println("Exception occured: " + castedEx.getMessage());
                throw new RuntimeException(castedEx);
            } catch (ClassCastException castEx) {
                throw new RuntimeException(castEx);
            }
        }
    };
}

Arrays.asList(1, 2, 3, 0, 4, 5, 6).forEach(LambdaTest.consumerExceptionWrapper(i -> writeToFile(i), IOException.class));
```

这里定义的函数式接口`ThrowingConsumer`使得其可以接收抛出检查性异常的`Lambda`表达式，而`consumerExceptionWrapper`对检查性异常进行处理，并返回没有检查型异常的`Consumer`函数式接口。这样使得`Lambda`表达式处理异常时也会更简洁。

> 对于非检查型异常，也可以通过类似的方式来编写一个高阶的函数方法来处理，或是直接在lambda表达式之外使用try..catch来处理。