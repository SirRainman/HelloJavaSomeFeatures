# 1 反射

## 1.1 Java反射机制概述

反射是动态语言的关键，反射机制允许程序在运行时借助Reflection API取得任何类的内部信息，并能直接操作任意对象的内部属性和方法

动态语言：在运行时，代码可以通过给定条件改变代码的自身结构。C#，js，php，python，erlang

静态语言：运行时代码结构不可改变的语言是静态语言。java，c，C++。

* java不是动态语言，但java可以利用反射机制、字节码的操作等方式获得类似动态语言的特性



Java反射的功能：

1. 在运行时判断任意对象的所属类
2. 在运行时构造任意一个类的对象
3. 在运行时获取 任意一个类所具有的成员变量和方法
4. 在运行时获取泛型信息
    1. 什么是泛型信息？
5. 在运行时调用任意一个对象的成员变量和方法
6. 在运行时处理注解
    1. 注解的原理是什么？
7. 生成动态代理



什么时候用反射？

1. 编译时不确定用哪一个类 - 比如通过配置文件制定
2. 运行时根据相应的信息决定创建哪一个对象 

## 1.2 理解java.lang.Class类

JVM加载完类之后，在堆内存的方法区中产生了该类的Class类型的对象（一个类只有一个Class对象），该Class对象包含了该类的完整的类结构信息。

在运行时通过该类的Class对象解析类的结构的方式成为反射。

正常：引入需要的“包类”名称 - 通过new实例话 - 生成实例话对象

反射：运行时 - 得到类的Class对象 - 得到类的具体信息 - 生成实例话对象



获取Class类对象的三种方法

```java
/**
  * 运行时加载类有三种方式，分别对应加载过程的三种状态
  * 1.还在.class文件中，jvm未加载该类时，手动输入包名进行加载
  *      多用于配置文件中，通过读取文件，加载类
  * 2.若jvm内存已经加载过该类，则直接加载该类
  *      多用于参数的传递
  * 3.若jvm堆中已经有了该类的对象，则通过该对象进行加载
  *      通过对象获取字节码
  */
public void testClass() throws Exception {
    // 一、加载类
    log.info("*** Load Class:");
    //1.未加载该类，调用Class的静态方法, 最常用，更能体现动态性
    Class cls = Class.forName("reflection.util.Foo");
    //2.运行时加载过该类
    Class cls2 = Foo.class;
    //3.通过运行时的对象加载该对象的类
    Object o = new Foo();
    Class cls3 = o.getClass();
}
```



## 1.3 类的加载与Classloader的理解

疑问：类的Class对象，和类的加载这个步骤之间有什么联系吗？

### 1.3.1 类的加载机制

类是在运行期间第一次使用时动态加载的，而不是一次性加载所有类。因为如果一次性加载，那么会占用很多的内存。

类的生命周期：

1. **加载（Loading）**
2. **验证（Verification）**
3. **准备（Preparation）**
4. **解析（Resolution）**
5. **初始化（Initialization）**
6. 使用（Using）
7. 卸载（Unloading）

### 1.3.2 类的加载过程：

1. 加载：把class字节码文件载入到jvm中
2. 验证：验证class文件中字节流信息是否符合虚拟机的要求
3. 准备：使用堆中的内存和初始值，对静态变量分配内存和设置初始值。
4. 解析：将常量池中的符号引用设置为直接引用
5. 初始化：开始执行类中定义的 Java 程序代码

**1. 加载**

加载过程中 类的加载器 主要完成以下三件事：

- 通过全类名 获取定义该**类的二进制字节流**，并将字节码加载到内存中。
- 将该字节流表示的**静态存储结构转换为方法区的运行时存储结构**。
- 在内存（堆）中**生成一个代表该类的 java.lang.Class 对象**，作为方法区中该类各种数据的访问入口。

其中二进制字节流可以从以下方式中获取：

