package rpc.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

public class Stub {
    public static Object getStub(Class clazz) {
        // 通过动态代理的方式得到Class的对象
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = new Socket("127.0.0.1", 8888);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                // 获取 远程过程调用需要用的方法的 类、参数类型、参数
                String clazzName = clazz.getName();
                String methodName = method.getName();
                Class[] parametersTypes = method.getParameterTypes();
                // 按照顺序填写到二进制流中，在服务器端也应按照相应的顺序接受数据
                oos.writeUTF(clazzName);
                oos.writeUTF(methodName);
                oos.writeObject(parametersTypes);
                oos.writeObject(args);
                oos.flush();

                // 接收远程过程调用的结果
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Object object = (Object) ois.readObject();

                oos.close();
                ois.close();

                return object;
            }
        };

        Object object = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
        return object;
    }
}
