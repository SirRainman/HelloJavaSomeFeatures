package reflection;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reflection.util.Foo;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Properties;

@Slf4j
public class Reflection {
    /**
     * 需求：在不能改变类的任何代码的前提下，通过反射，在运行时创建任意的类的对象，并且执行其中任意方法
     * 步骤：
     *      1.将需要创建的对象的全类名和需要执行的方法，定义在配置文件中
     *      2.在程序中加载执行配置文件
     *      3.使用反射技术来加载文件进内存
     *      4.创建对象
     *      5.执行方法
     * */
    public static void main(String[] args) throws Exception {

        // 1.加载配置文件
        // 1.1创建对象
        Properties pro = new Properties();
        // 1.2加载配置文件，转换为一个集合（双列map集合，Properties本身就是map的子类）
        // 1.2.1获取Class目录下的配置文件（用类加载器完成）
        // 获取字节码文件对应的类加载器：类加载器把字节码文件加载进内存，返回一个ClassLoader
        ClassLoader classLoader = Reflection.class.getClassLoader();
        // 用ClassLoader找配置文件字节流文件，注意文件的路径的书写：当前module下source为跟路径
        InputStream is = classLoader.getResourceAsStream("reflection/util/pro.properties");
        // 把字节流传进去即完成加载，抛出异常
        pro.load(is);

        // 2.获取配置文件中定义的数据
        String className = pro.getProperty("className");
        String methodName = pro.getProperty("methodName");

        // 3.加载该类进内存
        Class cls = Class.forName(className);
        // 4.创建对象
        //      class.newInstance(): 调用此方法，创建运行时类的对象，该方法内部调用了该类的空参构造器，
        //      1. 运行时类必须提供空参构造器
        //      2. 空参构造器的访问权限必须够，通常设置为public
        //      额外技巧：javaBean 中，要求提供一个public空参构造器
        //      1. 可以在运行时，通过反射创建该类
        //      2. 子类继承此运行时类并默认调用super()时，保证父类有此构造器
        Object obj = cls.newInstance();
        // 5.获取方法对象
        Method method = cls.getDeclaredMethod(methodName);
        method.setAccessible(true); // 暴力反射，忽略安全检查，不然会报错
        // 6.执行方法
        method.invoke(obj);
    }

    /**
     * 功能：加载类 - 获取属性 - 获取方法 - 获取构造函数 - 生成对象
     *
     * 注：
     * 运行时加载类有三种方式，分别对应加载过程的三种状态
     * 1.还在.class文件中，jvm未加载该类时，手动输入包名进行加载
     *      多用于配置文件中，通过读取文件，加载类
     * 2.若jvm内存已经加载过该类，则直接加载该类
     *      多用于参数的传递
     * 3.若jvm堆中已经有了该类的对象，则通过该对象进行加载
     *      通过对象获取字节码
     * @throws Exception
     */
    @Test
    public void testClass() throws Exception {
        // 一、加载类
        log.info("*** Get Class object:");
        //1.未加载该类
        Class cls = Class.forName("reflection.util.Foo");
        //2.加载过改类
        Class cls2 = Foo.class;
        //3.通过对象加载该类
        Object o = new Foo();
        Class cls3 = o.getClass();

        log.info("  Class name is {}", cls.getName());

        // 二、获取相应的属性(包括私有属性)
        //  1.getFields() 获取运行时类及其父类声明为public访问权限的属性
        //  2.getDeclaredFields() 获取运行时类所有的 属性（但不包含父类的属性）。
        log.info("*** Get DeclaredFields:");
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            log.info("  Field is {}", field);
        }

