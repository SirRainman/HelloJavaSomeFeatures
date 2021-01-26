package dynamicProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DynamicProxy {
    public static void main(String[] args) {
        // InvocationHandler作用：当代理对象的原本方法被调用的时候，会重定向到一个方法，
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println(method);

                if (method.getName().equals("morning")) {
                    System.out.println("Good morning, " + args[0]);
                }

                return null;
            }
        };

        // 动态代理：可以在运行期动态创建某个interface的实例
        Hello hello = (Hello) Proxy.newProxyInstance(
                Hello.class.getClassLoader(),
                new Class[]{Hello.class},
                handler
        );

        hello.morning("Rain");
    }
}