- 从 ZIP 包读取，成为 JAR、EAR、WAR 格式的基础。
- 从网络中获取，最典型的应用是 Applet。
- 运行时计算生成，例如动态代理技术，在 java.lang.reflect.Proxy 使用 ProxyGenerator.generateProxyClass 的代理类的二进制字节流。
- 由其他文件生成，例如由 JSP 文件生成对应的 Class 类。

注：类缓存：

* 标准的JavaSE类加载器 可以通过某种要求查找类，一旦某个类加载到了类加载器中，该类将在类加载器中加载（缓存）一段时间，JVM垃圾回收机制可以回收这些java.lang.Class对象

**2. 验证**

验证的目的是确保class文件的字节流中信息符合虚拟机的要求，不会危害虚拟机安全，使得虚拟机免受恶意代码的攻击。

- 文件格式验证
- 源数据验证
- 字节码验证



**3. 准备**

准备阶段**为静态变量（static）分配内存并设置初始值**（final修饰不为0，其他为0），**使用的是方法区的内存和初始值**。

* 在jdk8中，静态变量是存放在堆中的吗？？？

静态变量的初值为jvm默认的初值，而不是程序中设定的初值。(仅包含类变量,不包含实例变量)。初始值一般为 0 值，例如下面的类变量 value 被初始化为 0 而不是 123。

```
public static int value = 123;
```

如果类变量是常量，那么它将初始化为表达式所定义的值而不是 0。例如下面的常量 value 被初始化为 123 而不是 0。

```
public static final int value = 123;
```

而实例变量会在对象实例化时随着对象一起被分配在堆中。

* **实例化不是类加载的一个过程**，类加载发生在所有实例化操作之前，并且类加载只进行一次，实例化可以进行多次。



**4. 解析**

在类加载的解析阶段，会**将常量池的符号引用替换为直接引用**。

这种解析能成立的前提是：

* 方法在程序真正运行之前就有一个可确定的调用版本，并且这个方法的调用版本在运行期是不可改变的。
* “编译期可知，运行期不可变” 的方法，主要包括静态方法和私有方法两大类，

什么是符号引用？什么是直接引用？

1. 符号引用
    * 符号引用以一组符号来描述所引用的目标。
    * 符号引用与虚拟机的内存布局无关，引用的目标并不一定加载到内存中。
    * 在编译时，java类并不知道所引用的类的实际地址，因此只能使用符号引用来代替。比如org.simple.People类引用了org.simple.Language类，在编译时People类并不知道Language类的实际内存地址，因此只能使用符号org.simple.Language。
2. 直接引用
    * 直接指向目标的指针
    * 相对偏移量（比如指向实例变量、实例方法的直接引用都是偏移量）
    * 一个能间接定位到目标的句柄
3. 直接引用是和虚拟机的布局相关的，同一个符号引用在不同的虚拟机实例上翻译出来的直接引用一般不会相同。如果有了直接引用，那引用的目标必定已经被加载入内存中了。



**5. 初始化**

**调<clinit>()方法 给静态变量赋值，执行静态代码块**

初始化阶段才真正开始执行类中定义的 Java 程序代码。初始化阶段是虚拟机执行类构造器 <clinit>() 方法的过程。在准备阶段，类变量已经赋过一次系统要求的初始值，而在初始化阶段，根据程序员通过程序制定的主观计划去初始化类变量和其它资源。

<clinit>() 是由编译器自动收集类中所有类变量的赋值动作和静态语句块中的语句合并产生的，编译器收集的顺序由语句在源文件中出现的顺序决定。特别注意的是，静态语句块只能访问到定义在它之前的类变量，定义在它之后的类变量只能赋值，不能访问。例如以下代码：

```
public class Test {
    static {
        i = 0;                // 给变量赋值可以正常编译通过
        System.out.print(i);  // 这句编译器会提示“非法向前引用”
    }
    static int i = 1;
}
```

由于父类的 <clinit>() 方法先执行，也就意味着父类中定义的静态语句块的执行要优先于子类。例如以下代码：

