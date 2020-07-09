# 第4节：无处不在的函数式

上一节介绍的强大的`Stream`操作，本节将介绍Java语言基础库中那些正在潜移默化向函数式化、向声明式编程（`Declarative programming`）的变化。

#### Iterable接口

在Java中，`Iterable`接口是集合相关的一个很重要的接口，其提供了三个方法:

```java
Iterator<T> iterator();
default void forEach(Consumer<? super T> action);
default Spliterator<T> spliterator() {
  ...
}
```

其只有一个抽象方法`iterator()`，因而也可以被用做为函数式接口：

```java
@Test
void testFunctionalIterable() {
    Iterable<Integer> iterable = () -> List.of(1, 2, 3).iterator();
    iterable.forEach(System.out::println);
}
```

其提供的`default`方法`forEach`接收的是一个`Consumer`函数式接口参数，因而可以传递`Lambda`表达式或方法，测试用例中的`forEach`语句其实可看做等于：

```java
Consumer<Integer> anyIntegerConsumer = System.out::println;
iterable.forEach(anyIntegerConsumer);
```

Java语言中，`List`、`Set`、`Queue`等接口都最终继承自`Iterable`，因而可以在相应对象上直接调用`forEach`方法。

```java
@Test
void testListSetDequeue() {
    List<Integer> list = List.of(1, 2, 3);
    list.forEach(System.out::println);

    Set<Integer> set = Set.of(4, 5, 6);
    set.forEach(System.out::println);

    Queue<Integer> queue = new LinkedList<>(List.of(7, 8, 9));
    queue.forEach(System.out::println);
}
```

#### Iterator接口

`Iterable`接口是生产`Iterator`的接口，而`Iterator`接口提供了一种顺序访问一个容器对象中的各个方法的方式。`Iterable`接口中的方法:

```java
boolean hasNext();
E next();
default void remove() {
  ...
}
default void forEachRemaining(Consumer<? super E> action) {
  ...
}
```

接口中有两个抽象方法，通过这个两个方法来进行统一的顺序访问，传统方式如:

```java
@Test
void testTraditionalIteratorOverList() {
    Iterator<Integer> iterator = List.of(1, 2, 3).iterator();
    while(iterator.hasNext()) {
        System.out.println(iterator.next());
    }
}
```

而使用接口的`forEachRemaining`方法，会简洁许多：

```java
List.of(1, 2, 3).iterator().forEachRemaining(System.out::println);
```

自定义的`Iterator`实现闭区间的自然数列的生成：

```java
class RangeIterator implements Iterator {
    private int low;
    private int high;

    RangeIterator(int low, int high) {
        this.low = low;
        this.high = high;
    }

    @Override
    public boolean hasNext() {
        return low <= high;
    }

    @Override
    public Object next() {
        return low++;
    }
}

@Test
void testRangeIterator() {
    new RangeIterator(1, 10).forEachRemaining(System.out::println);
}
```

对于Java语言中的原生类型，提供了`PrimitiveIterator`接口，其继承自`Iterator`接口，但是能处理Java原生类型，避免对原生类型的自动拆装箱，这里自定义了一个对`int`数组的`Iterator`：

```java
class IntArrayIterator implements PrimitiveIterator.OfInt {
    private int []arr;
    private int index;

    IntArrayIterator(int ...a) {
        index = 0;
        arr = Arrays.copyOf(a, a.length);
    }

    @Override
    public int nextInt() {
        return arr[index++];
    }

    @Override
    public boolean hasNext() {
        return index < arr.length;
    }
}

@Test
void testIntArrayIterator() {
    new IntArrayIterator(1, 2, 3).forEachRemaining((int num) -> System.out.println(num));
}
```

#### Map接口

`Map`接口没有继承`Iterable`接口，但是其中也定义了`forEach`方法:

```java
default void forEach(BiConsumer<? super K, ? super V> action){
  .....
}
```

对`Map`的遍历也可以很简洁：

```java
@Test
void testMapForEach() {
    Map<String, String> map = Map.of("key1", "value1", "key2", "value2");
    map.forEach((k, v) -> System.out.println(k+"="+v));
}
```

`Map`中的遍历也可以是：

