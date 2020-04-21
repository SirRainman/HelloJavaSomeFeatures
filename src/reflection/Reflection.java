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
            �����ڲ��ܸı�����κδ����ǰ���£����Դ����������Ķ��󣬲���ִ���������ⷽ��

            ���裺
                1.����Ҫ�����Ķ����ȫ��������Ҫִ�еķ����������������ļ���
                2.�ڳ����м���ִ�������ļ�
                3.ʹ�÷��似���������ļ����ڴ�
                4.��������
                5.ִ�з���
         * */

        //1.���������ļ�
        //1.1��������
        Properties pro = new Properties();
        //1.2���������ļ���ת��Ϊһ�����ϣ�˫��map���ϣ�Properties�������map�����ࣩ
        //1.2.1��ȡClassĿ¼�µ������ļ��������������ɣ�
        //��ȡ�ֽ����ļ���Ӧ���������������������ֽ����ļ����ؽ��ڴ棬����һ��ClassLoader
        ClassLoader classLoader = Reflection.class.getClassLoader();
        //��ClassLoader�������ļ��ֽ����ļ�
        InputStream is = classLoader.getResourceAsStream("reflection/util/pro.properties");
        //���ֽ�������ȥ����ɼ��أ��׳��쳣
        pro.load(is);

        //2.��ȡ�����ļ��ж��������
        String className = pro.getProperty("className");
        String methodName = pro.getProperty("methodName");

        //3.���ظ�����ڴ�
        Class cls = Class.forName(className);
        //4.��������
        Object obj = cls.newInstance();
        //5.��ȡ��������
        Method method = cls.getDeclaredMethod(methodName);
        method.setAccessible(true);
        //6.ִ�з���
        method.invoke(obj);
    }

    @Test
    public void testClass() throws Exception {
        /**
         ����ʱ�����������ַ�ʽ���ֱ��Ӧ���ع��̵�����״̬
             1.����.class�ļ��У�jvmδ���ظ���ʱ���ֶ�����������м���
                �����������ļ��У�ͨ����ȡ�ļ���������
             2.��jvm�ڴ��Ѿ����ع����࣬��ֱ�Ӽ��ظ���
                �����ڲ����Ĵ���
             3.��jvm�����Ѿ����˸���Ķ�����ͨ���ö�����м���
                ͨ�������ȡ�ֽ���
         * */

        //1.δ���ظ���
        Class cls = Class.forName("reflection.util.Foo");
        //2.���ع�����
        Class cls2 = Foo.class;
        //3.ͨ��������ظ���
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
        // ����ʱ��һ�μ��ظ���
        Class c = Foo.class;
        Field field = c.getDeclaredField("fooName");
        Foo foo = new Foo();
        // �������䣬���԰�ȫ��飬��Ȼ�ᱨ��
        field.setAccessible(true);
        // ����ֵ
        field.set(foo, "zhangsan");
        System.out.println(field.get(foo));
    }

    @Test
    public void testConstructor() throws Exception {
        Class cls = Foo.class;
        Constructor con = cls.getConstructor(String.class);
        // ��������
        Object o = con.newInstance("lisi");
        System.out.println(o);
    }

    @Test
    public void testMethod() throws Exception {
        Class cls = Foo.class;
        //Method method = cls.getDeclaredMethod("say");
        Method method = cls.getDeclaredMethod("say", String.class);
        //����һ������ʹ��constructor��һ���취
        Object o = cls.newInstance();
        //�������䣬�ӱܰ�ȫ���
        method.setAccessible(true);
        method.invoke(o, " lisi");
    }
}