```
static class Parent {
    public static int A = 1;
    static {
        A = 2;
    }
}

static class Sub extends Parent {
    public static int B = A;
}

public static void main(String[] args) {
     System.out.println(Sub.B);  // 2
}
```

接口中不可以使用静态语句块，但仍然有类变量初始化的赋值操作，因此接口与类一样都会生成 <clinit>() 方法。但接口与类不同的是，执行接口的 <clinit>() 方法不需要先执行父接口的 <clinit>() 方法。只有当父接口中定义的变量使用时，父接口才会初始化。另外，接口的实现类在初始化时也一样不会执行接口的 <clinit>() 方法。

虚拟机会保证一个类的 <clinit>() 方法在多线程环境下被正确的加锁和同步，如果多个线程同时初始化一个类，只会有一个线程执行这个类的 <clinit>() 方法，其它线程都会阻塞等待，直到活动线程执行 <clinit>() 方法完毕。如果在一个类的 <clinit>() 方法中有耗时的操作，就可能造成多个线程阻塞，在实际过程中此种阻塞很隐蔽。



## 1.4 创建运行时类的对象

```java
@Test
public void testClass() throws Exception {
    // 一、加载类
    log.info("*** Get Class object:");
    //1.未加载该类
    Class cls = Class.forName("reflection.util.Foo");
    log.info("  Class name is {}", cls.getName());

    ...
        
    // 五、创建对象
    log.info("*** Create an object");
    Constructor constructor = cls.getConstructor(String.class);
    Object obj = constructor.newInstance("Rain");
    // 注意这种办法生成对象，只能调用空参的构造器
    Object obj2 = cls.newInstance();
    log.info("  the create object is {}", obj);
}
```



## 1.5 获取运行时类的完整结构

[github 代码]: https://github.com/SirRainman/HelloJavaSomeFeatures/tree/master/Reflect



## 1.6 调用运行时类的指定结构

[github 代码]: https://github.com/SirRainman/HelloJavaSomeFeatures/tree/master/Reflect



## 1.7 反射的应用-动态代理



---



# 2 动态代理



## 2.1 代理模式：

* 解决的主要问题：
    * 直接访问对象时会带来很多问题。
    * 为其他对象提供一个代理以控制对某个对象的访问。代理类负责为委托类预处理消息，过滤消息并转发消息，以及进行消息被委托类执行后的后续处理。
* 使用一个代理将被代理的对象包装起来，然后使用代理对象取代原始对象。
* 任何对原始对象的调用都要通过代理进行访问。
* 代理对象决定了是否访问原始对象，以及何时将方法调用转接到原始对象上。



## 2.2 静态代理：

* 特征：
    * 代理类和目标对象类都是在编译时期确定下来的，不利于程序的扩展。
    * 每个代理类只能为一个接口服务，这样会导致程序在开发的过程中会产生很多代理。
    * 动态代理 --- 最好通过一个代理类就完成全部的代理功能

