# Spring

## Spring 框架概述

1. Spring是轻量级的开源JavaEE框架
2. 解决企业应用开发的复杂性
3. 两个核心：IOC AOP
   1. IOC：控制反转，把创建对象过程交给Spring进行管理
   2. Aop：面向切面，不修改源代码进行功能增强

![Spring5模块](http://ruiimg.hifool.cn/Spring5模块.bmp)

## IOC

### 概念

1. 控制反转，把对象创建和对象之间的调用过程，交给Spring进行管理
2. 目的：降低耦合度

### 底层原理：**xml解析，工厂模式，反射**

![图2](http://ruiimg.hifool.cn/图2.png)

- IOC思想基于IOC容器完成，IOC容器底层就是对象工厂
- Spring提供IOC容器的两种实现：(两个接口)
  - BeanFactory：IOC容器基本实现，是Spring内部的使用接口，一般不提供开发人员使用
    - 加载配置文件的时候不会创建对象，在获取（使用）对象才去创建对象
  - ApplicationContext：BeanFactory接口的子接口，提供更多更强大的功能，一般由开发人员进行使用。
    - 加载配置文件的时候就会把在配置文件对象进行创建。![20210423101009](http://ruiimg.hifool.cn/20210423101009.png)
  
### IOC的Bean管理

> IOC的Bean管理的两个操作

1. Spring创建对象
2. Spring注入属性

> Bean管理的两种操作方式

- 基于xml配置文件
  - 基于xml方式创建对象
    - 在spring配置文件中，使用bean标签，标签里添加对应属性，即可实现对象创建
      ```xml
      <bean id="user" class="my.dave.spring5.User"></bean>
      ```
    - bean标签属性
      1. id属性：唯一标识
      2. class属性：类全路径
      3. name属性：早期属性，与id类似，可以使用特殊符号
    - 创建对象的时候默认使用无参构造器完成对象创建
- 基于xml方式注入属性
  - **DI：是IOC中的一种具体实现，就是依赖注入，注入属性，在创建对象的基础之上完成**
  - 注入方式：
    - 使用set方法
      ```xml
      <!--set方法注入属性-->
      <bean id="book" class="my.dave.spring5.Book">
          <!--使用property完成属性注入-->
          <property name="bName" value="易筋经"></property>
          <property name="bAuthor"><null></null></property>
      </bean>
      ```
    - 有参数构造注入
      ```xml
      <!--有参数构造注入属性-->
      <bean id="orders" class="my.dave.spring5.Orders">
          <constructor-arg name="oName" value="电脑"></constructor-arg>
          <constructor-arg name="address" value="China"></constructor-arg>
      <!--        <constructor-arg index="0" value="电脑"></constructor-arg>-->
      </bean>
      ```

> 两种Bean

1. 普通bean：在配置文件中定义bean类型就是返回类型
2. 工厂bean(FactoryBean)：在配置文件定义bean类型可以和返回类型不一样

> Bean的作用域

1. Spring默认bean单实例对象
2. 设置单实例多实例（属性scope）（多实例注解：@Scope("prototype")）
   1. singleton单实例，prototype多实例
   2. - 设置singleton，加载spring配置文件时候就会创建单实例对象
      - 设置prototype，调用getBean方法时创建多实例对象

### Bean的生命周期

1. 通过构造器创建bean实例（无参数构造）
2. 为bean的属性设置值和对其他bean引用（调用set方法）
3. **把bean实例传递给bean后置处理器** postProcessBeforeInitialization
4. 调用bean的初始化方法（需要配置初始化方法）
5. **把bean实例传递给bean后置处理器** postProcessAfterInitialization
6. bean可以使用（获取到对象）
7. 当容器关闭时候，调用bean的销毁方法（需要配置销毁的方法）

```java
public interface BeanPostProcessor {
    @Nullable
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Nullable
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```

> xml自动装配 autowired

- 根据指定装配规则（属性名称或者属性类型），Spring自动将匹配的属性进行注入

> 注解

- 创建对象
  - 四个注解功能是一样的，都可以用来创建bean实例
  1. @Component
  2. @Service
  3. @Controller
  4. @Repository
- 属性注入
  1. @Autowired：根据属性类型进行自动装配
  2. @Qualifier：根据属性名称进行注入
  3. @Resource：可以根据类型注入，可以根据名称注入
  4. @Value：注入普通类型属性

## AOP

> 定义：面向切面编程，利用AOP可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性，同时提高了开发的效率。

> AOP：不通过修改源代码的方式添加新的功能

### AOP底层原理

- 动态代理
  - 有接口，使用JDK动态代理
    - 创建接口实现类代理对象增强类的方法
    - ![20210517142303](http://ruiimg.hifool.cn/20210517142303.png)
  - 没有接口：使用CGLIB动态代理
    - 创建子类的代理对象，增强类的方法
    - ![20210517142519](http://ruiimg.hifool.cn/20210517142519.png)

- JDK：
  - 利用拦截器(拦截器必须实现InvocationHanlder)加上反射机制生成一个实现代理接口的匿名类，在调用具体方法前调用InvokeHandler来处理。
  - JDK动态代理只能对实现了接口的类生成代理，而不能针对类。
- CGLIB动态代理：
  - 利用ASM开源包，对代理对象类的class文件加载进来，通过修改其字节码生成子类来处理。
  - CGLIB是针对类实现代理，主要是对指定的类生成一个子类，覆盖其中的方法，并覆盖其中方法实现增强，但是因为采用的是继承，所以该类或方法最好不要声明成final，对于final类或方法，是无法继承的。

> 动态代理：https://cloud.tencent.com/developer/article/1461796

> JDK动态代理：java.lang.reflect.Proxy类

1. 调用newProxyInstance方法 ![20210517143024](http://ruiimg.hifool.cn/20210517143024.png)
   1. 类加载器
   2. 增强方法所在的类，这个类实现的接口，支持多个接口
   3. 实现这个接口InvocationHandler，创建代理对象，写增强的方法
2. JDK动态代理代码
   1. 创建接口，定义方法
   2. 创建接口实现类，实现方法
   3. 使用Proxy类创建接口代理对象

### AOP术语

1. 连接点：类里面的哪些方法可以被增强，这些方法称为连接点
2. 切入点：实际被真正增强的方法，称为切入点
3. 通知（增强）
   1. 实际增强的逻辑部分称为通知（增强）
   2. 通知有多种类型
      1. 前置通知
      2. 后置通知
      3. 环绕通知
      4. 异常通知
      5. 最终通知：类似finally
4. 切面
   1. 把通知应用到切入点的过程

### AOP操作

1. Spring框架一般基于AspectJ实现AOP操作
   - AspectJ：不是Spring组成部分，是独立AOP框架，一般把AspectJ和Spring框架一起使用，进行AOP操作。
2. 基于AspectJ实现AOP操作
   1. 基于xml配置文件实现
   2. 基于注解方式实现（常用）
3. 项目总引入AOP依赖![20210517153924](http://ruiimg.hifool.cn/20210517153924.png)
4. 切入点表达式
   1. 作用：知道对哪个类里面的哪个方法进行增强
   2. 语法结构：`execution([权限修饰符][返回类型][类全路径][方法名称]([参数列表]))` 权限修饰符可以省略

> 操作

1. 创建类，在类中定义方法
    ```java
    public class User {
        public void add(){
    //        int i = 1/0;
            System.out.println("add...");
        }
    }
    ```
2. 创建增强类（编写增强逻辑）
    ```java
    //增强的类
    public class UserProxy {
      public void before() {
        //前置通知 
        System.out.println("before......"); 
      } 
    }
    ```
3. 通知配置
   1. 在spring配置文件中，开启注解扫描
      ```xml
      <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xmlns:context="http://www.springframework.org/schema/context"
          xmlns:aop="http://www.springframework.org/schema/aop"
          xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                              http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
                              http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">
      <!--    开启注解扫描-->
      <context:component-scan base-package="my.dave.spring5.aop"></context:component-scan>
      ```
    1. 使用注解创建User和UserProxy对象![20210517162641](http://ruiimg.hifool.cn/20210517162641.png)
    2. 在增强类上面添加注解@Aspect
      ```java
      //增强的类
      @Component
      @Aspect //生成代理对象
      public class UserProxy {}
      ```
    3. 在spring配置文件中开启生成代理对象
      ```xml
      <!--    开启Aspect生成代理对象-->
          <aop:aspectj-autoproxy></aop:aspectj-autoproxy>
      ```
4. 配置不同类型的通知：在增强类中，在作为通知方法上面添加通知类型注解，使用切入点表达式配置
    ```java
    //增强的类
    @Component
    @Aspect //生成代理对象
    public class UserProxy {
    
        //前置通知
        @Before(value = "execution(* my.dave.spring5.aop.User.add(..))")
        public void before(){
            System.out.println("before...");
        }
    
        //后置（返回）通知
        @AfterReturning(value = "execution(* my.dave.spring5.aop.User.add(..))")
        public void afterReturning(){
            System.out.println("afterReturning...");
        }
    
        //最终通知
        @After(value = "execution(* my.dave.spring5.aop.User.add(..))")
        public void after(){
            System.out.println("after...");
        }
    
      //异常通知 
        @AfterThrowing(value = "execution(* my.dave.spring5.aop.User.add(..))")
        public void afterThrowing(){
            System.out.println("afterThrowing...");
        }
    
        //环绕通知
        @Around(value = "execution(* my.dave.spring5.aop.User.add(..))")
        public void around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
            System.out.println("环绕之前...");
            proceedingJoinPoint.proceed();
            System.out.println("环绕之后...");
        }
    }
    ```
    ![20210517163001](http://ruiimg.hifool.cn/20210517163001.png)

## JdbcTemplate

> JdbcTemplate：Spring框架对JDBC进行封装，使用JdbcTemplate方便实现对数据库操作

## 事务

> 事务：事务是数据库操作最基本单元，逻辑上一组操作，要么都成功，如果有一个失败所有都失败

> 四个特性（ACID）

1. 原子性
2. 一致性
3. 隔离性
4. 持久性

### 事务操作

1. 事务添加到JavaEE三层结构里面的Service层（业务逻辑层）
2. Spring进行事务管理操作
   - 编程式事务管理，在代码中硬编码。(不推荐使用)
   - 声明式事务管理，在配置文件中配置（推荐使用）
3. 声明式事务管理
   1. 基于注解方式
   2. 基于xml配置文件
4. Spring进行声明式事务管理，底层使用AOP
5. Spring事务管理API
   1. 提供一个接口，代表事务管理器，这个接口针对不同的框架提供不同的实现类 ![20210518170318](http://ruiimg.hifool.cn/20210518170318.png)

> 声明式事务管理注解：@Transactional

1. 可以加在类上，也可以加在方法上
2. 事务传播行为**![20210519103022](http://ruiimg.hifool.cn/20210519103022.png)
3. **isolation：事务隔离级别**
   1. 脏读：一个未提交的事务读取到另一个未提交事务的数据。
   2. 不可重复读：一个未提交事务读取到另一提交事务修改的数据。
   3. 幻读：一个未提交事务读取到另一提交事务添加的数据。
   ![20210519114403](http://ruiimg.hifool.cn/20210519114403.png)

## WebFlux

> 是SPring5添加的新模块，用于web开发，功能和SpringMVC类似，Webflux使用当前一种比较流行的响应式编程出现的框架
> Webflux是一种异步非阻塞的框架，核心是基于Reactor的相关API实现

> Webflux特点：

1. 非阻塞式，在有限的资源下，提高系统吞吐量和伸缩性
2. 函数时编程，Spring5框架基于java8，Webflux使用java8函数式编程方式实现路由请求

![20210519162216](http://ruiimg.hifool.cn/20210519162216.png)

> 响应式编程（RP）：响应式编程是一种面向数据流和变化传播的编程范式。这意味着可以在编程语言中很方便地表达静态或动态的数据流，而相关的计算模型会自动将变化的值通过数据流进行传播。**观察者模式**

## BeanFactory和FactoryBean

https://www.cnblogs.com/aspirant/p/9082858.html

- BeanFactory：它负责生产和管理bean的一个工厂。在Spring中，BeanFactory是IOC容器的核心接口，它的职责包括：实例化、定位、配置应用程序中的对象及建立这些对象间的依赖。BeanFactory只是个接口，并不是IOC容器的具体实现
- FactoryBean：用户可以通过实现该接口定制实例化Bean的逻辑
  - 以Bean结尾，表示它是一个Bean，不同于普通Bean的是：它是实现了FactoryBean<T>接口的Bean，根据该Bean的ID从BeanFactory中获取的实际上是FactoryBean的getObject()返回的对象，而不是FactoryBean本身，如果要获取FactoryBean对象，请在id前面加一个&符号来获取。

## SpringBoot启动

### 启动过程

![20210823164353](http://ruiimg.hifool.cn/20210823164353.png)



![img](http://haoimg.hifool.cn/img/format,png.png)

我们将各步骤总结精炼如下：

1. 通过 `SpringFactoriesLoader` 加载 `META-INF/spring.factories` 文件，获取并创建 `SpringApplicationRunListener` 对象
2. 然后由 `SpringApplicationRunListener` 来发出 `starting` 消息
3. 创建参数，并配置当前 `SpringBoot` 应用将要使用的 `Environment`
4. 完成之后，依然由 `SpringApplicationRunListener` 来发出 `environmentPrepared` 消息
5. 创建 `ApplicationContext`
6. 初始化 `ApplicationContext`，并设置 `Environment`，加载相关配置等
7. 由 `SpringApplicationRunListener` 来发出 `contextPrepared` 消息，告知Spring Boot 应用使用的 `ApplicationContext` 已准备OK
8. 将各种 `beans` 装载入 `ApplicationContext`，继续由 `SpringApplicationRunListener` 来发出 `contextLoaded` 消息，告知 Spring Boot 应用使用的 `ApplicationContext` 已装填OK
9. `refresh ApplicationContext`，完成IoC容器可用的最后一步
10. 由 `SpringApplicationRunListener` 来发出 `started` 消息
11. 调用`callRunners(...)`方法，让实现了`ApplicationRunner`和`CommandLineRunner`接口类的`run` 方法得以执行，用于在 Spring 应用上下文准备完毕后，执行一些额外操作。从而完成最终的程序的启动。
12. 由 `SpringApplicationRunListener` 来发出 `running` 消息，告知程序已运行起来了