package rpc;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

public class Stub {

    // 通过动态代理的方式得到Class的对象
    public static Object getStub(Class clazz) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = new Socket("127.0.0.1", 8888);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                String clazzName = clazz.getName();
                String methodName = method.getName();
                Class[] parametersTypes = method.getParameterTypes();

                oos.writeUTF(clazzName);
                oos.writeUTF(methodName);
                oos.writeObject(parametersTypes);
                oos.writeObject(args);
                oos.flush();

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Object o = (Object) ois.readObject();

                oos.close();
                socket.close();

                return o;
            }
        };

        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, handler);
        return o;
    }
}