```java
@Test
void testMapKeyValueSetIteration() {
    Map<String, String> map = Map.of("key1", "value1", "key2", "value2");
  	//entrySet()方法将返回一个Set集合，集合的每个值为Map.Entry对象
    map.entrySet().forEach(System.out::println);
  	//keySet()方法将返回Map中Key值的集合
    map.keySet().forEach(System.out::println);
  	//values()方法将返回Map中Value值的Collection接口对象
    map.values().forEach(System.out::println);
}
```

`Map`中还提供了可以接收`BiConsumer`为参数的`replaceAll`方法：

```java
default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
  ...
}
```

对`replaceAll`方法的使用：

```java
@Test
void testMapReplaceAll() {
    Map<String, Integer> map = new HashMap<>(Map.of("Key1", 1, "Key2", 2));
  	
  	//将所有的value值替换为value的平方
    map.replaceAll((k,v) -> v * v);

    assertEquals(1, map.get("Key1"));
    assertEquals(4, map.get("Key2"));
}
```

`Map`的非常有用的`computeIfAbsent`方法：

```java
default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
	...
}
```

`computeIfAbsent`方法的使用例子，将评分为`A`的学生放在一个列表中：

```java
@Test
void testMapComputeIfAbsent() {
    Map<String, List<String>> scoreStudentsMap = Maps.newHashMap();
    scoreStudentsMap.computeIfAbsent("A", k -> new LinkedList<>()).add("Student1");
    scoreStudentsMap.computeIfAbsent("B", k -> new LinkedList<>()).add("Student2");
    scoreStudentsMap.computeIfAbsent("A", k -> new LinkedList<>()).add("Student3");
    assertEquals(List.of("Student1", "Student3"), scoreStudentsMap.get("A"));
}
```

`Map`中也有`computeIfPresent`方法：

```java
default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
  ...
}
```

`computeIfPresent`方法与`computeIfAbsent`方法一起使用的例子：

```java
@Test
void testCountCharacterNumber() {
    Map<Character, Integer> wordCountMap = Maps.newHashMap();
    "hello world"
            .chars()
            .mapToObj(e -> (char)e)
            .collect(Collectors.toList())
            .forEach(c -> {
              	//先处理当字符在Map中不存在的情况，不存在初始化为0
                wordCountMap.computeIfAbsent(c, k -> 0);
              	//处理字符串在Map中存在的情况，存在则更新value值为value+1
                wordCountMap.computeIfPresent(c, (k, v) -> v + 1);
            });
    assertEquals(3, wordCountMap.get('l'));
    assertEquals(2, wordCountMap.get('o'));
}
```

但对于上面的例子，可以直接使用`Map`接口的`compute`方法来替换`computeIfAbsent`和`computeIfPresent`：

```java
@Test
void testCountCharacterNumberWithCompute() {
    Map<Character, Integer> wordCountMap = Maps.newHashMap();
    "hello world"
            .chars()
            .mapToObj(e -> (char)e)
            .collect(Collectors.toList())
            .forEach(c -> {
              	//第二个参数为BiFunction函数式接口
                wordCountMap.compute(c, (k,oldValue) -> Objects.isNull(oldValue) ? 1 : oldValue + 1);
            });
    assertEquals(3, wordCountMap.get('l'));
    assertEquals(2, wordCountMap.get('o'));
}
```

也可以使用`Map`接口的`merge`方法：

```java
@Test
void testCountCharacterNumberWithMerge() {
    Map<Character, Integer> wordCountMap = Maps.newHashMap();
    "hello world"
            .chars()
            .mapToObj(e -> (char)e)
            .collect(Collectors.toList())
            .forEach(c -> {
              	//如果oldValue为空，merge函数将选用其第二个参数为初始值
              	//第三个参数中的函数中的value使用的值就是第二个参数传入的值
                wordCountMap.merge(c, 1, (oldValue, value) -> oldValue + 1);
            });
    assertEquals(3, wordCountMap.get('l'));
    assertEquals(2, wordCountMap.get('o'));
}
```

#### Comparator接口

`Comparator`接口是一个函数式接口，其唯一的抽象方法为:

```java
int compare(T o1, T o2);
```

此接口常见的是被`List`接口的`sort`方法使用，如果我们需要对列表中数字进行反向排序：

