# 第2节：苦练快捷键

这一节将使用Intellij IDEA开发一些工具类：

- `IntegerUtils`类，这个类具有`isOdd`, `isEven`, `isPrime`, `isPalindrome`，`nextPrime`等方法来判断某个整数是奇数，偶数，素数还是回文数字，以及一些计算函数如下一个素数等。

本节使用的是上一节创建的`idea-java`项目，在练习本节内容前，尽量使用快捷键，不得已时才使用鼠标。

本节的快捷键是Intellij IDEA提供的默认`Keymap`，文中将给出`macOS`和`Windows`默认两套快捷键，默认给出两个操作系统的快捷键如`⌘\`|`Ctrl+/`，`|`前部分是MacOS操作系统中的快捷键，后部分为Windows操作系统中的快捷键，Windows快捷键中的`+`仅仅是分隔按键符号的符号。

关于MacOS操作系统的快捷键的一些简写：

- ⌃，表示`control`键
- ⇧，表示`shift`键
- ⌘，表示`command`键
- ⌥，表示`option`键
- ↩，表示`Enter`回车键
- ↑↓←→，表示键盘中的Up键
- ⇥，表示`Tab`键
- ⌫，表示删除键

回顾上一节创建的项目：

![avatar](images/idea-7.png)

#### 实现isPrime方法

##### 创建新文件IntegerUtils

`⌘1`|`Ctrl+1`打开项目结构，且光标会移动到属性结构中：

![avatar](images/idea-shortcut-1.png)

`↑`|`Up`选中idea.java项，`⌘N`|`Alt+Insert`将弹出创建菜单:

![avatar](images/idea-shortcut-2.png)

`↓`|`Down`选中`Package`，按`↩`|`Enter`，在`New Package`弹出框中填写`idea.java.utils`，填写完后`↩`|`Enter`，将在idea.java下创建一个utils的目录。

在`utils`目录上`⌘N`|`Alt+Insert`，默认选择`Java Class`后，按`↩`|`Enter`，在`New Java Class`弹出框中填写`IntegerUtils`，填写完后`↩`|`Enter`，将在utils下创建`IntegerUtils.java`:

![avatar](images/idea-shortcut-3.png)

##### 包结构调整

按第二章中的介绍，在写具体实现代码之前应先写验收测试。不过，在这里，创建单元类前，我们可以先考虑调整一下一下我们的项目结构:

- `src/main/java`目录下放置实现类
- `src/main/test`目录下放置测试类
- 包名从`idea.java`修改为`com.learn.idea`

`⌘1`|`Ctrl+1`将光标移动到项目结构中，将光标移动到`src`上，`⌘N`|`Alt+Insert`选择`Package`，输入`main.test`后`↩`|`Enter`。

，将光标移动到`idea.java`上，`⇧F6`|`Shift+F6`，在`Rename`弹出框中将报名`idea.java`修改为`main.java.com.learn.idea`，按`↩`|`Enter`：

![avatar](images/idea-shortcut-4.png)

将光标移动到`java.idea`，`delete`|`Del`，然后`⇥`|`Tab`，再`↩`|`Enter`删除此目录。`⌘;`|`Ctrl+;`打开项目配置页面：

![avatar](images/idea-shortcut-5.png)

这时需要使用鼠标了:

- 将`src`从`Source Folders`中移除。
- 选中`src/main/java`后，点击`Sources`，将`src/main/java`添加到`Source Folders`中。
- 选中`src/main/test`后，点击`Tests`，将`src/main/test`添加到`Test Source Folders`中。

![avatar](images/idea-shortcut-6.png)

点击`Apply`按钮后完成对当前项目的设置。

![avatar](images/idea-shortcut-7.png)

可以看到`IntegerUtils`中有错误提示，按`ESC`将光标移动到`IntegerUtils`类中，按`F2`光标将移动错误提示处。`⌥空格键`|`Ctrl+空格键`后选择提示菜单中的`Set package name to 'com.learn.idea.utils'`，而后`↩`|`Enter`节解决了此错误。

`⌘E`|`Ctrl+E`显示最近打开的文件记录列表，选择`Main`，如同解决`IntegerUtils`中的错误提示一样，`F2` -> `⌥空格键`|`Ctrl+空格键` -> `↓`|`Down` -> `↩`|`Enter`，四个快捷键操作解决`Main`类中的错误。而后`⌃⌥R`|`Alt+Shift+F10`运行`main`方法。

##### 创建单元测试类

`⌘E`|`Ctrl+E`选择`IntegerUtils`，回到`InteferUtils`类中。

`⌘⇧T`|`Ctrl+Shift+T`，弹出的菜单中只有一项为`Create New Test...`，按`↩`|`Enter`，将弹出提示`Create test in the same source root?`，按`⇥`|`Tab`让Ok按钮被选中，然后`↩`|`Enter`。

![avatar](images/idea-shortcut-8.png)

所有的选项都使用IDEA默认填充的值，`↩`|`Enter`。`IntegerUtilsTest`类将被创建，`F2` -> `⌥空格键`|`Ctrl+空格键` ->  `↓`|`Down` 选择`Add 'Junit5.4' to classpath` -> `↩`|`Enter`。`Junit5.4`将被自动下载并放入项目的`classpath`中。

![avatar](images/idea-shortcut-9.png)

##### 编写isPrime方法单元测试

`⌘N`|`Alt+Insert`后选择`Test Method`创建测试用例，默认的测试用例方法名为`test`，创建完测试用例后即可以修改方法名为`testIsPrime`，而后按`↩`|`Enter`。

![avatar](images/idea-shortcut-10.png)

下一步是将`@Test`修改为`@ParameterizedTest`和`@ValueSource`的组合。

使用`↑↓`|`Up或Down`将光标移动到`@Test`所在位置，`⌘⌫`|`Ctrl+Y`删除当前光标所在行 -> `↑`|`Up` -> `⇧↩`|`Shift+Enter`创建新的空行 -> 输入`@P`会弹出很多`@P`开头的选择项 -> `↩`|`Enter`将自动选择第一个提示项。

`⇧↩`|`Shift+Enter` -> 输入`@V` ->  `↩`|`Enter`自动选择`@ValueSource` -> 输入"()" -> `⌃空格`|`Ctrl+空格` :

<img src="images/idea-shortcut-11.png" alt="avatar" style="zoom:50%;" />

选择`ints`，输入`{5, 7, 31, 101, 1511}`，`⌘→`|`Ctrl+→`到行尾 -> `⌘⇧↩`|`Ctrl+Enter`自动结束本行代码，跳转到测试方法内 -> 输入`atru`:

<img src="images/idea-shortcut-12.png" alt="avatar" style="zoom:50%;" />

`↩`|`Enter`将寻找`assertTrue` -> 输入`IntegerUtils.isPrime(num)` ->  `⌥↩`|`Alt+Enter`提示信息中选择`Create paramater 'num'` ：

<img src="images/idea-shortcut-13.png" alt="avatar"/>

按`⇥`|`Tab`键在各个域和按钮之间切换，修改Type为`Integer`后`↩`|`Enter` -> `⇥`|`Tab`键切换到`Refactor`按钮 -> `↩`|`Enter` -> `⌥↩`|`Alt+Enter`提示信息中选择`Create method ‘isPrime' in 'IntegerUtils'` -> `↩`|`Enter` 将创建`isPrime`方法并跳转到`IntegerUtils`类中 -> 输入`boolean`替换`Object` -> 多按几次`↩`|`Enter` 直到方法被创建完成。