![img](http://haoimg.hifool.cn//img/SouthEast.png)



管理用户的接口

```java
public interface UserServiceInterface {
    public void addUser(String userName);
    public void delUser(String userName);
    public void updateUser(String userName);
    public String getUser(String userName);
}
```



管理用户的实现

```java
public class UserServiceImpl implements UserServiceInterface {
    @Override
    public void addUser(String userName) {
        log.info("UserServiceImpl.addUser()");
    }

    @Override
    public void delUser(String userName) {
        log.info("UserServiceImpl.delUser()");
    }

    @Override
    public void updateUser(String userName) {
        log.info("UserServiceImpl.updateUser()");
    }

    @Override
    public String getUser(String userName) {
        log.info("UserServiceImpl.getUser()");
        return "UserServiceImpl.Rain";
    }
}
```



代理（与被代理的类实现相同的接口）

* 代理类可以为委托类预处理消息、把消息转发给委托类和事后处理消息等。

```Java
public class UserServiceProxy implements UserServiceInterface{
    private UserServiceInterface userService;

    public UserServiceProxy(UserServiceInterface userService) {
        this.userService = userService;
    }

    @Override
    public void addUser(String userName) {
        log.info("Proxy ---> start");
        log.info("Proxy ---> filter some thing...");
        userService.addUser(userName);
        log.info("Proxy ---> handle result thing...");
        log.info("Proxy ---> end");
    }

    @Override
    public void delUser(String userName) {
        userService.delUser(userName);
    }

    @Override
    public void updateUser(String userName) {
        userService.updateUser(userName);
    }

    @Override
    public String getUser(String userName) {
        return userService.getUser(userName);
    }
}
```



主程序

```java
public class Main {
    public static void main(String[] args) {
        UserServiceInterface userService = new UserServiceImpl();
        UserServiceProxy proxy = new UserServiceProxy(userService);
        log.info("Main ---> receive request from client");
        log.info("Main ---> send request to proxy");
        proxy.addUser("Doria");
        log.info("Main ---> get result from proxy");
        log.info("Main ---> respond result to client");
    }
}
```



## 2.3 动态代理：

* 可以在运行期动态创建某个`interface`的实例

* 动态：没有实现类但是在运行期动态创建了一个接口的对象的方式称为动态代码。
* 动态代理实际上是JVM在运行期动态创建class字节码并加载的过程

与静态代理的对比：

* 创建时间的区别：
    * 静态代理类：静态代理事先知道要代理的是什么，由程序员创建或由特定工具自动生成源代码，再对其编译。在程序运行前，代理类的.class文件就已经存在了。
    * 动态代理类：动态代理事先不知道要代理什么东西，只有在运行时才知道。在程序运行时，运用反射机制动态创建而成。
* 代理的数量区别：
    * 静态代理通常只代理一个类，
    * 动态代理是代理一个接口下的多个实现类。

动态代理是实现JDK里的InvocationHandler接口的invoke方法，但注意的是代理的是接口，也就是你的业务类必须要实现接口，通过Proxy里的newProxyInstance得到代理对象。

![img](http://haoimg.hifool.cn//img/05cd18e7e33c5937c7c39bf8872c5753.jpg)



1 定义接口

```java
public interface Human {
    void morning(String name);
}
```



2 被代理类

```java
public class SuperMan implements Human{
    @Override
    public void morning(String name) {
        log.info("*** SuperHuman say hello to {} ***", name);
    }
}
```



3 代理类

```java
/**
 * 想要实现动态代理需要解决的问题：
 *      1 如何根据加载到内存中的被代理类，动态的创建一个代理类及其对象？
 *      2 通过代理类的对象进行方法调用时，如何动态的调用被代理类的同名方法？
 *
 * 无论被代理类是谁，当调用代理类的相应的方法时，代理类对象都会调用被代理类中与之同名的方法
 *
 */
public class DynamicProxyFactory {
    /**
     * 解决问题1：如何根据加载到内存中的被代理类，动态的创建一个代理类及其对象？
     * 调用此方法，返回一个代理类的对象
     * @param obj 传入被代理的对象
     * @return 返回代理类对象
     */
    public static Object getProxyInstance(Object obj) {
        // InvocationHandler作用：负责实现接口的方法调用，当代理对象的原本方法被调用的时候，会重定向到一个方法
        // InvocationHandler handler = new InvocationHandler() {
        //     @Override
        //     public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //         System.out.println(method);
        //
        //         if (method.getName().equals("morning")) {
        //             System.out.println("Good morning, " + args[0]);
        //         }
        //
        //         return null;
        //     }
        // };

        MyInvocationHandler handler = new MyInvocationHandler();

        // 将被代理对象与handler 绑定在一起
        handler.bind(obj);

        // 代理类的类加载器 和 被代理类的类加载器 是相同的；
        // 代理类和被代理类要实现相同的接口
        Object proxyInstance = Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), handler);

        return proxyInstance;
    }
}

@Slf4j
class MyInvocationHandler implements InvocationHandler {
    // 被代理类的对象
    private Object object;

    // 将此InvocationHandler 与 被代理类的对象 绑定
    public void bind(Object object) {
        this.object = object;
    }

    /**
     * 解决问题2：通过代理类的对象进行方法调用时，如何动态的调用被代理类的同名方法？
     * 通过代理类的对象进行调用方法morning()时，会自动调用invoke()方法
     * 将被代理类的要执行的方法morning() 声明在invoke 中
     * @param proxy
     * @param method : 代理类对象的调用的方法，
     *               因为代理类和被代理类实现的是同样的接口，他们的方法是相同的，所以将此方法作为被代理类对象的调用方法
     * @param args
     * @return 返回代理类对象方法调用的返回值，此时将被代理类对象的方法调用返回值作为执行结果返回
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("InvocationHandler ---> before invoke");
        // returnResult 被代理类的方法的执行后的返回值
        Object returnResult = method.invoke(object, args);
        log.info("InvocationHandler ---> after invoke");
        return returnResult;
    }
}

```

4 测试

```java
/**
 * @program: rain-java-ideas
 * @description: 测试动态代理
 * @author: Rain
 * @create: 2021-02-16 20:59
 **/
public class DynamicProxyTest {
    public static void main(String[] args) {
        // 被代理类对象
        SuperMan superMan = new SuperMan();
        // 获得代理类对象
        Human human = (Human) DynamicProxyFactory.getProxyInstance(superMan);
        // 通过代理类对象调用相应的方法时，代理类对象会自动的调用被代理类中 与该方法同名的方法
        human.morning("Doria");
    }
}

```

总结：通过上面两部分的代码能不能看出来动态代理相对于静态代理的优势？

* 静态代理的代码是写死的，一个代理类对应一个被代理类
* 动态代理是动态的，无论传进来的被代理类是什么，都可以代理该类



## 2.4 动态代理与AOP(Aspect Orient Programming)

多个类之间有重复的代码块，把这个代码块抽出来进行服用

### 2.4.1 什么是AOP？

# 3 RPC

![在这里插入图片描述](http://haoimg.hifool.cn//img/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NzMTIzbWxr,size_16,color_FFFFFF,t_70.png)

RPC 的核心功能主要有 5 个部分：

1. 客户端：服务调用方
2. 客户端 Stub：存放服务端地址信息，将客户端的请求参数数据信息打包成网络消息，再通过网络传输发送给服务端。
3. 网络传输：可以是 TCP、HTTP、HTTP2.0等
    1. 服务的调用方与服务的提供方建立 Socket 连接
    2. 服务的调用方通过 Socket 将需要调用的接口名称、方法名称和参数序列化后传递给服务的提供方
    3. 服务的提供方反序列化后再利用反射调用相关的方法。
4. 服务端 Stub：接收客户端发送过来的请求消息并进行解包，然后根据相应的信息调用本地服务进行处理。
5. 服务端：服务的真正提供者。



RPC中设计到的方向：

1. 反射 
2. 动态代理
    1. RPC 会自动给接口生成一个代理类，当我们在项目中注入接口的时候，运行过程中实际绑定的是这个接口生成的代理类。
    2. 在接口方法被调用的时候，它实际上是被生成代理类拦截到了，这样就可以在生成的代理类里面，加入远程调用逻辑。
3. 序列化
4. 网络传输协议
5. 服务发现
    1. 简单的动态代理和反射调用到其他微服务在多线程的情况下，可能会导致并发错乱，需要相关的服务注册中心（Eureka zookeeper） 进行服务发现，给相应的函数一个唯一的id
    2. 在 RPC 中，所有的函数都必须有自己的一个 ID。这个 ID 在所有进程中都是唯一确定的。客户端和服务端分别维护一个函数和Call ID的对应表。
        1. 客户端想要调用函数A，就查找自己所的对应表把A对应的ID通过存根传输
        2. 服务端根据ID在自己这边的表中找到函数，并执行
    3. 网络传输层需要把 Call ID 和序列化后的参数字节流传给服务端，然后再把序列化后的调用结果传回客户端。
6. 负载均衡
7. 服务熔断