```java
@Test
void testFunctionalComparator() {
    Comparator<Integer> intDesc = (o1, o2) -> o2 - o1;
    List<Integer> list = Lists.newArrayList(1, 2, 3);
    list.sort(intDesc);
    assertEquals(Lists.newArrayList(3, 2, 1), list);
}
```

`Comparator`接口的强大在于其提供的大量的`default`方法和默认静态构建方法，先来看一些函数式相关的`default`方法`thenComparing`的第一种实现：

```java
default Comparator<T> thenComparing(Comparator<? super T> other){
	....
}
```

可以对实体`Person`的列表进行各种排序：

```java
class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

@Test
void testThenComparingToComparator() {
    Comparator<Person> comparingByName = (person1, person2) -> person1.getName().compareTo(person2.getName());
    Comparator<Person> comparingByAge = (person1, person2) -> person1.getAge() - person2.getAge();

    List<Person> people = Lists.newArrayList(
            new Person("Zhang si", 20),
            new Person("Zhang san", 25),
            new Person("Wang er", 25)
    );

  	//先按name排序(字符串默认排序），而后再age进行排序（年龄小的排前面)
    people.sort(comparingByName.thenComparing(comparingByAge));
    assertEquals("Wang er", people.get(0).getName());

  	//先按age排序，而后再按name排序
    people.sort(comparingByAge.thenComparing(comparingByName));
    assertEquals("Zhang si", people.get(0).getName());
	
  	//先按age排序（年龄大的排前面），而后再按年龄排序（默认排序的反向排序），reversed方法使得反向排序变得容易
    people.sort(comparingByAge.reversed().thenComparing(comparingByName.reversed()));
    assertEquals("Zhang san", people.get(0).getName());
}
```

上面的测试用例也可以用如下方式实现：

```java
@Test
void testThenComparingWithComparing() {
    List<Person> people = Lists.newArrayList(
            new Person("Zhang si", 20),
            new Person("Zhang san", 25),
            new Person("Wang er", 25)
    );
  
  	//Comparator接口提供了comparing静态方法来更方便易读的获取一个Comparator，这里Person::getName为一个Function接口
    Comparator<Person> comparingByName = Comparator.comparing(Person::getName, Comparator.naturalOrder());
  
  	//针对Java原始类型，提供了comparingInt，comparingLong，comparingDouble的静态方法
    Comparator<Person> comparingByAge = Comparator.comparingInt(Person::getAge);

  	//thenComparing的另外一种写法
    people.sort(comparingByName.thenComparing(Person::getAge, Comparator.naturalOrder()));
    assertEquals("Wang er", people.get(0).getName());

    people.sort(comparingByAge.thenComparing(Person::getName, Comparator.naturalOrder()));
    assertEquals("Zhang si", people.get(0).getName());

    people.sort(comparingByAge.reversed().thenComparing(Person::getName, Comparator.reverseOrder()));
    assertEquals("Zhang san", people.get(0).getName());
}
```

#### Optional类

在第一章第一节对`Optional`类已经做了比较详细的介绍，我们再复习下`Optional`类中定义的一些使用了函数式接口的方法，如`ifPresent`方法：

```java
public void ifPresent(Consumer<? super T> action){
  ...
}

//使用例子
Optional
  .ofNullable(1)
  .ifPresent(System.out::println);
```

`flatMap`方法：

```java
public <U> Optional<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper) {
	....
}

//使用例子
Optional
  .ofNullable(1)
  .flatMap(value -> Optional.ofNullable(value+1))
  .ifPresent(System.out::println);
```

`filter`方法：

```java
public Optional<T> filter(Predicate<? super T> predicate) {
	...
}

//使用例子
Lists.newArrayList(1, null, 2, null, 3, null, 4)
  .forEach(
      num -> Optional.ofNullable(num).filter(i -> i%2==0).ifPresent(System.out::println)
  );
```

#### 其他

自从Java8引入`Lambda`表达式和函数式接口后，JDK基础类库中大量的类和接口被更新，一些新的方法被创建来支持函数式的写法。此章节无法一一介绍所有的类和接口，建议读者在读本节的同时也可以阅读下相应接口实现的源码来加深了对函数式接口使用的理解。也可以尝试用函数式的思想去定义一些共用的方法：定义参数为函数式接口的方法。

#### 











