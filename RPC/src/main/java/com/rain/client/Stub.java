package com.rain.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

/**
 * @Program: rain-java-ideas
 * @Description:
 * @Author: HouHao Ye
 * @Create: 2021-03-01 20:10
 **/
public class Stub {
    public static Object getStub(Class clazz) {

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // TODO: 去服务注册中心找服务相对应的ip和端口
                Socket socket = new Socket("127.0.0.1", 8888);


                // TODO：获取 远程过程调用需要用的方法的 类、参数类型、参数
                String clazzName = clazz.getName();
                String methodName = method.getName();
                Class[] parametersTypes = method.getParameterTypes();


                // TODO: 序列化，并发送请求
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeUTF(clazzName);
                oos.writeUTF(methodName);
                oos.writeObject(parametersTypes);
                oos.writeObject(args);
                oos.flush();


                // TODO：接收远程过程调用的结果
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Object result = ois.readObject();
                ois.close();

                // TODO: oos一定不能过早的关闭，否则会报错
                oos.close();
                return result;
            }
        };

        Object object = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
        return object;
    }


}
