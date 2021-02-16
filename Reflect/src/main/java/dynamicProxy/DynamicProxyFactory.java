package dynamicProxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
