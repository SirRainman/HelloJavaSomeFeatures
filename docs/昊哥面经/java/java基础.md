#  java三大特性

**1、封装**

**隐藏数据**

类就是封装数据和操作这些数据代码的逻辑实体。在一个类的内部，某些属性和方法是私有的，不能被外界所访问。通过这种方式，对象对内部数据进行了不同级别的访问控制，就避免了程序中的无关部分的意外改变或错误改变了对象的私有部分。

**2、继承**

**代码复用**
同时在使用继承时需要记住三句话：
1、子类拥有父类非private的属性和方法。
2、子类可以拥有自己属性和方法，即子类可以对父类进行扩展。
3、子类可以用自己的方式实现父类的方法。
通过这个示例可以看出，构建过程是从父类“向外”扩散的，也就是从父类开始向子类一级一级地完成构建。而且我们并没有显示的引用父类的构造器，这就是java的聪明之处：编译器会默认给子类调用父类的构造器。

对于继承而已，子类会默认调用父类的构造器，但是如果没有默认的父类构造器，子类必须要显示的指定父类的构造器，而且必须是在子类构造器中做的第一件事(第一行代码)。

## **多态**

多态分为编译时多态和运行时多态。

**编译时多态**的体现形式是重载，原理是静态分派。

**1. 重载（Overload）**

存在于同一个类中，指一个方法与已经存在的方法名称上相同，但是参数类型、个数、顺序至少有一个不同。

应该注意的是，返回值不同，其它都相同不算是重载。

编译阶段，**编译器根据参数的数量和静态类型确定重载版本。**但在很多情况下这个重载版本并不是“唯一的”，往往只能确定一个“更加合适的”版本。

**2.重写**

**运行时多态**存在的三个必要条件：

一、要有继承（包括接口的实现）；
二、要有重写；
三、父类引用指向子类对象。（父类是静态类型，子类是实际类型，静态类型编译时可知，实际类型运行时可知）

它的原理就是动态分派。

**原理：**类在方法区中建立一个虚方法表或者接口方法表，虚方法表中存放着各个方法的实际入口地址。子类方法表中，没有被重写的方法指向父类的实现入口。重写的方法地址替换为子类实现版本的入口地址。接口方法表同理。调用方法的invokevirtual指令的多态查找，第一步会找到实际类型就是子类，然后在虚方法表中找到对应的方法。invokeinterface同理

```
Parent p = new Child();
```

写或者分析代码时记住如下口诀：

- 成员变量：编译看左，运行看左（因为无法重写）；
- 成员方法：编译看左，运行看右（因为普通成员方法可以重写，变量不可以）；
- 静态方法：编译看左，运行看左（因为属于类）；

意思是当父类变量引用子类对象时，在这个引用变量 base 指向的对象中他的成员变量和静态方法与父类是一致的，他的非静态方法在编译时是与父类一致的，运行时却与子类一致（发生了复写）。



https://blog.csdn.net/clqyhy/article/details/78978785

https://www.cnblogs.com/wade-luffy/p/6058075.html

# 一、数据类型

## 基本类型

- byte/8
- char/16   **char用来Unicode编码的字符，unicode 编码字符集中包含了汉字，所以可以存储中文**
- short/16
- int/32
- float/32
- long/64
- double/64
- boolean/~

boolean 只有两个值：true、false，可以使用 1 bit 来存储，但是具体大小没有明确规定。JVM 会在编译时期将 boolean 类型的数据转换为 int，使用 1 来表示 true，0 表示 false。JVM 支持 boolean 数组，但是是通过读写 byte 数组来实现的。

**引用数据类型**
类（class），String，接口（interface），数组（array）

传参：基本类型传值，引用类型传地址

**包装类型**

基本类型都有对应的包装类型，基本类型与其对应的包装类型之间的赋值使用自动装箱与拆箱完成。

```
Integer x = 2;     // 装箱 调用了 Integer.valueOf(2)
int y = x;         // 拆箱 调用了 X.intValue()
```

**缓存池**

new Integer(123) 与 Integer.valueOf(123) 的区别在于：

- new Integer(123) 每次都会新建一个对象；
- Integer.valueOf(123) 会使用缓存池中的对象，多次调用会取得同一个对象的引用。

```
Integer x = new Integer(123);
Integer y = new Integer(123);
System.out.println(x == y);    // false
Integer z = Integer.valueOf(123);
Integer k = Integer.valueOf(123);
System.out.println(z == k);   // true
```

valueOf() 方法的实现比较简单，就是先判断值是否在缓存池中，如果在的话就直接返回缓存池的内容。

```
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}
```

在 Java 8 中，Integer 缓存池的大小默认为 -128~127。

```
static final int low = -128;
static final int high;
static final Integer cache[];

static {
    // high value may be configured by property
    int h = 127;
    String integerCacheHighPropValue =
        sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
    if (integerCacheHighPropValue != null) {
        try {
            int i = parseInt(integerCacheHighPropValue);
            i = Math.max(i, 127);
            // Maximum array size is Integer.MAX_VALUE
            h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
        } catch( NumberFormatException nfe) {
            // If the property cannot be parsed into an int, ignore it.
        }
    }
    high = h;

    cache = new Integer[(high - low) + 1];
    int j = low;
    for(int k = 0; k < cache.length; k++)
        cache[k] = new Integer(j++);

    // range [-128, 127] must be interned (JLS7 5.1.7)
    assert IntegerCache.high >= 127;
}
```