        // 三、获取声明的方法
        //  1.getMethods() 获取运行时类及其父类声明为public访问权限的方法
        //  2.getDeclaredMethods() 获取运行时类所有的 属性（但不包含父类的属性）。
        log.info("*** Get DeclaredMethods:");
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            log.info("  Method is {}", method);
        }

        // 四：拿到构造函数
        log.info("*** Get Constructors:");
        Constructor[] con = cls.getConstructors();
        for (Constructor constructor : con) {
            log.info("  Constructor is {}", constructor);
        }

        // 五、创建对象
        log.info("*** Create an object");
        // 拿到符合要求的构造器
        Constructor constructor = cls.getConstructor(String.class);
        Object obj1 = constructor.newInstance("Rain");
        // 注意这种办法生成对象，只能调用空参的构造器
        Object obj3 = cls.newInstance();
        log.info("  the create object is {}", obj1);
    }

    /**
     * 反射 - 属性
     *      1.getFields() 获取运行时类及其父类声明为public访问权限的属性
     *      2.getDeclaredFields() 获取运行时类所有的 属性（但不包含父类的属性）。
     * @throws Exception
     */
    @Test
    public void testField() throws Exception {
        // 1.运行时第一次加载该类
        Class<Foo> clazz = Foo.class;

        // 2.拿到相应的属性
        Field fieldFooName = clazz.getDeclaredField("fooName");
        // 注：暴力反射，忽略安全检查，不然会报错。保证当前属性是可以访问到的
        fieldFooName.setAccessible(true);

        // 3.更新值
        Foo foo = clazz.newInstance();
        fieldFooName.set(foo, "Rain");

        // 4.取值
        log.info("private field 'fooName' is {}", fieldFooName.get(foo));


        // 5.获取所有的属性
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            // 1. 获取变量名称
            log.info("*** Filed Name is {} ***", field.getName());

            // 2. 获取权限修饰符
            int modifiers = field.getModifiers();
            log.info("      modifier is {} ", Modifier.toString(modifiers));

            // 3. 获取数据类型
            Class fieldType = field.getType();
            log.info("      type is {}", fieldType.getName());
        }
    }

    /**
     * 测试执行方法
     *      1.getMethods() 获取运行时类及其父类声明为public访问权限的方法
     *      2.getDeclaredMethods() 获取运行时类所有的 属性（但不包含父类的属性）。
     *
     * @throws Exception
     */
    @Test
    public void testMethod() throws Exception {
        // 1. load class
        Class cls = Foo.class;

        // 2. get method according to parameters Types
        // Method method = cls.getDeclaredMethod("say");
        Method methodSay = cls.getDeclaredMethod("say", String.class);

        // 3. create object
        Object object = cls.newInstance();

        // 4. invoke
        // 暴力反射，逃避安全检查
        methodSay.setAccessible(true);
        String res = (String) methodSay.invoke(object, " lisi");
        log.info("invoke result: {}", res);


        // 5.获取所有的方法
        Method[] methods = cls.getDeclaredMethods();
        for(Method method : methods) {
            log.info("*** Method Name is {}", method.getName());

            // 获取方法的注解
            Annotation[] annotations = method.getAnnotations();
            for(Annotation anno : annotations) {
                log.info("      annotation is {}", anno);
            }

            // 方法的权限修饰符
            log.info("      modifier is {}", Modifier.toString(method.getModifiers()));

            // 方法的返回类型
            log.info("      return type is {}", method.getReturnType().getName());

            // 方法的型参列表
            Class[] parameters = method.getParameterTypes();
            if( !(parameters == null && parameters.length == 0) ) {
                for (int i = 0; i < parameters.length; i++) {
                    log.info("      parameter_{} is {}", i, parameters[i].getName());
                }
            }

            // 方法抛出的异常
            method.getExceptionTypes();
        }
    }

    /**
     * 测试构造函数
     *      1.getConstructors() 获取运行时类及其父类声明为public访问权限的构造方法
     *      2.getDeclaredConstructors() 获取运行时类所有的 构造方法（但不包含父类的属性）。
     * @throws Exception
     */
    @Test
    public void testConstructor() throws Exception {
        Class clazz = Foo.class;
        Constructor constructor = clazz.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        // 构建对象
        Object obj = constructor.newInstance("lisi");
        log.info("the created object is {}", obj);

        log.info("*** getDeclaredConstructors");
        Constructor[] declaredConstructors = clazz.getDeclaredConstructors();
        for (Constructor c : declaredConstructors) {
            log.info("      {}", c);
        }
    }

    /**
     * 测试运行时类的父类
     */
    @Test
    public void testParentClass() {
        Class clazz = Foo.class;
        Class superClazz = clazz.getSuperclass();
        log.info("parent class is {}", superClazz);

        // 带泛型的父类
        Type superGenericClass = clazz.getGenericSuperclass();
        log.info("parent generic super class is {}", superGenericClass);

        // 带泛型的父类的 泛型
        ParameterizedType parameterizedType = (ParameterizedType) superGenericClass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        for(Type type : actualTypeArguments) {
            log.info("{}", type);
        }
    }

    /**
     * 测试运行时类所实现的接口
     */
    @Test
    public void testInterface() {
        Class clazz = Foo.class;
        Class[] interfaces = clazz.getInterfaces();
        for(Class i : interfaces) {
            log.info("{}", i);
        }
    }

    /**
     * 测试运行时类所在的包
     */
    @Test
    public void testPackage() {
        Class clazz = Foo.class;
        Package clazzPackage = clazz.getPackage();
        log.info("class package is {}", clazzPackage);
    }
}