`⌘⇧T`|`Ctrl+Shift+T`跳转到测试类 ->`↑↓`|`Up或Down`移动光标到`testIsPrime`方法上 ->  `⌃⌥R`|`Alt+Shift+F10`选择`IntegerUtilsTest.testIsPrime`执行测试方法，因为事先类默认直接返回`false`，所以测试用例会执行失败。

`⌘⇧T`|`Ctrl+Shift+T`从测试类跳转会实现类 -> 输入`1 == num.if`后列出提示列表 -> `↩`|`Enter`自动选择第一个 -> 输入`return true;`，将创建如下代码:

```java
if (1 == num) {
  return true;
}
```

`⌘⇧↩`|`Ctrl+Enter`将跳出if语句块，新建一空行 -> 输入`fori` -> `↩`|`Enter`将自动创建如下代码:

```java
for (int i = 0; i < ; i++) {
            
}
```

按 `↩`|`Enter`在for循环块的相应位置填入期望的代码：

```java
for (int i = 2; i < num; i++) {
    if (0 == num % i) {
        return false;
    }
}
```

`⌘⌥]`|`Ctrl+]`移动光标到块尾，譬如如果光标在上面的`if`块中，按两次`⌘⌥]`|`Ctrl+]`将光标移动到`for`循环块尾 -> `⇧↩`|`Shift+Enter`在`for`循环块尾添加一空行 -> 输入`return true;` -> `⌃R`|`Shift+F10`重新运行测试，测试通过。

