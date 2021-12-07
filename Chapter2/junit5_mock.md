# 第3节：Mock

> 先思考你的程序是否可以改进！不要轻易使用Mock！

#### Mockito的使用

首先在`build.gradle`中添加`Mockito`的依赖，如：

```groovy
dependencies { 
	testImplementation "org.mockito:mockito-core:3.+" 
}
```

当某个类中的方法有副作用操作（如读写数据库、需要发送网络请求等），而调用此有副作用的方法的单元测试并不关注数据库的存储或如何发送网络请求，单元测试中往往引入`Mock`来让测试更加容易。

譬如存在如下一个`BaiduTransport`的类，其`query`方法就是从百度中查询某个字段:

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BaiduTransport {
    public String query(String query) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://www.baidu.com/s?wd="+query)).build();
        HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();
        try {
            return client.send(request, responseBodyHandler).body();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
```





#### 函数型参数解决Mock问题

某一个类依赖一些重量级的类、或需要网络调用、或需要调用第三方的重量级静态方法。这里定义一个`BigObject`来模拟重量级的类，这里`BigObject`没有赋予确定某个业务含义，其代表一类巨型对象：需要处理复杂输入参数，需要多个步骤的构造，提供一些接口以供第三方调用:

```java
public class BigObject {

    private String someField1;

    private String someField2;

    private String someField3;

    private BigObject() {
        super();
    }

    public String calculate(String prefix, String suffix) {
        StringJoiner joiner = new StringJoiner(", ", prefix, suffix);
        joiner.add(someField1);
        joiner.add(someField2);
        joiner.add(someField3);
        return joiner.toString();
    }

    public static class Builder {

        private static void buildStep1(BigObject obj, Map<String, String> map) {
            obj.someField1 = map.get("key1");
        }

        private static void buildStep2(BigObject obj, Map<String, String> map) {
            obj.someField2 = map.get("key2");
        }

        private static void buildStep3(BigObject obj, Map<String, String> map) {
            obj.someField3 = map.get("key3");
        }

        public static BigObject create(Map<String, String> map) {
            BigObject obj = new BigObject();
            buildStep1(obj, map);
            buildStep2(obj, map);
            buildStep3(obj, map);
            return obj;
        }
    }
}
```

笔者已经尽量简化了这个`BigObject`，但是其代码量还是不少。有一个专门的`Builder`类来构造这个`BigObject`，`BigObject`有一个公共的`calculate`方法，针对`calculate`的单元测试：

```java
@Test
void testBuildBigObjectAndCalculate() {
  HashMap<String, String> map = new HashMap<>();
  map.put("key1", "value1");
  map.put("key2", "value2");
  map.put("key3", "value3");
  BigObject obj = BigObject.Builder.create(map);
  assertEquals("value1, value2, value3", obj.calculate("", ""));
}
```

`BigObject`的构造测试我们已经有所了解，这里一个`BigObjectUser`需要使用`BigObject`：

```java
public class BigObjectUser {

    String calPrefix(String rawPrefix) {
        return rawPrefix.trim() + " ";
    }

    String calSuffix(String rawSuffix) {
        return " " + rawSuffix.trim();
    }

    public String process(BigObject object, String rawPrefix, String rawSuffix) {
        return object.calculate(
                calPrefix(rawPrefix),
                calSuffix(rawSuffix)
        );
    }
}
```

`BigObject`的`process`方法中需要调用`BigObject`的`calculate`方法，我们该如何写这个测试呢，最直接的方式是：

```java
class BigObjectUserTest {

    private BigObjectUser bigObjectUser;

    @BeforeEach
    void setUp() {
        bigObjectUser = new BigObjectUser();
    }

    @Test
    void testProcess() {
        HashMap<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        BigObject obj = BigObject.Builder.create(map);

        assertEquals("prefix value1, value2, value3 suffix",
                bigObjectUser.process(obj, "prefix", "suffix"));
    }
}
```

> 测试代码也是一等公民，我们也需要尽量考虑降低测试代码的复杂度。

这里`BigObjectUserTest`中需要了解`BigObject`的构造细节，写测试代码的开销编程了如何构造这个`BigObject`。我们需要想办法解决这个问题！

一些程序员或许在想，`BigObject`的构造以及`calculate`方法在`BigObjectTest`中已经测试过了，在`BigObjectUser`中我们没有必要再去测试`BigObject`和`calculate`方法了。是否有一种方式可以模拟这个`BigObject`以及`calculate`方法？答案是肯定的，`Mockito`，`EasyMock`和`JMockit`等都提供了模拟对象和对象方法的功能，且都很强大易用。

笔者在这里希望把这些程序员的思绪拉回来，我们何不先想想，我们的程序是否有改进的空间？对于`BigObjectUser`的`process`方法来说，其本质上只依赖于`BigObject`的`calculate`方法，而对`BigObject`对象是没有依赖的。而依照我们对第一章第一节的学习，应该很容易想到`BigObject`的`calculate`方法可以看做`BiFunction<String, String, String>`，而`BiFunction<String, String, String>`更简单一点的表示是`BinaryOperator<String> calculator`。我们可以重构`process`方法了：

```java
public String process(BinaryOperator<String> calculator, String rawPrefix, String rawSuffix) {
  return calculator.apply(
    calPrefix(rawPrefix),
    calSuffix(rawSuffix)
  );
}
```

非常好! `process`方法已经不依赖`BigObject`了，可是测试没有通过，显然编译错误了，我们简单的修复了这个编译错误，这时，测试代码变成了:

```java
@Test
void testProcess() {
  HashMap<String, String> map = new HashMap<>();
  map.put("key1", "value1");
  map.put("key2", "value2");
  map.put("key3", "value3");
  BigObject obj = BigObject.Builder.create(map);

  assertEquals("prefix value1, value2, value3 suffix",
               bigObjectUser.process(obj::calculate, "prefix", "suffix"));
}
```

这里改动很小，只是把`bigObjectUser.process(obj, "prefix", "suffix")`改成了`bigObjectUser.process(obj::calculate, "prefix", "suffix")`，把对对象的依赖修改成了对对象方法的依赖。测试通过了，可是我们还不能结束，因为测试代码还是需要构造`BigObject`对象。运用我们再第一章第一节学到的内容，我们是否可以用一个简单的Lambda表达式来替换`obj::calculate`呢，答案是当然可以：

```java
@Test
void testProcess() {
  assertEquals("final calculated string",
               bigObjectUser.process((a,b)->"final calculated string", "prefix", "suffix"));
}
```

`process`方法现在依赖度的是一个函数式接口，而函数式接口我们几乎不需要构造成本。但是我们还有需要改进的地方么？当然， 如果上面的重构开发人员不注意（粗心）把`calPrefix(rawPrefix)`和`calSuffix(rawSuffix)`写反了位置，我们的测试检查不出来这个问题。解决办法是：

```java
BinaryOperator<String> calculator = (prefix, suffix) -> {
  return prefix + " calculated " + suffix;
};
assertEquals("prefix calculated suffix", bigObjectUser.process(calculator, "prefix", "suffix"));

```

> 也许这还不是最好的解决方式，或许读者们发挥自己的聪明才智，会有更好的解决方案。

这一节基本到这就要画一个句号了，一些读者也许有疑惑，为什么不介绍一些一些`Mockito`，`EasyMock`和`JMockit`，笔者还是那句话：`不到万不得已不要用Mock`，再加上一句是：如果你需要了请自行搜索各种Mock库的使用。

> 需要特别提到的是，Spring boot test框架默认使用Mockito，其为Spring boot开发时编写单元测试提供了极大的遍历。



