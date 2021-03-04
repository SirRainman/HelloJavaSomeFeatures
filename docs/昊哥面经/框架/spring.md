# Servlet

![在这里插入图片描述](https://img-blog.csdn.net/20181002102613861?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MjIyOTA1Ng==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

```java
public interface Servlet {
    //初始化方法
    void init(ServletConfig var1) throws ServletException;

    ServletConfig getServletConfig();
    //执行方法
    void service(ServletRequest var1, ServletResponse var2) throws ServletException, IOException;

    String getServletInfo();
   //销毁的方法
    void destroy();
}
```

servlet启动时，开始加载servet,生命周期开始。

两个被继承的类在被继承的时候，都会加载相关的静态资源，所以自定义的类一旦被创建，就会在编译器自动加载静态资源。

```java
public abstract class HttpServlet extends GenericServlet implements Serializable {
    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_TRACE = "TRACE";
    private static final String HEADER_IFMODSINCE = "If-Modified-Since";
    private static final String HEADER_LASTMOD = "Last-Modified";
    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
    //静态资源在编译器就会被加载
    private static ResourceBundle lStrings = ResourceBundle.getBundle("javax.servlet.http.LocalStrings");


  // GenericServlet类
public abstract class GenericServlet implements Servlet, ServletConfig, Serializable {
    private static final String LSTRING_FILE = "javax.servlet.LocalStrings";
    private static ResourceBundle lStrings = ResourceBundle.getBundle("javax.servlet.LocalStrings");
    private transient ServletConfig config;
```

## 生命周期：

**1.Servlet容器通过反射创建Servlet实例**

**2. 通过调用 init () 方法进行初始化**

**3.调用 service()方法，根据方法调用相应的doGet（）doPost（）方法**　

**4.通过调用 destroy()方法终止，让JVM回收**

调用实例的init()和destroy()方法都只进行一次。

## 如何响应

![浏览器访问Servlet过程](http://c.biancheng.net/uploads/allimg/190610/5-1Z610154349322.png)

浏览器向 Web 服务器发送了一个 HTTP 请求，Web 服务器收到请求后，创建一个 HttpServletRequest 和 HttpServletResponse 对象，然后再调用相应的 Servlet 程序。

 Servlet 先从 HttpServletRequest 对象中读取数据信息，然后通过 service() 方法处理请求消息，并将处理后的响应数据写入到 HttpServletResponse 对象中。最后，Web 服务器会从 HttpServletResponse 对象中读取到响应数据，并发送给浏览器。

需要注意的是，在 Web 服务器运行阶段，每个 Servlet 都只会创建一个实例对象，针对每次 HTTP 请求，Web 服务器都会调用所请求 Servlet 实例的 service（HttpServletRequest request，HttpServletResponse response）方法，并重新创建一个 request 对象和一个 response 对象。

## 单实例多线程

Servlet采用单实例多线程来处理多个请求同时访问。收到一个访问Servlet的请求，调度线程从线程池中选取一个工作线程，将请求传递给该线程，然后由这个线程执行Servlet的service()方法。 

解决线程不安全：

一：单实例变成多实例，但过时了，因为耗费资源多，服务器压力大。

二：加线程锁，但数据会重复出现(没有同步机制)，且运行效率低。

三：解决线程安全问题的最佳办法：不要写全局变量，而写局部变量(即改变变量的作用域)。

一个项目只有一个ServletContext对象，服务器会为每个应用创建一个ServletContext对象，我们可以在N多个Servlet中来获取这个唯一的对象，使用它可以给多个Servlet传递数据，与Tomcat同生同死。

## 设计一个tomcat？

 Tomcat是满足Servlet规范的容器，所以Tomcat需要提供API：doGet/doPost/service。 

# Spring

设计思想：Spring 是一种轻量级开发框架，旨在提高开发人员的开发效率以及系统的可维护性。重要模块是Spring Core，Spring Aop

# SpringBoot

springboot是spring的简化版，用大量的默认配置，简化了xml的配置。

能直接使用java的main方法启动内嵌的Tomcat，不用配置war包

**Spring Boot应用程序的入口点是使用@SpringBootApplication注释的类**

# SpringFrameWork

## IOC

**控制反转，从程序主动创建对象变为了由IOC容器注入，需要获取对象时就通过名字去IOC容器获取。**

**DI依赖注入也由IOC容器负责，有两种依赖注入方式：构造器依赖注入：** 构造器依赖注入通过容器触发一个类的构造器来实现的，该类有一系列参数，每个参数代表一个对其他类的依赖。

**Setter方法注入：** Setter方法注入是容器通过调用无参构造器或无参static工厂 方法实例化bean之后，调用该bean的setter方法，即实现了基于setter的依赖注入。

**IOC容器：**

 BeanFactory是springbean的根接口，只能提供基本的DI功能。 最主要的方法是getBean(String beanName)。第一次getbean时实例化。  

 ApplicationContext(应用上下文)  是BeanFactory的子接口 ，提供解析配置信息等功能，一次性加载所有单例非懒加载的bean。

**IOC过程：**

创建ApplicationContext对象

**a. 加载javabean的xml配置文件，把bean的类型，名字，作用域等信息解析成 BeanDefinition 放在 一个concurrenthashmap 里。**

**b. 调用 getBean 的时候，从 BeanDefinition 所属的 Map 里，拿出  Class 对象进行实例化，同时，如果有依赖关系，将递归调用  getBean 方法 —— 完成依赖注入。**

## **bean的生命周期**

1. **实例化Bean**

对于BeanFactory容器，当客户向容器请求一个尚未初始化的bean时，或初始化bean的时候需要注入另一个尚未初始化的依赖时，容器就会调用createBean进行实例化。 
对于ApplicationContext容器，当容器启动结束后，便实例化所有的bean。 
容器通过获取BeanDefinition对象中的信息进行实例化。并且这一步仅仅是简单的实例化，并未进行依赖注入。

**2 进行依赖注入**

可以通过构造器和setter进行注入

**3  通过PostProcessor接口对bean增强，添加功能** 

**4 调用初始化方法**
**5 使用bean**

**6 IOC容器关闭时，调用销毁方法**

### 循环依赖问题

结论：

1、构造器注入和**prototype**类型的field注入发生循环依赖时都**无法初始化**

2、field注入**单例**的bean时，尽管有循环依赖，但bean仍然**可以被成功初始化**

通过三级缓存解决循环依赖问题。第三级缓存存放早期引用。刚创建好还未填充属性的对象的引用。假设a对象依赖于b，b依赖于a。A对象创建时，会把早期引用放入缓存，发现自己依赖对象 B，此时就会去尝试 get(B)，b对象创建并拿到了a对象的早期引用，进行完属性注入以后，则返回一个b对象，然后A可以顺利拿到B进行初始化。

**注解原理**

 **除了@ResponseBody，controller层如何标准返回给前端所要的数据类型？你会怎么实现？**

## **bean的作用域**

- **singleton :** bean在每个Spring ioc 容器中只有一个实例。无状态的bean使用singleton
- **prototype**：一个bean的定义可以有多个实例。有状态的bean使用prototype
- **request**：每次http请求都会创建一个bean，该作用域仅在基于web的Spring ApplicationContext情形下有效。
- **session**：同一个HTTP Session共享一个Bean。该作用域仅在基于web的Spring ApplicationContext情形下有效。
- **global-session**：一个全局的HTTP Session中共享一个bean。该作用域仅在基于web的Spring ApplicationContext情形下有效。

##  @Autowired

@Autowired它会根据类型找到对应的Bean，如果不是唯一的，就会根据其属性名称和Bean的名称进行匹配。如果匹配成功，就会使用该Bean，如果无法匹配，则抛出异常。

## AOP

**AOP是面向切面编程，把日志，权限认知，事务处理这些主业务逻辑无关的代码封装成切面。从而实现代码复用和解耦。**可用于权限认证、日志、事务处理。日志代码和业务逻辑毫无关系，安全性、异常处理和透明的持续性也都是如此。

**AOP有几个概念：**

**1） 切面（Aspect）**是一个添加了@aspect注解的一个类，里面定义pointcut方法和advice方法

2） **通知（Advice）**advice是对方法的增强

**通知有5种注解：**

前置通知（Before）：在目标方法被调用之前调用通知功能

后置通知（After）：在目标方法完成之后调用通知，不关心方法的输出是什么。是“返回通知”和“异常通知”的并集。

返回通知（After-returning）：在目标方法成功执行之后调用通知

异常通知（After-throwing）：在目标方法抛出异常后调用通知

环绕通知（Around）通知包裹了被通知的方法，可同时定义前置通知和后置通知。

3） **切点（Pointcut）**

**定义一个空方法，加上@pointcut注解，参数为execution（正则表达式）。**全类名，方法名，通配符

4）连接点（Join point）

可以被切入的代表方法

5） 引入（Introduction）

引介让一个切面可以声明被通知的对象实现了任何他们没有真正实现的额外接口，而且为这些对象提供接口的实现。

引入允许我们向现有的类添加新方法或属性。这个新方法和实例变量就可以被引入到现有的类中，从而可以再无需修改这些现有的类的情况下，让它们具有新的行为和状态。

5） 织入（Weaving）：织入是把切面应用到目标对象并创建新的代理对象的过程。切面在指定的连接点被织入到目标对象中。

- 编译器：切面在目标类编译时被织入。这种方式需要特殊的编译器。
- 类加载期：切面在目标类被引入应用之前增强该目标类的字节码。
- 运行期：切面在应用运行的某个时刻被织入。

**AOP的实现原理是动态代理。有两种方式，JDK动态代理和CGLIB动态代理：**

**jdk动态代理：创建一个实现接口InvocationHandler的类，在inkove方法通过反射调用被代理类的方法，并加入自己的逻辑，进行功能增强。在运行过程中通过Proxy类的newinstance（）动态创建代理类。**

**被代理类没有实现接口就用CGlib动态代理，其原理是通过字节码为一个类创建子类，并在子类中采用方法拦截的技术拦截所有父类方法的调用，并加入逻辑。**

```java
@Component
@Aspect
//切面
public class Aspect{
    //前面是pointcut,括号里是joinpoint
	@Pointcut("execution(* com.fch.service..*.*(..))")
    public void pointCut(){
	
    }
    @Pointcut("execution(* com.fch.dao..*.*(..))")
    public void pointCut(){
	
    }
    @before(@Pointcut())
    public void advice(){
		system.out.println("aop before--- logger");
    }
}
```



## 写过AOP吗？

## spring设计模式

**工厂模式**
Spring使用工厂模式可以通过 BeanFactory 或 ApplicationContext 创建 bean 对象。

两者对比：

BeanFactory ：延迟注入(使用到某个 bean 的时候才会注入),相比于BeanFactory 来说会占用更少的内存，程序启动速度更快。
ApplicationContext ：容器启动的时候，不管你用没用到，一次性创建所有 bean 。BeanFactory 仅提供了最基本的依赖注入支持，ApplicationContext 扩展了 BeanFactory ,除了有BeanFactory的功能还有额外更多功能，所以一般开发人员使用ApplicationContext会更多。
ApplicationContext的三个实现类：

ClassPathXmlApplication：把上下文文件当成类路径资源。
FileSystemXmlApplication：从文件系统中的 XML 文件载入上下文定义信息。
XmlWebApplicationContext：从Web系统中的XML文件载入上下文定义信息。

单例设计模式
在我们的系统中，有一些对象其实我们只需要一个，比如说：线程池、缓存、对话框、注册表、日志对象、充当打印机、显卡等设备驱动程序的对象。事实上，这一类对象只能有一个实例，如果制造出多个实例就可能会导致一些问题的产生，比如：程序的行为异常、资源使用过量、或者不一致性的结果。

**单例模式:**

对于频繁使用的对象，可以省略创建对象所花费的时间，也会减轻 GC 压力，缩短 GC 停顿时间。
Spring 中 bean 的默认作用域就是 singleton(单例)的。

**代理模式**
AOP 

**模板模式**


Spring 中 jdbcTemplate、hibernateTemplate 等以 Template 结尾的对数据库操作的类，它们就使用到了模板模式。一般情况下，我们都是使用继承的方式来实现模板模式，但是 Spring 并没有使用这种方式，而是使用Callback 模式与模板方法模式配合，既达到了代码复用的效果，同时增加了灵活性。

**观察者模式**

Spring 事件驱动模型中的三种角色
事件角色
ApplicationEvent (org.springframework.context包下)充当事件的角色,这是一个抽象类，它继承了java.util.EventObject并实现了 java.io.Serializable接口。

Spring 中默认存在以下事件，他们都是对 ApplicationContextEvent 的实现(继承自ApplicationContextEvent)：

ContextStartedEvent：ApplicationContext 启动后触发的事件;
ContextStoppedEvent：ApplicationContext 停止后触发的事件;
ContextRefreshedEvent：ApplicationContext 初始化或刷新完成后触发的事件;
ContextClosedEvent：ApplicationContext 关闭后触发的事件。

**适配器模式**

**spring AOP中的适配器模式**
我们知道 Spring AOP 的实现是基于代理模式，但是 Spring AOP 的增强或通知(Advice)使用到了适配器模式，与之相关的接口是AdvisorAdapter 。Advice 常用的类型有：BeforeAdvice（目标方法调用前,前置通知）、AfterAdvice（目标方法调用后,后置通知）、AfterReturningAdvice(目标方法执行结束后，return之前)等等。每个类型Advice（通知）都有对应的拦截器:MethodBeforeAdviceInterceptor、AfterReturningAdviceAdapter、AfterReturningAdviceInterceptor。Spring预定义的通知要通过对应的适配器，适配成 MethodInterceptor接口(方法拦截器)类型的对象（如：MethodBeforeAdviceInterceptor 负责适配 MethodBeforeAdvice）。

**spring MVC中的适配器模式**
在Spring MVC中，DispatcherServlet 根据请求信息调用 HandlerMapping，解析请求对应的 Handler。解析到对应的 Handler（也就是我们平常说的 Controller 控制器）后，开始由HandlerAdapter 适配器处理。HandlerAdapter 作为期望接口，具体的适配器实现类用于对目标类进行适配，Controller 作为需要适配的类。

为什么要在 Spring MVC 中使用适配器模式？ Spring MVC 中不同类型的 Controller 通过不同的方法来对请求进行处理。如果不用适配器模式的话，添加新controller需要添加if else判断，也违反了开闭原则 。

## 事务

- **编程式事务**使用TransactionTemplate或者直接使用底层的PlatformTransactionManager。对于编程式事务管理，spring推荐使用TransactionTemplate。事务控制代码和业务代码交织在一起。
- **声明式事务**是建立在AOP之上的，基于@Transactional注解。其本质是对方法前后进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。

声明式事务缺点是，最细粒度只能作用到方法级别，而编程式事务可以作用到代码块级别。

解决方法：将需要进行事务管理的代码块独立为方法等等。

### 传播机制

事务的传播性一般用在事务嵌套的场景，比如一个事务方法里面调用了另外一个事务方法，那么两个方法是各自作为独立的方法提交还是内层的事务合并到外层的事务一起提交，这就是需要事务传播机制的配置来确定怎么样执行。
常用的事务传播机制如下：

- PROPAGATION_REQUIRED
  Spring默认的传播机制，能满足绝大部分业务需求，如果外层有事务，则当前事务加入到外层事务，一块提交，一块回滚。如果外层没有事务，新建一个事务执行
- PROPAGATION_REQUES_NEW
  该事务传播机制是每次都会新开启一个事务，同时把外层事务挂起，当当前事务执行完毕，恢复上层事务的执行。如果外层没有事务，执行当前新开启的事务即可
- PROPAGATION_SUPPORT
  如果外层有事务，则加入外层事务，如果外层没有事务，则直接使用非事务方式执行。完全依赖外层的事务
- PROPAGATION_NOT_SUPPORT
  该传播机制不支持事务，如果外层存在事务则挂起，执行完当前代码，则恢复外层事务，无论是否异常都不会回滚当前的代码
- PROPAGATION_NEVER
  该传播机制不支持外层事务，即如果外层有事务就抛出异常
- PROPAGATION_MANDATORY
  与NEVER相反，如果外层没有事务，则抛出异常
- PROPAGATION_NESTED
  该传播机制的特点是可以保存状态保存点，当前事务回滚到某一个点，从而避免所有的嵌套事务都回滚，即各自回滚各自的，如果子事务没有把异常吃掉，基本还是会引起全部回滚的。

# SpringMVC

M : model  V ： View  C：controller

springmvc基于方法开发，一个url对应一个方法，请求参数传递到方法的形参。使用@Controller 标记一个类是Controller ，然后使用@RequestMapping 和@RequestParam 等注解定义URL 和Controller 方法之间的映射，这样的Controller 就能被外界访问到。

SpringMVC的入口是DispatchServlet，它的工作大致可以分为两个部分，一个是初始化，另外一个是请求处理。

逻辑实现其实是在servlet接口的实现类DispatcherServlet中进行的。

## **主要组件**

（1）前端控制器 DispatcherServlet（不需要程序员开发）

作用：接收请求、响应结果，相当于转发器，有了DispatcherServlet 就减少了其它组件之间的耦合度。

（2）处理器映射器HandlerMapping（不需要程序员开发）

作用：根据请求的URL来查找Handler

（3）处理器适配器HandlerAdapter

注意：在编写Handler的时候要按照HandlerAdapter要求的规则去编写，这样适配器HandlerAdapter才可以正确的去执行Handler。

（4）处理器Handler（需要程序员开发）

（5）视图解析器 ViewResolver（不需要程序员开发）

作用：进行视图的解析，根据视图逻辑名解析成真正的视图（view）

（6）视图View（需要程序员开发jsp）

View是一个接口， 它的实现类支持不同的视图类型（jsp，freemarker，pdf等等）

## 请求处理逻辑

https://blog.csdn.net/a362212624/article/details/80431499

https://blog.csdn.net/zcw4237256/article/details/78320566

（1）用户发送请求至前端控制器DispatcherServlet；DispatcherServlet收到请求后，调用doService方法，最终调用doDispatch方法；调用HandlerMapping，获取Handler，即controller；
（2）DispatcherServlet根据handler得到对应的HandlerAdapter，HandlerAdapter 经过适配调用 Handler，（也叫后端控制器)；
（3）Handler执行完成返回ModelAndView，HandlerAdapter将其返回给DispatcherServlet；
（4）DispatcherServlet将ModelAndView传给ViewResolver解析得到view，渲染view返回客户端（即将模型数据填充至视图中）


![img](https://img-blog.csdn.net/20180708224853769?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2E3NDUyMzM3MDA=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

## 常用注解

@RequestMapping：用于处理请求 url 映射的注解，可用于类或方法上。用于类上，则表示类中的所有响应请求的方法都是以该地址作为父路径。

@RequestBody：注解实现接收http请求的json数据，将json转换为java对象。

@ResponseBody：注解实现将conreoller方法返回对象转化为json对象响应给客户。

@RequestParam: 注解接收参数

 **除了@ResponseBody，controller层如何标准返回给前端所要的数据类型？你会怎么实现？**

**用注解@RequestBody接受请求体的数据，常用于POST方法，@RequestParam接受请求头的参数，常用于Get方法。**（post，put也可以）

**@RequestBody**

**接收前端传给后端的json字符串，匹配对应类的属性，调setter方法将值赋**

核心逻辑分析示例：
        假设前端传的json串是这样的： {"name1":"邓沙利文","age":123,"mot":"我是一只小小小小鸟~"} 后端的模型只有name和age属性，以及对应的setter/getter方法；给出一般用到的deserializeFromObject(JsonParser p, DeserializationContext ctxt)方法的核心逻辑：

![img](https://img-blog.csdn.net/20180915034035239?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2p1c3RyeV9kZW5n/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

后端@RequestBody注解对应的类在将HTTP的输入流(含请求体)装配到目标类(即:@RequestBody后面
               的类)时，会根据json字符串中的key来匹配对应实体类的属性，如果匹配一致且json中的该key对应的值
               符合(或可转换为)实体类的对应属性的类型要求时，会调用实体类的setter方法将值赋给该属性。

**@RequestParam**

@RequestParam有三个配置参数：

- `required` 表示是否必须，默认为 `true`，必须。
- `defaultValue` 可设置请求参数的默认值。
- `value` 为接收url的参数名（相当于key值）。

**响应报文**

**@responseBody把controller的方法返回的对象写入到响应报文的请求体中。**

**@RequestMapping用来处理请求地址映射**

**@ResponseBody**

@responseBody注解的作用是将**controller的方法返回的对象**通过适当的转换器转换为指定的格式之后，**写入到response对象的body区**，通常用来返回JSON数据或者是XML.如果是bean对象，会调用对象的getXXX（）方法获取属性值并且以键值对的形式进行封装，进而转化为json串。如果是map集合，采用get(key)方式获取value值，然后进行封装.

使用此注解之后不会再走试图处理器，而是直接将数据写入到输入流中，效果等同于通过response对象输出指定格式的数据。

```java
　　@RequestMapping("/login")
　　@ResponseBody
　　public User login(User user){
　　　　return user;
　　}
```

User字段：userName pwd   那么在前台接收到的数据为：'{"userName":"xxx","pwd":"xxx"}'



**@RequestMapping**

RequestMapping是一个用来**处理请求地址映射**的注解，可用于类或方法上。用于类上，表示类中的所有响应请求的方法都是以该地址作为父路径。

RequestMapping注解有六个属性，下面我们把她分成三类进行说明。

1、 value， method；

value：   指定请求的实际地址，指定的地址可以是URI Template 模式（后面将会说明）；

method： 指定请求的method类型， GET、POST、PUT、DELETE等；

 

2、 consumes，produces；

consumes： 指定处理请求的提交内容类型（Content-Type），例如application/json, text/html;

produces:  指定返回的内容类型，仅当request请求头中的(Accept)类型中包含该指定类型才返回；

 

3、 params，headers；

params： 指定request中必须包含某些参数值是，才让该方法处理。

headers： 指定request中必须包含某些指定的header值，才能让该方法处理请求。


# Mybatis 

Mybatis装了JDBC，用XML 来配置

**#{}和${}的区别是什么？**

#{}是预编译处理，${}是字符串替换。

Mybatis在处理#{}时，会将sql中的#{}替换为?号，调用PreparedStatement的set方法来赋值；

Mybatis在处理${}时，就是把${}替换成变量的值。

使用#{}可以有效的防止SQL注入，提高系统安全性。

**用了四个注解**@Select @Update @Delete@Insert

```java
@Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.miaosha_price  from miaosha_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
	
	public GoodsVo getGoodsVoByGoodsId(@Param("goodsId")long goodsId);
...
@Insert("insert into miaosha_order(user_id, goods_id, order_id)values(#{userId},#{goodsId},#{orderId})")
	public int insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);
```