编译器会在自动装箱过程调用 valueOf() 方法，因此多个值相同且值在缓存池范围内的 Integer 实例使用自动装箱来创建，那么就会引用相同的对象。

```
Integer m = 123;
Integer n = 123;
System.out.println(m == n); // true
```

基本类型对应的缓冲池如下：

- boolean values true and false
- all byte values
- short values between -128 and 127
- int values between -128 and 127
- char in the range \u0000 to \u007F

在使用这些基本类型对应的包装类型时，如果该数值范围在缓冲池范围内，就可以直接使用缓冲池中的对象。

在 jdk 1.8 所有的数值类缓冲池中，Integer 的缓冲池 IntegerCache 很特殊，这个缓冲池的下界是 - 128，上界默认是 127，但是这个上界是可调的，在启动 jvm 的时候，通过 -XX:AutoBoxCacheMax=<size> 来指定这个缓冲池的大小，该选项在 JVM 初始化的时候会设定一个名为 java.lang.IntegerCache.high 系统属性，然后 IntegerCache 初始化的时候就会读取该系统属性来决定上界。

[StackOverflow : Differences between new Integer(123), Integer.valueOf(123) and just 123](https://stackoverflow.com/questions/9030817/differences-between-new-integer123-integer-valueof123-and-just-123)

# 二、String（面试题）

## 概览

String 被声明为 final，因此它不可被继承。(Integer 等包装类也不能被继承）

在 Java 8 中，String 内部使用 char 数组存储数据。

```
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final char value[];
}
```

在 Java 9 之后，String 类的实现改用 byte 数组存储字符串，同时使用 `coder` 来标识使用了哪种编码。

```
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final byte[] value;

    /** The identifier of the encoding used to encode the bytes in {@code value}. */
    private final byte coder;
}
```

内部数组被声明为 final，这意味着 value 数组初始化之后就不能再引用其它数组。并且 String 内部没有改变 value 数组的方法，因此可以保证 String 不可变。

## 不可变的好处

**1. 可以缓存 hash 值**

因为 String 的 hash 值经常被使用，例如 String 用做 HashMap 的 key。不可变的特性可以使得 hash 值也不可变，因此只需要进行一次计算。

**2.字符串常量池的需要**

如果一个 String 对象已经被创建过了，那么就会从 字符串常量池中取得引用。只有 String 是不可变的，才可能使用 String Pool。

**3. 安全性**

String 经常作为参数，String 不可变性可以保证参数不可变。例如在作为网络连接参数的情况下如果 String 是可变的，那么在网络连接过程中，String 被改变，改变 String 的那一方以为现在连接的是其它主机，而实际情况却不一定是。

**4. 线程安全**



[Program Creek : Why String is immutable in Java?](https://www.programcreek.com/2013/04/why-string-is-immutable-in-java/)

## String, StringBuffer and StringBuilder

**1. 可变性**

- String 不可变
- StringBuffer 和 StringBuilder 可变

**2. 线程安全**

- String 不可变，因此是线程安全的
- StringBuilder 不是线程安全的
- StringBuffer 是线程安全的，内部使用 synchronized 进行同步

[StackOverflow : String, StringBuffer, and StringBuilder](https://stackoverflow.com/questions/2971315/string-stringbuffer-and-stringbuilder)

## 字符串常量池

字符串常量池（String Pool）是方法区的一部分，在 JDK 1.8 之后，使用元空间实现，位于本地内存中。原来永久代的数据被分到了堆和元空间中。元空间存储类的元信息，静态变量和常量池等放入堆中。

保存着所有字符串字面量（literal strings），这些字面量在编译时期就确定。不仅如此，还可以使用 String 的 intern() 方法在运行过程将字符串添加到 String Pool 中。

当一个字符串调用 intern() 方法时，如果 String Pool 中已经存在一个字符串和该字符串值相等（使用 equals() 方法进行确定），那么就会返回 String Pool 中字符串的引用；否则，就会在 String Pool 中添加一个新的字符串，并返回这个新字符串的引用。

下面示例中，s1 和 s2 采用 new String() 的方式新建了两个不同字符串，而 s3 和 s4 是通过 s1.intern() 方法取得同一个字符串引用。intern() 首先把 s1 引用的字符串放到 String Pool 中，然后返回这个字符串引用。因此 s3 和 s4 引用的是同一个字符串。

```
String s1 = new String("aaa");
String s2 = new String("aaa");
System.out.println(s1 == s2);           // false
String s3 = s1.intern();
String s4 = s1.intern();
System.out.println(s3 == s4);           // true
```

如果是采用 "bbb" 这种字面量的形式创建字符串，会自动地将字符串放入 String Pool 中。

```
String s5 = "bbb";
String s6 = "bbb";
System.out.println(s5 == s6);  // true
```

在 Java 7 之前，String Pool 被放在运行时常量池中，它属于永久代。而在 Java 7，String Pool 被移到堆中。这是因为永久代的空间有限，在大量使用字符串的场景下会导致 OutOfMemoryError 错误。

- [StackOverflow : What is String interning?](https://stackoverflow.com/questions/10578984/what-is-string-interning)
- [深入解析 String#intern](https://tech.meituan.com/in_depth_understanding_string_intern.html)

## new String("abc")

使用这种方式一共会创建两个字符串对象（前提是 String Pool 中还没有 "abc" 字符串对象）。

- "abc" 属于字符串字面量，因此编译时期会在 String Pool 中创建一个字符串对象，指向这个 "abc" 字符串字面量；
- 而使用 new 的方式会在堆中创建一个字符串对象。

创建一个测试类，其 main 方法中使用这种方式来创建字符串对象。

```
public class NewStringTest {
    public static void main(String[] args) {
        String s = new String("abc");
    }
}
```

使用 javap -verbose 进行反编译，得到以下内容：

```
// ...
Constant pool:
// ...
   #2 = Class              #18            // java/lang/String
   #3 = String             #19            // abc
// ...
  #18 = Utf8               java/lang/String
  #19 = Utf8               abc
// ...

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=3, locals=2, args_size=1
         0: new           #2                  // class java/lang/String
         3: dup
         4: ldc           #3                  // String abc
         6: invokespecial #4                  // Method java/lang/String."<init>":(Ljava/lang/String;)V
         9: astore_1
// ...
```

在 Constant Pool 中，#19 存储这字符串字面量 "abc"，#3 是 String Pool 的字符串对象，它指向 #19 这个字符串字面量。在 main 方法中，0: 行使用 new #2 在堆中创建一个字符串对象，并且使用 ldc #3 将 String Pool 中的字符串对象作为 String 构造函数的参数。

以下是 String 构造函数的源码，可以看到，在将一个字符串对象作为另一个字符串对象的构造函数参数时，并不会完全复制 value 数组内容，而是都会指向同一个 value 数组。

```
public String(String original) {
    this.value = original.value;
    this.hash = original.hash;
}
```

## String + String 和 String += String 底层原理     

![img](https://img-blog.csdnimg.cn/20190313235136559.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMTk5OTE5,size_16,color_FFFFFF,t_70)

```java
 //运行代码
    public static void main(String[] args) {
        String a= "ab";
        String c= "cd";
        String e = "ab" + "cd" + "";
        String f = a + "cd";
        String g = a + c;
    }
 
   //javap 之后的机器执行码 一步步看看吧 
  public static void main(java.lang.String[]);
    Code:
                                  //@从常量池中取"ab"存入第一个变量a 1-1
       0: ldc           #2                  // String ab      
       2: astore_1
                                  //@从常量池中取"cd"存入第二个变量c  2-1
       3: ldc           #3                  // String cd
       5: astore_2
                                  //@从常量池中取"abcd"存入第三个变量e  3-1
       6: ldc           #4                  // String abcd
       8: astore_3
                                  //@开始计算f=a+"cd": 先new StringBuilder实例，引用入栈 4-1
       9: new           #5                  // class java/lang/StringBuilder
                                  //@栈顶值复制再压入栈顶 4-2
      12: dup
                                  //@调用StringBuilder类实例初始化方法 4-3
      13: invokespecial #6                  // Method java/lang/StringBuilder."<init>":()V
                                  //@将第一个变量a压入栈 4-4  
      16: aload_1
                                  //@调用 StringBuilder的append方法将栈顶元素做参数传入，返回StringBuilder类型 4-5
      17: invokevirtual #7                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
                                  //@从常量池中取"cd" 压入栈 4-6
      20: ldc           #3                  // String cd
                                  //@调用 StringBuilder的append方法将栈顶元素做参数传入，返回StringBuilder类型 4-7
      22: invokevirtual #7                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
                                  //@调用 StringBuilder的toString方法，返回String类型 4-8
      25: invokevirtual #8                  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
                                  //@将toString压入栈顶的值存入第四个变量：f 4-9
      28: astore        4
 
                                  //@类似于4-X的步骤，就是其中的cd 的来源不同
      30: new           #5                  // class java/lang/StringBuilder
      33: dup
      34: invokespecial #6                  // Method java/lang/StringBuilder."<init>":()V
      37: aload_1
      38: invokevirtual #7                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      41: aload_2
      42: invokevirtual #7                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      45: invokevirtual #8                  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      48: astore        5
      50: return

```

由此看出，在执行String+时，底层会产生一个StringBuilder对象和String对象。

下面看几个case

（1）String s1=”1”+”2”+”3”;

 

![img](https://img-blog.csdn.net/20180827123632152?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NDkwMDE4/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

使用包含常量的字符串连接创建是也是常量，编译期就能确定了，直接入字符串常量池，当然同样需要判断是否已经存在该字符串。不会创建 1 ， 2 ， 3

 （2）String s2=”1”+”3”+new String(“1”)+”4”;


    	当使用“+”连接字符串中含有变量时，也是在运行期才能确定的。首先连接操作最开始时如果都是字符串常量，编译后将尽可能多的字符串常量连接在一起，形成新的字符串常量参与后续的连接（可通过反编译工具jd-gui进行查看）。
    
        接下来的字符串连接是从左向右依次进行，对于不同的字符串，首先以最左边的字符串为参数创建StringBuilder对象（可变字符串对象），然后依次对右边进行append操作，最后将StringBuilder对象通过toString()方法转换成String对象（注意：中间的多个字符串常量不会自动拼接）。
    
        实际上的实现过程为：String s2=new StringBuilder(“13”).append(new String(“1”)).append(“4”).toString();
    
        当使用+进行多个字符串连接时，实际上是产生了一个StringBuilder对象和一个String对象。

 ![img](https://img-blog.csdn.net/20180827123647339?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NDkwMDE4/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)


        （3）String s3=new String(“1”)+new String(“1”);

![img](https://img-blog.csdn.net/20180827123737708?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM0NDkwMDE4/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)



对于final字段，编译期直接进行了常量替换（而对于非final字段则是在运行期进行赋值处理的）。
final String str1=”ja”;
final String str2=”va”;
String str3=str1+str2;
在编译时，直接替换成了String str3=”ja”+”va”，因此再次替换成String str3=”JAVA”

## intern

**String.intern()** 方法可以使得所有含相同内容的字符串都共享同一个内存对象。

用处：高并发下加锁？

JDK 1.7后，intern方法还是会先去查询常量池中是否有已经存在，如果存在，则返回常量池中的引用。不存在，则在常量池中生成一个对原字符串的引用。将在堆上的地址引用复制到常量池。

![这里写图片描述](https://img-blog.csdn.net/20170412203343662?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvc29vbmZseQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

# 三、运算

**参数传递**

Java 的参数是以值传递的形式传入方法中，而不是引用传递。

**float 与 double**

Java 不能隐式执行向下转型，因为这会使得精度降低。

1.1 字面量属于 double 类型，不能直接将 1.1 直接赋值给 float 变量，因为这是向下转型。

```
// float f = 1.1;
```

1.1f 字面量才是 float 类型。

```
float f = 1.1f;
```

**隐式类型转换**

因为字面量 1 是 int 类型，它比 short 类型精度要高，因此不能隐式地将 int 类型向下转型为 short 类型。

```
short s1 = 1;
// s1 = s1 + 1;
```

但是使用 += 或者 ++ 运算符会执行隐式类型转换。

```
s1 += 1;
s1++;
```

上面的语句相当于将 s1 + 1 的计算结果进行了向下转型：

```
s1 = (short) (s1 + 1);
```

# 四、关键字

**final**

**final用来修饰变量，方法，类**

**修饰变量表示数据为常量，修饰方法表示方法不可重写，修饰类表示类不可继承。**

1. **数据**

声明数据为常量，可以是编译时常量，也可以是在运行时被初始化后不能被改变的常量。

- 对于基本类型，final 使数值不变；
- 对于引用类型，final 使引用不变，也就不能引用其它对象，但是被引用的对象本身是可以修改的。

```
final int x = 1;
// x = 2;  // cannot assign value to final variable 'x'
final A y = new A();
y.a = 1;
```

**2. 方法**

声明方法不能被子类重写。

private 方法隐式地被指定为 final，如果在子类中定义的方法和基类中的一个 private 方法签名相同，此时子类的方法不是重写基类方法，而是在子类中定义了一个新的方法。

**3. 类**

声明类不允许被继承。

## final，finally，finalize

**final用来修饰变量，方法，类**

**修饰变量表示数据为常量，修饰方法表示方法不可重写，修饰类表示类不可继承。**

**finally只能用在try/catch语句中处理异常，后面的语句一定会被执行。**

**finalize()在gc，对象被回收的时候被调用。**

## static

**用来修饰静态变量，静态方法，静态内部类，被它修饰的在类加载时就完成加载。**

**1. 静态变量**

- 静态变量：又称为类变量，也就是说这个变量属于类的，类所有的实例都共享静态变量，可以直接通过类名来访问它。静态变量在内存中只存在一份。
- 实例变量：每创建一个实例就会产生一个实例变量，它与该实例同生共死。

```
public class A {

    private int x;         // 实例变量
    private static int y;  // 静态变量

    public static void main(String[] args) {
        // int x = A.x;  // Non-static field 'x' cannot be referenced from a static context
        A a = new A();
        int x = a.x;
        int y = A.y;
    }
}
```

**2. 静态方法**

静态方法在类加载的时候就存在了，它不依赖于任何实例。所以静态方法必须有实现，也就是说它不能是抽象方法。

```
public abstract class A {
    public static void func1(){
    }
    // public abstract static void func2();  // Illegal combination of modifiers: 'abstract' and 'static'
}
```

只能访问所属类的静态字段和静态方法，方法中不能有 this 和 super 关键字，因此这两个关键字与具体对象关联。

```
public class A {

    private static int x;
    private int y;

    public static void func1(){
        int a = x;
        // int b = y;  // Non-static field 'y' cannot be referenced from a static context
        // int b = this.y;     // 'A.this' cannot be referenced from a static context
    }
}
```

**3. 静态代码块**

静态代码块在类初始化时运行一次。

```
public class A {
    static {
        System.out.println("123");
    }

    public static void main(String[] args) {
        A a1 = new A();
        A a2 = new A();
    }
}
123
```

**4. 静态内部类**

非静态内部类依赖于外部类的实例，也就是说需要先创建外部类实例，才能用这个实例去创建非静态内部类。而静态内部类不需要。

```
public class OuterClass {

    class InnerClass {
    }

    static class StaticInnerClass {
    }

    public static void main(String[] args) {
        // InnerClass innerClass = new InnerClass(); // 'OuterClass.this' cannot be referenced from a static context
        OuterClass outerClass = new OuterClass();
        InnerClass innerClass = outerClass.new InnerClass();
        StaticInnerClass staticInnerClass = new StaticInnerClass();
    }
}
```

静态内部类不能访问外部类的非静态的变量和方法。

**6 初始化顺序**

静态变量和静态语句块优先于实例变量和普通语句块，静态变量和静态语句块的初始化顺序取决于它们在代码中的顺序。

```
public static String staticField = "静态变量";
static {
    System.out.println("静态语句块");
}
public String field = "实例变量";
{
    System.out.println("普通语句块");
}
```

最后才是构造函数的初始化。

```
public InitialOrderTest() {
    System.out.println("构造函数");
}
```

存在继承的情况下，初始化顺序为：

- 父类（静态变量、静态语句块）
- 子类（静态变量、静态语句块）
- 父类（实例变量、普通语句块）
- 父类（构造函数）
- 子类（实例变量、普通语句块）
- 子类（构造函数）

# 五、Object 通用方法

```
public native int hashCode()

public boolean equals(Object obj)

protected native Object clone() throws CloneNotSupportedException

public String toString()

public final native Class<?> getClass()

protected void finalize() throws Throwable {}

public final native void notify()

public final native void notifyAll()

public final native void wait(long timeout) throws InterruptedException

public final void wait(long timeout, int nanos) throws InterruptedException

public final void wait() throws InterruptedException
```

## equals()

**1. 等价关系**

两个对象具有等价关系，需要满足以下五个条件：

Ⅰ 自反性

```
x.equals(x); // true
```

Ⅱ 对称性

```
x.equals(y) == y.equals(x); // true
```

**2. 如何重写equals**

- 检查是否为同一个对象的引用，如果是直接返回 true；
- 检查是否是同一个类型，如果不是，直接返回 false；
- 将 Object 对象进行转型；
- 判断每个关键域是否相等。

```
public class EqualExample {

    private int x;
    private int y;
    private int z;

    public EqualExample(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqualExample that = (EqualExample) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        return z == that.z;
    }
}
```

## hashCode()

**在覆盖 equals() 方法时应当总是覆盖 hashCode() 方法，保证等价的两个对象哈希值也相等。**

 需要大量并且快速的对比的话如果都用equal()去做显然效率太低，**所以解决方式是，每当需要对比的时候，首先用hashCode()去对比，如果hashCode()不一样，则表示这两个对象肯定不相等** 

HashSet 和 HashMap 等集合类使用了 hashCode() 方法来计算对象应该存储的位置。

R 一般取 31，因为它是一个奇素数，如果是偶数的话，当出现乘法溢出，信息就会丢失，因为与 2 相乘相当于向左移一位，最左边的位丢失。并且一个数与 31 相乘可以转换成移位和减法：`31*x == (x<<5)-x`，编译器会自动进行这个优化。

**定义一个result，result一般为17，然后对每个字段计算值v，然后执行result = 31 * result +v**

```
@Override
    public int hashCode() {
        int result = dishCode != null ? dishCode.hashCode() : 0;
        result = 31 * result + (soldOut ? 1 : 0);
        result = 31 * result + (stopSell ? 1 : 0);
        result = 31 * result + (currentPrice ? 1 : 0);
        result = 31 * result + (weighing ? 1 : 0);
        result = 31 * result + (categoryCode != null ? categoryCode.hashCode() : 0);
        result = 31 * result + (dealGroupList != null ? dealGroupList.hashCode() : 0);
        return result;
    }

```

## toString()

默认返回 ToStringExample@4554617c 这种形式，其中 @ 后面的数值为散列码的无符号十六进制表示。

```
public class ToStringExample {

    private int number;

    public ToStringExample(int number) {
        this.number = number;
    }
}
ToStringExample example = new ToStringExample(123);
System.out.println(example.toString());
ToStringExample@4554617c
```

## clone()

**1. cloneable**

clone() 是 Object 的 protected 方法。必须要重写才能调用。

```
public class CloneExample {
    private int a;
    private int b;
}
CloneExample e1 = new CloneExample();
// CloneExample e2 = e1.clone(); // 'clone()' has protected access in 'java.lang.Object'
```

重写 clone() 得到以下实现：

```
public class CloneExample {
    private int a;
    private int b;

    @Override
    public CloneExample clone() throws CloneNotSupportedException {
        return (CloneExample)super.clone();
    }
}
CloneExample e1 = new CloneExample();
try {
    CloneExample e2 = e1.clone();
} catch (CloneNotSupportedException e) {
    e.printStackTrace();
}
java.lang.CloneNotSupportedException: CloneExample
```

以上抛出了 CloneNotSupportedException，这是因为 CloneExample 没有实现 Cloneable 接口。

应该注意的是，clone() 方法并不是 Cloneable 接口的方法，而是 Object 的一个 protected 方法。Cloneable 接口只是规定，如果一个类没有实现 Cloneable 接口又调用了 clone() 方法，就会抛出 CloneNotSupportedException。

```
public class CloneExample implements Cloneable {
    private int a;
    private int b;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
```

**2. 浅拷贝**

 **浅拷贝：创建一个新对象，对基本类型的字段复制值；对引用类型的话，则复制引用但不复制引用的对象。因此，原始对象及其副本引用同一个对象。** 0bject的clone只能实现浅拷贝。

**3. 深拷贝**

 **深拷贝：创建一个新对象，然后将当前对象的非静态字段复制到该新对象，无论该字段是值类型的还是引用类型，都复制独立的一份。当你修改其中一个对象的任何内容时，都不会影响另一个对象的内容。** 

**4. clone() 的替代方案**

使用 clone() 方法来拷贝一个对象即复杂又有风险，它会抛出异常，并且还需要类型转换。Effective Java 书上讲到，最好不要去使用 clone()，可以使用拷贝构造函数或者拷贝工厂来拷贝一个对象。

```
public class CloneConstructorExample {

    private int[] arr;

    public CloneConstructorExample() {
        arr = new int[10];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
    }

    public CloneConstructorExample(CloneConstructorExample original) {
        arr = new int[original.arr.length];
        for (int i = 0; i < original.arr.length; i++) {
            arr[i] = original.arr[i];
        }
    }

    public void set(int index, int value) {
        arr[index] = value;
    }

    public int get(int index) {
        return arr[index];
    }
}
CloneConstructorExample e1 = new CloneConstructorExample();
CloneConstructorExample e2 = new CloneConstructorExample(e1);
e1.set(2, 222);
System.out.println(e2.get(2)); // 2
```

# 六、继承

## 访问权限

Java 中有三个访问权限修饰符：private、protected 以及 public，如果不加访问修饰符，表示包级可见。

可以对类或类中的成员（字段和方法）加上访问修饰符。

- 类可见表示其它类可以用这个类创建实例对象。
- 成员可见表示其它类可以用这个类的实例对象访问到该成员；

protected 用于修饰成员，表示在继承体系中成员对于子类可见，但是这个访问修饰符对于类没有意义。

设计良好的模块会隐藏所有的实现细节，把它的 API 与它的实现清晰地隔离开来。模块之间只通过它们的 API 进行通信，一个模块不需要知道其他模块的内部工作情况，这个概念被称为信息隐藏或封装。因此访问权限应当尽可能地使每个类或者成员不被外界访问。

如果子类的方法重写了父类的方法，那么子类中该方法的访问级别不允许低于父类的访问级别。这是为了确保可以使用父类实例的地方都可以使用子类实例去代替，也就是确保满足里氏替换原则。

字段决不能是公有的，因为这么做的话就失去了对这个字段修改行为的控制，客户端可以对其随意修改。例如下面的例子中，AccessExample 拥有 id 公有字段，如果在某个时刻，我们想要使用 int 存储 id 字段，那么就需要修改所有的客户端代码。

```
public class AccessExample {
    public String id;
}
```

可以使用公有的 getter 和 setter 方法来替换公有字段，这样的话就可以控制对字段的修改行为。

```
public class AccessExample {

    private int id;

    public String getId() {
        return id + "";
    }

    public void setId(String id) {
        this.id = Integer.valueOf(id);
    }
}
```

但是也有例外，如果是包级私有的类或者私有的嵌套类，那么直接暴露成员不会有特别大的影响。

```
public class AccessWithInnerClassExample {

    private class InnerClass {
        int x;
    }

    private InnerClass innerClass;

    public AccessWithInnerClassExample() {
        innerClass = new InnerClass();
    }

    public int getValue() {
        return innerClass.x;  // 直接访问
    }
}
```

## 抽象类与接口的区别

- 从设计层面上看，抽象类提供了一种 IS-A 关系，需要满足里式替换原则，即子类对象必须能够替换掉所有父类对象。而接口更像是一种 LIKE-A 关系，它只是提供一种方法实现契约，并不要求接口和实现接口的类具有 IS-A 关系。

- 从使用上来看，一个类可以实现多个接口，但是不能继承多个抽象类。

- 接口中所有的方法隐含的都是抽象的（1.8后可以有非抽象方法。而抽象类则可以同时包含抽象和非抽象的方法。

- 接口的成员只能是 public 的，而抽象类的成员可以有多种访问权限。

  抽象类不能通过new实例化，只能在父类引用指向子类对象时实例化。

**4. 使用选择**

使用接口：

- 要让不相关的类都实现一个方法，例如不相关的类都可以实现 Compareable 接口中的 compareTo() 方法；
- 实现多个接口

使用抽象类：

- 需要在几个相关的类中共享代码。
- 需要能控制继承来的成员的访问权限，而不是都为 public。
- 需要继承非静态和非常量字段。

在很多情况下，接口优先于抽象类。因为接口没有抽象类严格的类层次结构要求，**可以灵活地为一个类添加行为**。并且从 Java 8 开始，接口也可以有默认的方法实现，使得修改接口的成本也变的很低。

- [Abstract Methods and Classes](https://docs.oracle.com/javase/tutorial/java/IandI/abstract.html)
- [深入理解 abstract class 和 interface](https://www.ibm.com/developerworks/cn/java/l-javainterface-abstract/)
- [When to Use Abstract Class and Interface](https://dzone.com/articles/when-to-use-abstract-class-and-intreface)

## super

- **super() 函数用来访问父类的构造函数。**

  从而委托父类完成一些初始化的工作。应该注意到，子类一定会调用父类的构造函数来完成初始化工作，一般是调用父类的默认构造函数，如果子类需要调用父类其它构造函数，那么就可以使用 super() 函数。

- 访问父类的成员：如果子类重写了父类的某个方法，可以通过使用 super 关键字来引用父类的方法实现。

```
public class SuperExample {

    protected int x;
    protected int y;

    public SuperExample(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void func() {
        System.out.println("SuperExample.func()");
    }
}
public class SuperExtendExample extends SuperExample {

    private int z;

    public SuperExtendExample(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    @Override
    public void func() {
        super.func();
        System.out.println("SuperExtendExample.func()");
    }
}
SuperExample e = new SuperExtendExample(1, 2, 3);
e.func();
SuperExample.func()
SuperExtendExample.func()
```

# 七、反射

类在第一次使用时才动态加载到 JVM 中。类加载时，每个类在内存中生成一个 java.lang.Class 对象，包含了与类有关的信息。反射通过获取到Class类，来得到属性、构造方法、普通方法

也可以使用 Class.forName(“全类名”)； 该方法让当前类加载到JVM中，返回一个 Class 对象。

反射核心就是JVM 在运行时才动态加载类或调用方法/访问属性

Class 和 java.lang.reflect 一起对反射提供了支持，java.lang.reflect 类库主要包含了以下三个类：

- **Field** ：可以使用 get() 和 set() 方法读取和修改 Field 对象关联的字段；
- **Method** ：可以使用 invoke() 方法调用与 Method 对象关联的方法；
- **Constructor** ：可以用 Constructor 的 newInstance() 创建新的对象。

**反射的优点：**

- 可以利用全类名创建实例，访问private的属性和方法。
- **反射最重要的用途就是开发各种通用框架**

**反射的缺点：**

- **性能开销**   ：反射涉及了动态类型的解析，所以 JVM 无法对这些代码进行优化。因此，反射操作的效率要比那些非反射操作低得多。我们应该避免在经常被执行的代码或对性能要求很高的程序中使用反射。

- **内部暴露**   ：由于反射允许代码执行一些在正常情况下不被允许的操作（比如访问私有的属性和方法），所以使用反射可能会导致意料之外的副作用，这可能导致代码功能失调并破坏可移植性。反射代码破坏了抽象性，因此当平台发生改变的时候，代码的行为就有可能也随着变化。

  **尽量不用反射**

- [Trail: The Reflection API](https://docs.oracle.com/javase/tutorial/reflect/index.html)

- [深入解析 Java 反射（1）- 基础](http://www.sczyh30.com/posts/Java/java-reflection-1/)

# 八、异常

**异常类的父类是Throwable ，分为Error 和 Exception。其中 Error 用来表示 JVM 无法处理的错误，Exception 分为两种：**

- **RuntimeException无法捕获，程序会崩溃。**
- **其他异常（例如IO）需要用 try...catch... 或者 throws exception处理，并且可以从异常中恢复；**

自定义异常类继承Exception类，构造函数中传入string，调用super（）方法传入string。在要抛出异常的函数使用throws关键字，catch中写自己创建的异常类。 (MyException me)

```java
public class MyException extends Exception {
    //异常信息
    private String message;
    //构造函数
    public MyException(String message){
        super(message);
        this.message = message;
    }
    //获取异常信息,由于构造函数调用了super(message),不用重写此方法
    //public String getMessage(){
    //    return message;
    //}
}

```

throws：用在方法声明后面，跟的是异常类名，如果产生异常就把异常抛给调用自己的方法。
throw：则是用来抛出一个具体的异常类型。该代码必须处于try块里，或处于带throws声明的方法中

exception种类 ： concurrentmodifyexception    NullPointerException   ClassNotFoundException   ArrayIndexOutOfBoundsException   SQLException  



# 九、泛型

**泛型把数据类型被指定为一个参数，是通过编译时擦除类型信息实现的。**

泛型必须为引用数据类型，用在collection，map约束容器中是同一种类型。

泛型类：作为泛型类的成员字段或成员方法的参数间接出现

![image-20200304182415228](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200304182415228.png)

泛型方法：![image-20200304182523722](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200304182523722.png)

泛型方法不能重载，所以就有了通配符。

```java
public void print1(List<Integer> objects){		
}
public void print2(List<String> strings){		
}
//但是后来聪明的程序员使用通配符解决了这个泛型不能重载的问题
public void print(List<? extends Object> list){		
}
```

**通配符只能使用定义好的泛型**。  ![image-20200304182702018](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200304182702018.png)

**限定通配符**：一种是*<? extends T>*意思是类型必须是T的子类，另一种是*<? super T>*，意思是类型必须是它的父类

**非限定通配符**：<?> 任意类型

返回值，参数类型替换成泛型符号

- [Java 泛型详解](http://www.importnew.com/24029.html)
- [10 道 Java 泛型面试题](https://cloud.tencent.com/developer/article/1033693)

# 十 日期

java.util 包提供了 Date 类来封装日期和时间。

String和Date之间做转化。使用SimpleDateFormat类的parse方法，字符串转换成Date对象，format方法，可以将Date对象转换成字符串。

![image-20200229162732613](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200229162732613.png)

# 十一、特性

## Java 8新特性

- jdk 8  lambda  与内部类的区别？
- 接口可以有默认方法和静态方法
- [Stream API](http://www.javacodegeeks.com/2014/05/the-effects-of-programming-with-java-8-streams-on-algorithm-performance.html)将生成环境的函数式编程
- hashmap concurrenthashmap

## Java 与 C++ 的区别

- Java 面向对象，C++ 可以面向对象也可以面向过程。
- Java 运行在JVM上，C++不是。
- Java 没有指针，只有引用，而 C++ 有指针。
- Java 有垃圾回收机制，而 C++ 需要手动回收。
- Java 不支持多重继承，只能通过实现多个接口来达到相同目的，而 C++ 支持多重继承。

  

  

[What are the main differences between Java and C++?](http://cs-fundamentals.com/tech-interview/java/differences-between-java-and-cpp.php)

## JRE or JDK

JDK是Java Development Kit的缩写，包含jre+开发工具

JRE包含JVM+核心类库

# 十二 日志

```java
public class Demo {
    private static Logger logger = Logger.getLogger(vincent_player_framt.class);
    
    public static void main(String[] args) throws Exception {
        // debug级别的信息  
        logger.debug("This is debug message.");  
        // info级别的信息  
        logger.info("This is info message.");  
        // error级别的信息  
        logger.error("This is error message."); 
    }
}
```

pom文件中导入log4j的依赖,然后配置log4j.properties文件

# 十三 单元测试

junit单元测试，确保单个方法顺利运行，修改代码只要确保对应单元测试通过即可。

添加maven依赖，在src目录下的test目录测试

使用**断言(**Assertion)测试期望结果  

assertEquals(3, new Calculator().calculate("1 + 2"));

assertNull(x): 断言为null

assertTrue(x > 0): 断言为true

@Test

@Before方法中初始化测试资源  test前调用

@After方法中释放测试资源	test后调用

@BeforeClass: 初始化非常耗时的资源, 例如创建数据库。在构造方法前调用

@AfterClass: 清理@BeforeClass创建的资源, 例如创建数据库。在构造方法后调用

异常测试：测试是否抛出指定类型异常   @Test(expected=Exception.class)

# 问题

**继承和组合的区别？**继承，父类的实现对于子类是可见的，所以我们一般称之为白盒复用。对象持有（其实就是组合）要求建立一个号的接口，但是整体类和部分类之间不会去关心各自的实现细节，即它们之间的实现细节是不可见的，故成为黑盒复用。继承是在编译时刻静态定义的，即是静态复用，在编译后子类和父类的关系就已经确定了。而组合这是运用于复杂的设计，它们之间的关系是在运行时候才确定的，即在对对象没有创建运行前，整体类是不会知道自己将持有特定接口下的那个实现类。在扩展方面组合比集成更具有广泛性。


**深拷贝与浅拷贝**，简单点来说，就是假设B复制了A，当修改A时，看B是否会发生变化，如果B也跟着变了，说明这是浅拷贝，如果B没变，那就是深拷贝

**基本类型和引用类型有什么区别**
**基本类型和引用类型在赋值，方法，参数传递，以及比较时的区别**

 **如何跳出多层循环**

思路：对每一层设置标志位，设置方法---变量名：

**类的静态属性和实例属性分别是什么概念**

**如何覆盖一个类的equals方法？**

覆盖equals方法必须同时覆盖hashcode。然后比较每个属性，任一属性不相等就返回false

x.equals(x)必须返回true。

y.equals(x)返回true时，x.equals(y)必须返回true。

x.equals(y)返回true，并y.equals(z)返回true，那么x.equals(z)也应该返回true。

**重写类的方法为什么要重写hashcode**

用hashcode方法提前校验，避免每一次都调用equals方法，提高效率

保证是同一个对象，如果重写了equals方法，而没有重写hashcode方法，会出现equals相等的对象，hashcode不相等的情况，重写hashcode方法就是为了避免这种情况的出现。

**类的静态属性和实例属性有哪些初始化方式？比如构造方法**
答案：定义申明初始化表达式，构造方法，静态/非静态初始化程序块
**java里的多态怎么理解**
**你刚才讲到重载和重写，重载是什么概念？**
**其他都一样，只有泛型不一样，是不是重载？**

**描述java异常的类结构**
**检查异常和非检查异常的区别**
**如何定义自己的非检查异常类**

java8有哪些特性？lambda

**lambda和内部类有什么区别？**

Lambda表达式和匿名内部类的区别
所需类型不同
　　●匿名内部类:可以是接口，也可以是抽象类，还可以是具体类
　　●Lambda表达式:只能是接口，且只有一个未实现方法
实现原理不同
　　●匿名内部类:编译之后，产生-一个单独的.class字节码文件
　　●Lambda表达式:编译之后，没有一个单独的.class字节码文件。对应的字节码会在运行的时候动态生成

**SimpleDateFormat是不是线程安全的**

![image-20200229162732613](C:\Users\we\AppData\Roaming\Typora\typora-user-images\image-20200229162732613.png)