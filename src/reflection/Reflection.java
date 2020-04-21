package reflection;

import org.junit.Test;
import reflection.util.Foo;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

public class Reflection {
    public static void main(String[] args) throws Exception {
        /**
            需求：在不能改变类的任何代码的前提下，可以创建任意的类的对象，并且执行其中任意方法

            步骤：
                1.将需要创建的对象的全类名和需要执行的方法，定义在配置文件中
                2.在程序中加载执行配置文件
                3.使用反射技术来加载文件进内存
                4.创建对象
                5.执行方法
         * */

        //1.加载配置文件
        //1.1创建对象
        Properties pro = new Properties();
        //1.2加载配置文件，转换为一个集合（双列map集合，Properties本身就是map的子类）
        //1.2.1获取Class目录下的配置文件（用类加载器完成）
        //获取字节码文件对应的类加载器：类加载器把字节码文件加载进内存，返回一个ClassLoader
        ClassLoader classLoader = Reflection.class.getClassLoader();
        //用ClassLoader找配置文件字节流文件
        InputStream is = classLoader.getResourceAsStream("reflection/util/pro.properties");
        //把字节流传进去即完成加载，抛出异常
        pro.load(is);

        //2.获取配置文件中定义的数据
        String className = pro.getProperty("className");
        String methodName = pro.getProperty("methodName");

        //3.加载该类进内存
        Class cls = Class.forName(className);
        //4.创建对象
        Object obj = cls.newInstance();
        //5.获取方法对象
        Method method = cls.getDeclaredMethod(methodName);
        method.setAccessible(true);
        //6.执行方法
        method.invoke(obj);
    }

    @Test
    public void testClass() throws Exception {
        /**
         运行时加载类有三种方式，分别对应加载过程的三种状态
             1.还在.class文件中，jvm未加载该类时，手动输入包名进行加载
                多用于配置文件中，通过读取文件，加载类
             2.若jvm内存已经加载过该类，则直接加载该类
                多用于参数的传递
             3.若jvm堆中已经有了该类的对象，则通过该对象进行加载
                通过对象获取字节码
         * */

        //1.未加载该类
        Class cls = Class.forName("reflection.util.Foo");
        //2.加载过改类
        Class cls2 = Foo.class;
        //3.通过对象加载该类
        Object o = new Foo();
        Class cls3 = o.getClass();

        System.out.println(cls.getName());

        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            System.out.println(field);
        }

        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            System.out.println(method);
        }

        Constructor[] con = cls.getConstructors();
        for (Constructor constructor : con) {
            System.out.println(constructor);
        }
    }

    @Test
    public void testField() throws Exception {
        // 运行时第一次加载该类
        Class c = Foo.class;
        Field field = c.getDeclaredField("fooName");
        Foo foo = new Foo();
        // 暴力反射，忽略安全检查，不然会报错
        field.setAccessible(true);
        // 更新值
        field.set(foo, "zhangsan");
        System.out.println(field.get(foo));
    }

    @Test
    public void testConstructor() throws Exception {
        Class cls = Foo.class;
        Constructor con = cls.getConstructor(String.class);
        // 构建对象
        Object o = con.newInstance("lisi");
        System.out.println(o);
    }

    @Test
    public void testMethod() throws Exception {
        Class cls = Foo.class;
        //Method method = cls.getDeclaredMethod("say");
        Method method = cls.getDeclaredMethod("say", String.class);
        //创建一个对象不使用constructor的一个办法
        Object o = cls.newInstance();
        //暴力反射，逃避安全检查
        method.setAccessible(true);
        method.invoke(o, " lisi");
    }
}
