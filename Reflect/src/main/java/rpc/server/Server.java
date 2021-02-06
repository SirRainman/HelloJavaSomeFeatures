package rpc.server;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static boolean isRunning = true;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);
        while(isRunning) {
            Socket socket = serverSocket.accept();
            processRequest(socket);
            socket.close();
        }
        serverSocket.close();
    }

    public static void processRequest(Socket socket) throws Exception {
        InputStream inputStream = socket.getInputStream();
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

        // 接收远程过程调用的参数信息
        String clazzName = objectInputStream.readUTF();
        String methodName = objectInputStream.readUTF();
        Class[] parametersTypes = (Class[]) objectInputStream.readObject();
        Object[] args = (Object[]) objectInputStream.readObject();

        // 相应的类进行处理相应的请求
        Class clazz = null;

        // 通过clazzName从服务注册表找到的具体的类，这里省略掉
        clazz = UserServiceInterfaceImpl.class;

        // 通过反射，调用相应的方法进行处理，得到处理结果
        Method method = clazz.getMethod(methodName, parametersTypes);
        Object object = method.invoke(clazz.newInstance(), args);

        // 将结果返回
        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();

        objectOutputStream.close();
        objectInputStream.close();
    }
}
