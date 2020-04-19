# 第4节：Java11特性概要

Java11于2018年9月25日发布，是一个长期维护的版本，其引入的新特性也并不多，本节将介绍其引入的部分新特性：

- HttpClient

- 字符串操作增强
- IO操作增强
- 命令`java`增强
- Z垃圾回收器

## HttpClient

Java11之前使用`HttpUrlConnection`来发送HTTP请求，不过比较难用。Java9在孵化器项目中定义了全新的`HttpClient`，目的是创建好用的既支持`HTTP/1.1`也支持`HTTP/2`的`HttpClient`。Java11将`HttpClient`从孵化器包里挪到了`java.net.http`包中，这个模块的信息：

```shell
⇒  java -d java.net.http
java.net.http@11.0.2
exports java.net.http
requires java.base mandated
contains jdk.internal.net.http
contains jdk.internal.net.http.common
contains jdk.internal.net.http.frame
contains jdk.internal.net.http.hpack
contains jdk.internal.net.http.websocket
```

使用`HttpClient`发送`GET`请求：

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
  			.version(HttpClient.Version.HTTP_1_1)
        .uri(URI.create("http://openjdk.java.net/"))
        .build();
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(response.body());
```

使用`HttpClient`发送`POST`请求：

```java
String body = "{\"name\":\"hello\"}";
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .header("Content-Type", "application/json")
        .uri(URI.create("http://httpbin.org/post"))
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
CompletableFuture<String> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(HttpResponse::body);

System.out.println("Before request complete!");
System.out.println(future.get());
```

这里总结一下这里面的知识点：

- `HttpClient`用来发送请求和接收响应，其`send`方法发送同步请求，`sendAsync`发送异步请求。实例化`HttpClient`的方式是通过其提供的`Builder`，`newHttpClient`方法会使用`HttpClientBuilderImpl`来构建`HttpClient`的实例。
- `HttpRequest`封装了请求的细节，如请求的URL、请求方式(`GET`、`POST`等)、请求头等。实例化`HttpRequest`也是通过其提供的`Builder`来创建，`newBuilder`方法会使用`HttpRequestBuilderImpl`来构建`HttpRequest`实例。
- `HttpRequest.BodyPublishers`提供了很多生成`HttpReqeust.BodyPublisher`的工厂方法，用来处理请求的`body`，可以从字符串、文件或字节流中生成请求`body`。`BodyPublisher`是响应式流中`Flow.Publisher`的子接口。
- `HttpResponse.BodyHandlers`用提供了很多生成`HttpResponse.BodySubscriber`的工厂方法，`BodySubscriber`可以将返回的`response`转换成各种格式，字符串、文件等。`BodySubscriber`是响应式流总`Flow.Subscriber`的子接口。
- 构建`HttpRequest`时可以使用`version`方法指定`HTTP`协议版本，如果没有指定，其默认使用`HTTP/2`：第一个请求使用`HTTP/2`发送请求到服务端，如果服务端不支持则后续使用`HTTP/1.1`。
- 如果没有特殊指定，默认是发送`GET`请求。

再来看一个使用`Basic Authorization`的例子：

```java
HttpClient client = HttpClient.newBuilder()
        .authenticator(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("admin", "password".toCharArray());
            }
        })
        .build();

HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://httpbin.org/basic-auth/admin/password"))
        .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

System.out.println(response.statusCode());
System.out.println(response.body());
```

`HttpClient`还支持`WebSocket`协议：

```java
ExecutorService executor = Executors.newFixedThreadPool(10);
HttpClient httpClient = HttpClient.newBuilder().executor(executor).build();
WebSocket.Builder webSocketBuilder = httpClient.newWebSocketBuilder();
WebSocket webSocket = webSocketBuilder.buildAsync(URI.create("wss://echo.websocket.org"), new WebSocket.Listener() {
    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("已连接");
        webSocket.sendText("发送消息-1", true);
        WebSocket.Listener.super.onOpen(webSocket);
    }
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        System.out.println("接收到: " + data);
        if(!webSocket.isOutputClosed()) {
            webSocket.sendText("发送消息-2", true);
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }
    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("关闭连接，状态码: " + statusCode + ", 原因: " + reason);
        executor.shutdown();
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }
}).join();
Thread.sleep(1000);
webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "ok").thenRun(() -> System.out.println("关闭发送"));
```

## 字符串操作增强

Java11中提供了一些非常有用字符串操作有关方法：

- 字符串`repeat`方法，可以很方便的生成长字符串，如`"a".repeat(1000)`

- 字符串`isBlank`方法，来判断字符串是否为空或者只包含空格/制表符

- 字符串`strip`方法是`trim`方法的增强，譬如`strip`方法能够处理全角空格，而`trim`不可以

- 字符串`lines`方法返回`Stream<String>`，如

  ```java
  "hello\nworld\n".lines().toArray(String[]::new).length
  ```

## IO操作增强

Java11中在`Files`类中添加了读写文件的函数，使得`Java`对文件的操作更加方便:

- `writeString`方法直接将字符串写入文件：

  ```java
  Files.writeString(Path.of("test1.txt"), "hello file 1!"); //默认UTF-8
  Files.writeString(Path.of("test2.txt"), "hello file 2!", Charsets.UTF_8);
  ```

- `readString`方法将读取文件到字符串中：

  ```java
  String s = Files.readString(Path.of("test1.txt"));
  String s2 = Files.readString(Path.of("test2.txt"), Charsets.UTF_8);
  System.out.println(s);
  System.out.println(s2);
  ```

## 命令java增强

`java`命令也可以直接导入单个的Java文件执行了，不需要再先运行`javac`命令去将源代码编译成字节码。

如定义的`HelloWorld.java`：

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
```

直接运行`java HelloWorld.java`就可以得到结果。不过注意此种方法有很大的局限性，如果`HelloWorld`类依赖于非JDK提供的类，会提示错误。解决方式是将依赖的类加到`Classpath`中：

```java
java -cp SOME_CLASSPATH HelloWorld.java
```

> 与Jshell加载运行文件不同的是，java命令运行定义的Java类中的`main`方法，而Jshell是顺序执行文件中定义的Java语句，文件也不是Java类。

## Z垃圾回收器

Java11中引入了Z垃圾回收器，不过其为实验版，其旨在实现以下几个目标：

- 停顿时间不超过10ms
- 停顿时间与堆大小无关，与存活对象的数量大小无关
- 可以处理小到几百兆，大到4T的内存大小

ZGC中没有新生代和老年代的概念，只有一块一块的内存区域页，以页为单位进行对象的分配和回收。每次进行GC时，都会对页进行压缩操作。

> 注意这个实验版本只支持`Linux64`位系统，在`Windows`和`MacOS`系统中都暂不支持。