小重构：将光标移动到`i`，`⇧F6`|`Shift+F6`，修改变量名为`mod`，`↩`|`Enter`后所有的变量`i`都将被修改为`mod`。

```java
public class IntegerUtils {
    public static boolean isPrime(Integer num) {
        if (1 == num) {
            return true;
        }
        for (int mod = 2; mod < num; mod++) {
            if (0 == num % mod) {
                return false;
            }
        }
        return true;
    }
}
```

##### 调试初试

将光标移动到`isPrime`方法下的第一行，`⌘F8`|`Ctrl+F8`添加断点。`⌃D`|`Shift+F9`以Debug的方式运行测试方法：

<img src="images/idea-shortcut-debug-1.png" alt="avatar"/>

`F8`进入下一步，`⌥F8`|`Alt+F8`在弹出框中可以输入表达式，查看表达式的值：

<img src="images/idea-shortcut-debug-2.png" alt="avatar" style="zoom:50%;"/>

`⌘⌥R`|`F9`将完成第一个测试用例（num=5）并继续执行下一个测试用例（num=7）。

##### 用到的快捷键总结

- `⌘1`|`Ctrl+1`打开项目结构窗口或将光标移动到项目结构窗口
- `⌘N`|`Alt+Insert`在项目结构窗口中可用来创建包或文件；在测试用例类中用来生成测试方法；在实现类中用来生成代码
- `⇧F6`|`Shift+F6`，可以用来重命名包，类，接口，方法，变量
- `⌘;`|`Ctrl+;`打开当前项目配置
- `F2`在当前代码的错误之间跳转
- `⌥空格键`|`Ctrl+空格键`，智能提示错误的修复建议
- `⌘E`|`Ctrl+E`显示最近打开的文件记录列表
- `⌃⌥R`|`Alt+Shift+F10`运行`main`方法，测试方法，测试类，测试包
- `⌘⇧T`|`Ctrl+Shift+T`在测试类和实现类之间跳转，如果没有测试类，可以通过此命令创建测试类
- `⌘⌫`|`Ctrl+Y`删除光标所在行
- `⇧↩`|`Shift+Enter`在当前光标行后创建新的空行，并将光标移动到新的空行
- `⌘⇧↩`|`Ctrl+Enter`可自动结束本行代码，如自动在行尾添加`;`；如果本行已经结束但不是块结束，将在此行下面创建新行；如果本行也是块结束，将在块下面创建新行且光标移动到行
- `⌘⌥]`|`Ctrl+]`将光标移动到块尾
- `⌘F8`|`Ctrl+F8`在当前光标所在代码行添加断点
- `⌃D`|`Shift+F9`以Debug的方式运行测试方法
- `⌘⌥R`|`F9`让测试继续恢复程序运行，如果该断点下面代码还有断点则停在下一个断点上

