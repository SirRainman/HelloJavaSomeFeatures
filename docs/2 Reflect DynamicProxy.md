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
