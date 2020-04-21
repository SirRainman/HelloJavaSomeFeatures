package rpc;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static boolean running = true;

    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(8888);
        while (running) {
            Socket socket = serverSocket.accept();
            process(socket);
            socket.close();
        }
        serverSocket.close();
    }

    private static void process(Socket socket) throws Exception {
        InputStream in = socket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(in);

        String clazzName = ois.readUTF();
        String methodName = ois.readUTF();
        Class[] parameterTypes = (Class[]) ois.readObject();
        Object[] args = (Object[]) ois.readObject();

        Class clazz = null;

        // 通过clazzName从服务注册表找到的具体的类
        clazz = ServerProductServiceImpl.class;

        Method method = clazz.getMethod(methodName, parameterTypes);
        Object o = method.invoke(clazz.newInstance(), args);

        OutputStream out = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(o);
        oos.flush();
    }
}
